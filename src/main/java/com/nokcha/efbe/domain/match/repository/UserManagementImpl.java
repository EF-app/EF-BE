package com.nokcha.efbe.domain.match.repository;

import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.model.BodyType;
import com.nokcha.efbe.domain.match.model.Drinking;
import com.nokcha.efbe.domain.match.model.Fashion;
import com.nokcha.efbe.domain.match.model.Grooming;
import com.nokcha.efbe.domain.match.model.HairLength;
import com.nokcha.efbe.domain.match.model.HeightBand;
import com.nokcha.efbe.domain.match.model.Ideal;
import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import com.nokcha.efbe.domain.profile.entity.Purpose;
import com.nokcha.efbe.domain.match.model.Self;
import com.nokcha.efbe.domain.match.model.Smoking;
import com.nokcha.efbe.domain.match.model.Tendency;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.pool.CandidateSelector;
import com.nokcha.efbe.domain.profile.entity.CodeKeyword;
import com.nokcha.efbe.domain.profile.entity.CodePersonal;
import com.nokcha.efbe.domain.profile.entity.UserCustomKeyword;
import com.nokcha.efbe.domain.profile.entity.UserKeyword;
import com.nokcha.efbe.domain.profile.entity.UserPersonal;
import com.nokcha.efbe.domain.profile.entity.UserPersonalType;
import com.nokcha.efbe.domain.profile.entity.UserProfile;
import com.nokcha.efbe.domain.profile.repository.ProfileRepository;
import com.nokcha.efbe.domain.profile.repository.UserCustomKeywordRepository;
import com.nokcha.efbe.domain.profile.repository.UserKeywordRepository;
import com.nokcha.efbe.domain.profile.repository.UserPersonalRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.CodeKeywordRepository;
import com.nokcha.efbe.domain.user.repository.CodePersonalRepository;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 통합 지점 1 (Ports & Adapters) — 우리 엔티티 → {@link UserContext} 변환.
 *  매칭 계산기/풀/슬롯/태그/배치는 이 클래스를 경유해서만 도메인을 본다.
 *
 *  ── 흐름 ────────────────────────────────────────────
 *    findEligible(me, cfg):
 *      1) native SQL 로 후보 ID 풀 추리기
 *         · users + user_profile + code_area JOIN
 *         · status / profile_status / last_active / age / 국내·해외 그룹
 *         · block 양방향 제외
 *         · match_actions 통합 필터 (LIKE/SUPER_LIKE/POWER_MESSAGE 영구 + PASS 30일)
 *      2) ID 목록으로 batch fetch (User/Profile/Personal/Keyword/CustomKeyword/CodeArea/CodeKeyword/CodePersonal)
 *      3) 메모리에서 UserContext 조립
 *
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserManagementImpl implements UserManagement {

    private final EntityManager em;
    private final UserRepository userRepo;
    private final ProfileRepository profileRepo;
    private final UserPersonalRepository userPersonalRepo;
    private final UserKeywordRepository userKeywordRepo;
    private final UserCustomKeywordRepository userCustomKeywordRepo;
    private final AreaRepository areaRepo;
    private final CodeKeywordRepository codeKeywordRepo;
    private final CodePersonalRepository codePersonalRepo;

    /* ─────────────────────── public API ─────────────────────── */

    @Override
    public List<UserContext> findEligible(UserContext me, MatchingConfig cfg) {
        boolean meIsOverseas = !CandidateSelector.isDomestic(me);
        LocalDateTime since = LocalDateTime.now().minusDays(cfg.getLastActiveDays());

        @SuppressWarnings("unchecked")
        List<Number> rows = em.createNativeQuery("""
                SELECT u.id
                  FROM users u
                  JOIN user_profile up ON up.user_id = u.id
                  JOIN code_area ca    ON ca.id = u.area_id
                 WHERE u.id != :meId
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.last_active_at >= :since
                   AND ABS(u.age - :myAge) <= :ageMaxDiff
                   AND (ca.country = '해외') = :meIsOverseas
                   AND u.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :meId)
                   AND u.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :meId)
                   AND u.id NOT IN (
                       SELECT ma.target_id FROM match_actions ma
                        WHERE ma.actor_id = :meId
                          AND (
                              ma.action_type IN ('LIKE','SUPER_LIKE','POWER_MESSAGE')
                              OR (ma.action_type = 'PASS' AND ma.expires_at >= NOW())
                          )
                   )
                """)
                .setParameter("meId", me.id())
                .setParameter("myAge", me.age())
                .setParameter("ageMaxDiff", cfg.getAgeMaxDiff())
                .setParameter("since", since)
                .setParameter("meIsOverseas", meIsOverseas ? 1 : 0)
                .getResultList();

        return loadContexts(toLongIds(rows));
    }

    @Override
    public List<UserContext> findEligibleViewers(MatchingConfig cfg) {
        LocalDateTime since = LocalDateTime.now().minusDays(cfg.getLastActiveDays());

        @SuppressWarnings("unchecked")
        List<Number> rows = em.createNativeQuery("""
                SELECT u.id
                  FROM users u
                  JOIN user_profile up ON up.user_id = u.id
                 WHERE u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.last_active_at >= :since
                """)
                .setParameter("since", since)
                .getResultList();

        return loadContexts(toLongIds(rows));
    }

    @Override
    public UserContext loadContext(long userId) {
        List<UserContext> ctxs = loadContexts(List.of(userId));
        return ctxs.isEmpty() ? null : ctxs.get(0);
    }

    @Override
    public List<UserContext> findFailedViewersToday(MatchingConfig cfg) {
        LocalDateTime since = LocalDateTime.now().minusDays(cfg.getLastActiveDays());

        @SuppressWarnings("unchecked")
        List<Number> rows = em.createNativeQuery("""
                SELECT u.id
                  FROM users u
                  JOIN user_profile up ON up.user_id = u.id
                 WHERE u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.last_active_at >= :since
                   AND u.id NOT IN (
                       SELECT DISTINCT viewer_id FROM match_daily_feed
                        WHERE feed_date = CURDATE()
                   )
                """)
                .setParameter("since", since)
                .getResultList();

        return loadContexts(toLongIds(rows));
    }

    @Override
    public List<UserContext> topLikedYesterday(UserContext me, MatchingConfig cfg) {
        boolean meIsOverseas = !CandidateSelector.isDomestic(me);
        LocalDateTime yStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yEnd   = LocalDate.now().atStartOfDay();

        @SuppressWarnings("unchecked")
        List<Number> rows = em.createNativeQuery("""
                SELECT u.id
                  FROM users u
                  JOIN user_profile up ON up.user_id = u.id
                  JOIN code_area ca    ON ca.id = u.area_id
                  JOIN (
                      SELECT ma.target_id, COUNT(*) AS likes
                        FROM match_actions ma
                       WHERE ma.action_type IN ('LIKE','SUPER_LIKE')
                         AND ma.create_time >= :yStart
                         AND ma.create_time <  :yEnd
                       GROUP BY ma.target_id
                  ) liked ON liked.target_id = u.id
                 WHERE u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.id != :meId
                   AND ABS(u.age - :myAge) <= :ageMaxDiff
                   AND (ca.country = '해외') = :meIsOverseas
                 ORDER BY liked.likes DESC
                 LIMIT :limit
                """)
                .setParameter("meId", me.id())
                .setParameter("myAge", me.age())
                .setParameter("ageMaxDiff", cfg.getAgeMaxDiff())
                .setParameter("meIsOverseas", meIsOverseas ? 1 : 0)
                .setParameter("yStart", yStart)
                .setParameter("yEnd", yEnd)
                .setParameter("limit", cfg.getDailyShow())
                .getResultList();

        return loadContexts(toLongIds(rows));
    }

    @Override
    public List<UserContext> recentlyActive(UserContext me, MatchingConfig cfg) {
        boolean meIsOverseas = !CandidateSelector.isDomestic(me);

        @SuppressWarnings("unchecked")
        List<Number> rows = em.createNativeQuery("""
                SELECT u.id
                  FROM users u
                  JOIN user_profile up ON up.user_id = u.id
                  JOIN code_area ca    ON ca.id = u.area_id
                 WHERE u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.id != :meId
                   AND ABS(u.age - :myAge) <= :ageMaxDiff
                   AND (ca.country = '해외') = :meIsOverseas
                 ORDER BY u.last_active_at DESC
                 LIMIT :limit
                """)
                .setParameter("meId", me.id())
                .setParameter("myAge", me.age())
                .setParameter("ageMaxDiff", cfg.getAgeMaxDiff())
                .setParameter("meIsOverseas", meIsOverseas ? 1 : 0)
                .setParameter("limit", cfg.getDailyShow())
                .getResultList();

        return loadContexts(toLongIds(rows));
    }

    /**
     * 활성 viewer 전체 → Map<id, UserContext>.
     *  내부 = findEligibleViewers + List → Map. loadContexts 1회만.
     */
    @Override
    public Map<Long, UserContext> loadAllActiveContextsAsMap(MatchingConfig cfg) {
        List<UserContext> all = findEligibleViewers(cfg);
        Map<Long, UserContext> map = new HashMap<>(Math.max(16, all.size() * 2));
        for (UserContext ctx : all) {
            map.put(ctx.id(), ctx);
        }
        return map;
    }

    /**
     *  하드필터 + bbox 좌표 필터 적용 후 ID 만 반환
     *
     *  bbox: viewer 의 좌표 기준 radiusKm 반경의 위경도 사각형. code_area.(latitude, longitude) 의
     *        복합 인덱스가 있으면 인덱스 range scan. 해외 그룹은 좌표 필터 skip (그룹 크기 작음).
     */
    @Override
    public List<Long> findEligibleIds(UserContext me, MatchingConfig cfg, int radiusKm) {
        boolean meIsOverseas = !CandidateSelector.isDomestic(me);
        boolean bboxApplied  = !meIsOverseas && me.lat() != 0 && me.lon() != 0;
        LocalDateTime since  = LocalDateTime.now().minusDays(cfg.getLastActiveDays());

        // bbox 계산 — 위도 1도 ≈ 111km, 경도 1도 ≈ 111km × cos(latitude).
        // 국내 + 좌표 있는 viewer 만 bbox 적용. 해외/좌표 0 은 skip.
        StringBuilder bboxClause = new StringBuilder();
        if (bboxApplied) {
            bboxClause.append(" AND ca.latitude  BETWEEN :latMin  AND :latMax ");
            bboxClause.append(" AND ca.longitude BETWEEN :lonMin AND :lonMax ");
        }

        //  bbox 못 적용 (좌표 0 / 해외) 면 결과셋이 모든 호환 ACTIVE 후보로 폭주 가능.
        //   안전망으로 cap = poolSize × 4 의 LIMIT 부여. cap 안 후보가 충분히 있으므로 풀 500 구성에 무리 X.
        //   PK 순으로 잘리는 편향은 호출처 (CandidateSelector) 의 메모리 셔플로 흡수.
        String limitClause = bboxApplied ? "" : " LIMIT :fallbackCap ";

        String sql = """
                SELECT u.id
                  FROM users u
                  JOIN user_profile up ON up.user_id = u.id
                  JOIN code_area ca    ON ca.id = u.area_id
                 WHERE u.id != :meId
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.last_active_at >= :since
                   AND ABS(u.age - :myAge) <= :ageMaxDiff
                   AND (ca.country = '해외') = :meIsOverseas
                   AND u.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :meId)
                   AND u.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :meId)
                   AND u.id NOT IN (
                       SELECT ma.target_id FROM match_actions ma
                        WHERE ma.actor_id = :meId
                          AND (
                              ma.action_type IN ('LIKE','SUPER_LIKE','POWER_MESSAGE')
                              OR (ma.action_type = 'PASS' AND ma.expires_at >= NOW())
                          )
                   )
                """ + bboxClause + limitClause;

        Query q = em.createNativeQuery(sql)
                .setParameter("meId", me.id())
                .setParameter("myAge", me.age())
                .setParameter("ageMaxDiff", cfg.getAgeMaxDiff())
                .setParameter("since", since)
                .setParameter("meIsOverseas", meIsOverseas ? 1 : 0);
        if (bboxApplied) {
            double latDelta = radiusKm / 111.0;
            double lonDelta = radiusKm / (111.0 * Math.max(0.1, Math.cos(Math.toRadians(me.lat()))));
            q.setParameter("latMin", me.lat() - latDelta);
            q.setParameter("latMax", me.lat() + latDelta);
            q.setParameter("lonMin", me.lon() - lonDelta);
            q.setParameter("lonMax", me.lon() + lonDelta);
        } else {
            q.setParameter("fallbackCap", cfg.getPoolSize() * 4);
        }

        @SuppressWarnings("unchecked")
        List<Number> rows = q.getResultList();
        return toLongIds(rows);
    }

    @Override
    public List<Long> findCompatibleViewerIds(long targetUserId, MatchingConfig cfg) {
        @SuppressWarnings("unchecked")
        List<Number> rows = em.createNativeQuery("""
                SELECT v.id FROM users v
                  JOIN user_profile vp ON vp.user_id = v.id
                  JOIN code_area     vca ON vca.id = v.area_id
                  JOIN users         t   ON t.id = :target
                  JOIN code_area     tca ON tca.id = t.area_id
                 WHERE v.id != :target
                   AND v.status = 'ACTIVE'
                   AND vp.profile_status = 'APPROVED'
                   AND v.last_active_at >= NOW() - INTERVAL :lastActiveDays DAY
                   AND ABS(v.age - t.age) <= :ageMaxDiff
                   AND (vca.country = '해외') = (tca.country = '해외')
                   AND v.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :target)
                   AND v.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :target)
                   AND NOT EXISTS (
                       SELECT 1 FROM match_actions ma
                        WHERE ((ma.actor_id = v.id AND ma.target_id = :target)
                            OR (ma.actor_id = :target AND ma.target_id = v.id))
                          AND (
                              ma.action_type IN ('LIKE','SUPER_LIKE','POWER_MESSAGE')
                              OR (ma.action_type = 'PASS' AND ma.expires_at >= NOW())
                          )
                   )
                """)
                .setParameter("target", targetUserId)
                .setParameter("lastActiveDays", cfg.getLastActiveDays())
                .setParameter("ageMaxDiff", cfg.getAgeMaxDiff())
                .getResultList();
        return toLongIds(rows);
    }

    /* ─────────────────────── 변환 로직 ─────────────────────── */

    /**
     * 후보 ID 리스트 → UserContext 리스트.
     *  batch fetch 7회: users / user_profile / user_personal / user_keyword / user_custom_keyword
     *                   / code_area / code_keyword + code_personal
     */
    @Override
    public List<UserContext> loadContexts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        // ── 1. users / user_profile
        Map<Long, User> userMap = indexById(userRepo.findAllById(ids), User::getId);
        Map<Long, UserProfile> profileMap = indexBy(
                profileRepo.findByUserIdIn(ids), UserProfile::getUserId);

        // ── 2. user_personal (SELF + IDEAL), CodePersonal (라벨 → enum 매핑)
        List<UserPersonal> personals = userPersonalRepo.findByUserIdIn(ids);
        Set<Long> personalIds = personals.stream()
                .map(UserPersonal::getPersonalId).collect(Collectors.toSet());
        Map<Long, CodePersonal> codePersonalMap = indexById(
                codePersonalRepo.findAllById(personalIds), CodePersonal::getId);
        Map<Long, List<UserPersonal>> personalsByUser = groupBy(personals, UserPersonal::getUserId);

        // ── 3. user_keyword (관심사) + CodeKeyword (라벨)
        List<UserKeyword> keywords = userKeywordRepo.findByUserIdIn(ids);
        Set<Long> keywordIds = keywords.stream()
                .map(UserKeyword::getKeywordId).collect(Collectors.toSet());
        Map<Long, CodeKeyword> codeKeywordMap = indexById(
                codeKeywordRepo.findAllById(keywordIds), CodeKeyword::getId);
        Map<Long, List<UserKeyword>> keywordsByUser = groupBy(keywords, UserKeyword::getUserId);

        // ── 4. user_custom_keyword (개인 키워드)
        Map<Long, List<UserCustomKeyword>> customByUser = groupBy(
                userCustomKeywordRepo.findByUserIdIn(ids), UserCustomKeyword::getUserId);

        // ── 5. code_area (위경도)
        Set<Long> areaIds = userMap.values().stream()
                .map(User::getAreaId).filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, CodeArea> areaMap = indexById(areaRepo.findAllById(areaIds), CodeArea::getId);

        // ── 6. 조립
        List<UserContext> result = new ArrayList<>(ids.size());
        for (Long uid : ids) {
            User u = userMap.get(uid);
            UserProfile p = profileMap.get(uid);
            if (u == null || p == null) continue;  // 무결성 누락 — 스킵
            CodeArea area = areaMap.get(u.getAreaId());
            if (area == null) {
                log.warn("[UserManagement] code_area 누락 — userId={}, areaId={}", uid, u.getAreaId());
                continue;
            }
            UserContext ctx = toContext(u, p, area,
                    personalsByUser.getOrDefault(uid, List.of()), codePersonalMap,
                    keywordsByUser.getOrDefault(uid, List.of()), codeKeywordMap,
                    customByUser.getOrDefault(uid, List.of()));
            if (ctx != null) result.add(ctx);
        }
        return result;
    }

    private UserContext toContext(User u, UserProfile profile, CodeArea area,
                                  List<UserPersonal> personals, Map<Long, CodePersonal> codePersonalMap,
                                  List<UserKeyword> keywords, Map<Long, CodeKeyword> codeKeywordMap,
                                  List<UserCustomKeyword> customs) {

        LocalDate signupAt = u.getCreateTime() == null
                ? LocalDate.now() : u.getCreateTime().toLocalDate();

        /* Purpose 직접 사용 — 매칭 도메인 enum 분리 폐기 */
        Purpose purpose =
                profile.getPurpose() == null
                        ? Purpose.MIXED
                        : profile.getPurpose();

        /* 키워드 + 카테고리 그룹 */
        Set<String> keywordNames = new HashSet<>();
        Map<String, Set<String>> byCategory = new HashMap<>();
        for (UserKeyword uk : keywords) {
            CodeKeyword ck = codeKeywordMap.get(uk.getKeywordId());
            if (ck == null) continue;
            keywordNames.add(ck.getSmallCategory());
            byCategory.computeIfAbsent(ck.getBigCategory(), k -> new HashSet<>())
                    .add(ck.getSmallCategory());
        }
        /* immutable view */
        Map<String, Set<String>> keywordsByCategory = byCategory.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey, e -> Set.copyOf(e.getValue())));

        /* 개인 키워드 */
        Set<String> customKeywords = customs.stream()
                .map(UserCustomKeyword::getKeyword)
                .collect(Collectors.toUnmodifiableSet());

        /* Self / Ideal */
        Self self = buildSelf(personals, codePersonalMap);
        Ideal ideal = buildIdeal(personals, codePersonalMap);

        /* Drinking / Smoking — SELF 의 personal row 에서 추출 */
        Drinking drinking = firstSelfEnum(personals, codePersonalMap,
                PersonalLabelMapper.BIG_DRINKING, PersonalLabelMapper::toDrinking, Drinking.NEVER);
        Smoking smoking = firstSelfEnum(personals, codePersonalMap,
                PersonalLabelMapper.BIG_SMOKING, PersonalLabelMapper::toSmoking, Smoking.NEVER);

        /* IdealPointType 직접 사용 — UserProfile.idealPointTypes */
        Set<IdealPointType> importantPoints = profile.getIdealPointTypes() == null
                ? Set.of()
                : Set.copyOf(profile.getIdealPointTypes());

        /* country / lat / lon */
        double lat = area.getLatitude() == null ? 0.0 : area.getLatitude().doubleValue();
        double lon = area.getLongitude() == null ? 0.0 : area.getLongitude().doubleValue();

        return new UserContext(
                u.getId(),
                u.getAge() == null ? 0 : u.getAge(),
                signupAt,
                area.getCountry(),
                lat, lon,
                purpose,
                Set.copyOf(keywordNames),
                customKeywords,
                keywordsByCategory,
                ideal,
                self,
                drinking,
                smoking,
                importantPoints
        );
    }

    /* ─── Self / Ideal 빌더 ─── */

    private Self buildSelf(List<UserPersonal> personals, Map<Long, CodePersonal> cpMap) {
        List<CodePersonal> selfRows = filterRows(personals, UserPersonalType.SELF, cpMap);
        HairLength hair = pickFirst(selfRows, PersonalLabelMapper::toHair);
        BodyType body   = pickFirst(selfRows, PersonalLabelMapper::toBody);
        HeightBand h    = pickFirst(selfRows, PersonalLabelMapper::toHeight);
        Tendency tend   = pickFirst(selfRows, PersonalLabelMapper::toTendency);
        Set<Fashion> fashions = selfRows.stream()
                .map(PersonalLabelMapper::toFashion).filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Fashion.class)));
        Grooming groom  = pickFirst(selfRows, PersonalLabelMapper::toGrooming);
        return new Self(hair, body, h, tend, Collections.unmodifiableSet(fashions), groom);
    }

    private Ideal buildIdeal(List<UserPersonal> personals, Map<Long, CodePersonal> cpMap) {
        List<CodePersonal> idealRows = filterRows(personals, UserPersonalType.IDEAL, cpMap);
        HairLength hair = pickFirst(idealRows, PersonalLabelMapper::toHair);
        BodyType body   = pickFirst(idealRows, PersonalLabelMapper::toBody);
        HeightBand h    = pickFirst(idealRows, PersonalLabelMapper::toHeight);
        Tendency tend   = pickFirst(idealRows, PersonalLabelMapper::toTendency);
        Set<Fashion> fashions = idealRows.stream()
                .map(PersonalLabelMapper::toFashion).filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Fashion.class)));
        Grooming groom  = pickFirst(idealRows, PersonalLabelMapper::toGrooming);
        return new Ideal(hair, body, h, tend, Collections.unmodifiableSet(fashions), groom);
    }

    private List<CodePersonal> filterRows(List<UserPersonal> personals, UserPersonalType type,
                                          Map<Long, CodePersonal> cpMap) {
        return personals.stream()
                .filter(p -> p.getType() == type)
                .map(p -> cpMap.get(p.getPersonalId()))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private <E extends Enum<E>> E pickFirst(List<CodePersonal> rows,
                                            java.util.function.Function<CodePersonal, E> mapper) {
        return rows.stream().map(mapper).filter(java.util.Objects::nonNull).findFirst().orElse(null);
    }

    /** SELF row 중 해당 카테고리(첫 매칭) → enum. 없으면 fallback. */
    private <E extends Enum<E>> E firstSelfEnum(List<UserPersonal> personals,
                                                Map<Long, CodePersonal> cpMap,
                                                String bigCategory,
                                                java.util.function.Function<CodePersonal, E> mapper,
                                                E fallback) {
        for (UserPersonal up : personals) {
            if (up.getType() != UserPersonalType.SELF) continue;
            CodePersonal cp = cpMap.get(up.getPersonalId());
            if (cp == null || !bigCategory.equals(cp.getBigCategory())) continue;
            E v = mapper.apply(cp);
            if (v != null) return v;
        }
        return fallback;
    }

    /* ─── 컬렉션 헬퍼 ─── */

    private static List<Long> toLongIds(List<Number> rows) {
        if (rows == null || rows.isEmpty()) return List.of();
        return rows.stream().map(Number::longValue).toList();
    }

    private static <T, K> Map<K, T> indexById(Iterable<T> items, java.util.function.Function<T, K> key) {
        Map<K, T> map = new HashMap<>();
        for (T item : items) map.put(key.apply(item), item);
        return map;
    }

    private static <T, K> Map<K, T> indexBy(Collection<T> items, java.util.function.Function<T, K> key) {
        Map<K, T> map = new HashMap<>(items.size() * 2);
        for (T item : items) map.put(key.apply(item), item);
        return map;
    }

    private static <T, K> Map<K, List<T>> groupBy(Collection<T> items, java.util.function.Function<T, K> key) {
        return items.stream().collect(Collectors.groupingBy(key));
    }
}
