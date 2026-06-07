package com.nokcha.efbe.domain.match.pool;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.fixture.UserContextBuilder;
import com.nokcha.efbe.domain.match.repository.UserManagement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * §9.2 체크리스트 (후보·슬롯):
 *  - 두 양동이 (뉴비/베테랑) 비율
 *  - 반경 20→50→100→전국(-1) 확장
 *  - 백필로 총 poolSize 유지
 */
class CandidateSelectorTest {

    @Test
    @DisplayName("뉴비 비율 0.4 - poolSize 100 → 뉴비 40 / 베테랑 60")
    void twoBucketsRatio() {
        MatchingConfig cfg = new MatchingConfig();
        cfg.setPoolSize(100);
        cfg.setNewbieRatio(0.40);
        cfg.setNewbieWindowDays(7);
        cfg.setRadiusStepsKm(new int[]{-1});  // 전국 (반경 영향 제거)

        // 뉴비 100 명 + 베테랑 100 명 → 정확히 40 / 60 채워야
        List<UserContext> eligible = new ArrayList<>();
        for (int i = 0; i < 100; i++) eligible.add(newbieAt(i, 0.0, 0.0));
        for (int i = 100; i < 200; i++) eligible.add(veteranAt(i, 0.0, 0.0));

        UserManagement stub = stubReturning(eligible);
        UserContext me = UserContextBuilder.builder().id(999).coord(0.0, 0.0).build();

        List<UserContext> pool = new CandidateSelector(stub).buildPool(me, cfg);

        long newbieCount = pool.stream().filter(u -> isNewbie(u, cfg)).count();
        long veteranCount = pool.size() - newbieCount;
        assertEquals(100, pool.size());
        assertEquals(40, newbieCount);
        assertEquals(60, veteranCount);
    }

    @Test
    @DisplayName("반경 확장 — 20km 안에 4명만, 100km 안에 더 있으면 확장해서 채움")
    void radiusExpansion() {
        MatchingConfig cfg = new MatchingConfig();
        cfg.setPoolSize(10);
        cfg.setNewbieRatio(0.0);  // 베테랑만
        cfg.setNewbieWindowDays(7);
        cfg.setRadiusStepsKm(new int[]{20, 50, 100, -1});

        List<UserContext> eligible = new ArrayList<>();
        // 20km 안: 4명 (위도 +0.05 = 약 5.5km)
        for (int i = 0; i < 4; i++) eligible.add(veteranAt(i, 37.55, 127.0));
        // 50km 안: 6명 추가 (위도 +0.4 = 약 44km)
        for (int i = 4; i < 10; i++) eligible.add(veteranAt(i, 37.9, 127.0));

        UserManagement stub = stubReturning(eligible);
        UserContext me = UserContextBuilder.builder().id(999).coord(37.5, 127.0).build();

        List<UserContext> pool = new CandidateSelector(stub).buildPool(me, cfg);

        assertEquals(10, pool.size(), "반경 확장으로 전부 채워야");
    }

    @Test
    @DisplayName("백필 — 뉴비 부족하면 베테랑으로 채워 poolSize 유지")
    void backfillWhenNewbieShort() {
        MatchingConfig cfg = new MatchingConfig();
        cfg.setPoolSize(50);
        cfg.setNewbieRatio(0.40);  // 20 명 뉴비 목표
        cfg.setNewbieWindowDays(7);
        cfg.setRadiusStepsKm(new int[]{-1});

        // 뉴비 5명만 (목표 20 미달) + 베테랑 100 명 → 백필로 총 50 채워야
        List<UserContext> eligible = new ArrayList<>();
        for (int i = 0; i < 5; i++) eligible.add(newbieAt(i, 0.0, 0.0));
        for (int i = 5; i < 105; i++) eligible.add(veteranAt(i, 0.0, 0.0));

        UserManagement stub = stubReturning(eligible);
        UserContext me = UserContextBuilder.builder().id(999).coord(0.0, 0.0).build();

        List<UserContext> pool = new CandidateSelector(stub).buildPool(me, cfg);

        assertEquals(50, pool.size(), "백필로 poolSize 유지");
        long newbieCount = pool.stream().filter(u -> isNewbie(u, cfg)).count();
        assertTrue(newbieCount <= 5, "원래 뉴비 풀(5)을 초과할 수 없음");
    }

    @Test
    @DisplayName("자격 풀이 poolSize 미만이면 가능한 만큼만 — 부족해도 에러 X")
    void shortEligibleShortPool() {
        MatchingConfig cfg = new MatchingConfig();
        cfg.setPoolSize(50);
        cfg.setNewbieRatio(0.40);
        cfg.setNewbieWindowDays(7);
        cfg.setRadiusStepsKm(new int[]{-1});

        List<UserContext> eligible = new ArrayList<>();
        for (int i = 0; i < 10; i++) eligible.add(veteranAt(i, 0.0, 0.0));

        UserManagement stub = stubReturning(eligible);
        UserContext me = UserContextBuilder.builder().id(999).coord(0.0, 0.0).build();

        List<UserContext> pool = new CandidateSelector(stub).buildPool(me, cfg);

        assertEquals(10, pool.size(), "자격 풀 전체 반환");
    }

    /* ─── 헬퍼 ─── */

    private static UserContext newbieAt(long id, double lat, double lon) {
        return UserContextBuilder.builder().id(id).coord(lat, lon)
                .signupAt(LocalDate.now().minusDays(1)).build();   // 가입 1일차 = 뉴비
    }

    private static UserContext veteranAt(long id, double lat, double lon) {
        return UserContextBuilder.builder().id(id).coord(lat, lon)
                .signupAt(LocalDate.now().minusDays(60)).build();
    }

    private static boolean isNewbie(UserContext u, MatchingConfig cfg) {
        return java.time.temporal.ChronoUnit.DAYS.between(u.signupAt(), LocalDate.now())
                < cfg.getNewbieWindowDays();
    }

    private static UserManagement stubReturning(List<UserContext> eligible) {
        return new UserManagement() {
            @Override public List<UserContext> findEligible(UserContext me, MatchingConfig cfg) { return eligible; }
            @Override public List<UserContext> findEligibleViewers(MatchingConfig cfg)         { return List.of(); }
            @Override public UserContext loadContext(long userId)                              { return null; }
            @Override public List<UserContext> loadContexts(List<Long> userIds)                { return List.of(); }
            @Override public List<UserContext> topLikedYesterday(UserContext me, MatchingConfig cfg) { return List.of(); }
            @Override public List<UserContext> recentlyActive(UserContext me, MatchingConfig cfg)   { return List.of(); }
            @Override public List<Long> findCompatibleViewerIds(long targetUserId, int cap, MatchingConfig cfg) { return List.of(); }
            @Override public List<UserContext> findFailedViewersToday(MatchingConfig cfg) { return List.of(); }
        };
    }
}
