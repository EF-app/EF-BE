package com.nokcha.efbe.domain.match.repository;

import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.model.MatchActionType;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.fixture.UserContextBuilder;
import com.nokcha.efbe.domain.profile.entity.ProfileStatus;
import com.nokcha.efbe.domain.profile.entity.Purpose;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import com.nokcha.efbe.support.IntegrationTest;
import com.nokcha.efbe.support.MatchTestSeed;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * §9.2 후보·슬롯 — UserManagementImpl 의 native SQL 4 메서드 통합 검증.
 *  대상:
 *    findEligible — 하드 필터 (status / profile_status / last_active / age / 국내·해외 그룹 / block / match_actions)
 *    findEligibleViewers — 활성+승인+최근접속만
 *
 *  ※ ColdStartFeed 의 topLikedYesterday / recentlyActive 는 콜드스타트 E2E 작업에서 별도 검증.
 */
@IntegrationTest
class UserManagementImplIntegrationTest {

    @Autowired EntityManager em;
    @Autowired UserManagementImpl userMgmt;
    @Autowired AreaRepository areaRepository;

    private MatchTestSeed seed;
    private MatchingConfig cfg;

    /** 운영 시드의 마스터 area 가져오기 (CodeAreaDataInitializer 가 INSERT 한 row). */
    private CodeArea areaSeoul()   { return areaRepository.findByCountryAndCity("서울특별시", "선택안함").orElseThrow(); }
    private CodeArea areaTokyo()   { return areaRepository.findByCountryAndCity("해외", "일본").orElseThrow(); }

    @BeforeEach
    void setUp() {
        seed = new MatchTestSeed(em);
        cfg = new MatchingConfig();   // 기본값: age_max_diff=8, last_active_days=31, pass_cooldown_days=30
    }

    /* ─────────── §9.2.1 하드 필터 ─────────── */

    @Nested
    @DisplayName("§9.2.1 findEligible — 하드 필터")
    class HardFilter {

        @Test
        @DisplayName("status≠ACTIVE / profile_status≠APPROVED / last_active 31일 초과 / age 차 9 → 모두 제외")
        void excludesByHardFilter() {
            CodeArea area = areaSeoul();

            User me = seed.activeUser("me", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(me.getId(), Purpose.MIXED);

            // 정상 후보
            User ok = seed.activeUser("ok", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(ok.getId(), Purpose.MIXED);

            // 비활성 status
            User suspended = seed.user("susp", 27, area.getId(),
                    UserStatus.TEMPORARY, LocalDateTime.now());
            seed.approvedProfile(suspended.getId(), Purpose.MIXED);

            // profile 미승인 (REJECTED)
            User rejected = seed.activeUser("rej", 27, area.getId(), LocalDateTime.now());
            seed.profile(rejected.getId(), Purpose.MIXED, ProfileStatus.REJECTED);

            // 비활동 (32일 전)
            User dormant = seed.activeUser("dorm", 27, area.getId(),
                    LocalDateTime.now().minusDays(32));
            seed.approvedProfile(dormant.getId(), Purpose.MIXED);

            // age 차 9 초과 (27±8 = 19~35) → age 37 제외
            User tooOld = seed.activeUser("old", 37, area.getId(), LocalDateTime.now());
            seed.approvedProfile(tooOld.getId(), Purpose.MIXED);

            seed.flush();

            UserContext meCtx = ctxOf(me, area, "한국");
            List<UserContext> eligible = userMgmt.findEligible(meCtx, cfg);

            assertTrue(containsId(eligible, ok.getId()),  "정상 후보는 포함");
            assertFalse(containsId(eligible, suspended.getId()), "비활성 status 제외");
            assertFalse(containsId(eligible, rejected.getId()),  "profile 미승인 제외");
            assertFalse(containsId(eligible, dormant.getId()),   "비활동 제외");
            assertFalse(containsId(eligible, tooOld.getId()),    "age 차 초과 제외");
            assertFalse(containsId(eligible, me.getId()),        "자기 자신 제외");
        }

        @Test
        @DisplayName("age 경계 — ±8 정확히 (35 포함, 36 제외)")
        void ageBoundary() {
            CodeArea area = areaSeoul();

            User me = seed.activeUser("me", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(me.getId(), Purpose.MIXED);

            User edge35 = seed.activeUser("e35", 35, area.getId(), LocalDateTime.now());
            seed.approvedProfile(edge35.getId(), Purpose.MIXED);

            User edge36 = seed.activeUser("e36", 36, area.getId(), LocalDateTime.now());
            seed.approvedProfile(edge36.getId(), Purpose.MIXED);

            seed.flush();

            UserContext meCtx = ctxOf(me, area, "한국");
            List<UserContext> eligible = userMgmt.findEligible(meCtx, cfg);

            assertTrue(containsId(eligible, edge35.getId()), "age 35 (차 8) 포함");
            assertFalse(containsId(eligible, edge36.getId()), "age 36 (차 9) 제외");
        }
    }

    /* ─────────── §9.2.2 국내·해외 그룹 분리 ─────────── */

    @Nested
    @DisplayName("§9.2.2 국내·해외 그룹 분리")
    class CountryGroup {

        @Test
        @DisplayName("국내 me → 후보에 country='해외' 안 섞임")
        void domesticMeExcludesOverseas() {
            CodeArea seoul = areaSeoul();
            CodeArea tokyo = areaTokyo();

            User me = seed.activeUser("me", 27, seoul.getId(), LocalDateTime.now());
            seed.approvedProfile(me.getId(), Purpose.MIXED);

            User domestic = seed.activeUser("dom", 27, seoul.getId(), LocalDateTime.now());
            seed.approvedProfile(domestic.getId(), Purpose.MIXED);

            User overseas = seed.activeUser("ovr", 27, tokyo.getId(), LocalDateTime.now());
            seed.approvedProfile(overseas.getId(), Purpose.MIXED);

            seed.flush();

            UserContext meCtx = ctxOf(me, seoul, "한국");
            List<UserContext> eligible = userMgmt.findEligible(meCtx, cfg);

            assertTrue(containsId(eligible, domestic.getId()));
            assertFalse(containsId(eligible, overseas.getId()), "국내 me 는 해외 후보 안 봐야");
        }

        @Test
        @DisplayName("해외 me → 후보에 국내 안 섞임 (역대칭)")
        void overseasMeExcludesDomestic() {
            CodeArea seoul = areaSeoul();
            CodeArea tokyo = areaTokyo();

            User me = seed.activeUser("me", 27, tokyo.getId(), LocalDateTime.now());
            seed.approvedProfile(me.getId(), Purpose.MIXED);

            User domestic = seed.activeUser("dom", 27, seoul.getId(), LocalDateTime.now());
            seed.approvedProfile(domestic.getId(), Purpose.MIXED);

            User overseas = seed.activeUser("ovr", 27, tokyo.getId(), LocalDateTime.now());
            seed.approvedProfile(overseas.getId(), Purpose.MIXED);

            seed.flush();

            UserContext meCtx = ctxOf(me, tokyo, "해외");
            List<UserContext> eligible = userMgmt.findEligible(meCtx, cfg);

            assertTrue(containsId(eligible, overseas.getId()));
            assertFalse(containsId(eligible, domestic.getId()), "해외 me 는 국내 후보 안 봐야");
        }
    }

    /* ─────────── §9.2.3 차단 양방향 + match_actions 통합 필터 ─────────── */

    @Nested
    @DisplayName("§9.2.3 차단·match_actions 통합 필터")
    class BlockAndActionFilter {

        @Test
        @DisplayName("block 양방향 — blocker=me 또는 blocked=me 둘 다 제외")
        void blockBothDirections() {
            CodeArea area = areaSeoul();

            User me = seed.activeUser("me", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(me.getId(), Purpose.MIXED);

            User iBlocked = seed.activeUser("a", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(iBlocked.getId(), Purpose.MIXED);

            User blockedMe = seed.activeUser("b", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(blockedMe.getId(), Purpose.MIXED);

            seed.block(me, iBlocked);       // me → a 차단
            seed.block(blockedMe, me);       // b → me 차단
            seed.flush();

            UserContext meCtx = ctxOf(me, area, "한국");
            List<UserContext> eligible = userMgmt.findEligible(meCtx, cfg);

            assertFalse(containsId(eligible, iBlocked.getId()),  "me 가 차단한 유저 제외");
            assertFalse(containsId(eligible, blockedMe.getId()), "me 를 차단한 유저 제외");
        }

        @Test
        @DisplayName("match_actions 통합 필터 — LIKE/SUPER_LIKE/POWER_MESSAGE 영구 제외, PASS 미만료 제외")
        void matchActionsUnifiedFilter() {
            CodeArea area = areaSeoul();

            User me = seed.activeUser("me", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(me.getId(), Purpose.MIXED);

            User liked = seed.activeUser("l", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(liked.getId(), Purpose.MIXED);

            User superLiked = seed.activeUser("sl", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(superLiked.getId(), Purpose.MIXED);

            User powerMsged = seed.activeUser("pm", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(powerMsged.getId(), Purpose.MIXED);

            User passedActive = seed.activeUser("pa", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(passedActive.getId(), Purpose.MIXED);

            User passedExpired = seed.activeUser("pe", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(passedExpired.getId(), Purpose.MIXED);

            User normal = seed.activeUser("n", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(normal.getId(), Purpose.MIXED);

            // me 의 액션 4 종 (영구 3 + PASS 2)
            seed.action(me.getId(), liked.getId(),       MatchActionType.LIKE,          null);
            seed.action(me.getId(), superLiked.getId(),  MatchActionType.SUPER_LIKE,    null);
            seed.action(me.getId(), powerMsged.getId(),  MatchActionType.POWER_MESSAGE, null);
            seed.action(me.getId(), passedActive.getId(),  MatchActionType.PASS,
                    LocalDateTime.now().plusDays(15));   // 만료 전
            seed.action(me.getId(), passedExpired.getId(), MatchActionType.PASS,
                    LocalDateTime.now().minusDays(1));   // 만료됨
            seed.flush();

            UserContext meCtx = ctxOf(me, area, "한국");
            List<UserContext> eligible = userMgmt.findEligible(meCtx, cfg);

            assertFalse(containsId(eligible, liked.getId()),         "LIKE 영구 제외");
            assertFalse(containsId(eligible, superLiked.getId()),    "SUPER_LIKE 영구 제외");
            assertFalse(containsId(eligible, powerMsged.getId()),    "POWER_MESSAGE 영구 제외");
            assertFalse(containsId(eligible, passedActive.getId()),  "PASS 만료 전 제외");
            assertTrue(containsId(eligible, passedExpired.getId()),  "PASS 만료 후 다시 포함");
            assertTrue(containsId(eligible, normal.getId()),         "액션 없는 유저는 포함");
        }
    }

    /* ─────────── §9.2 findEligibleViewers ─────────── */

    @Nested
    @DisplayName("findEligibleViewers — 활성+승인+최근접속만")
    class EligibleViewers {

        @Test
        @DisplayName("활성+승인+최근접속만 포함")
        void onlyActiveApprovedAndRecent() {
            CodeArea area = areaSeoul();

            User ok = seed.activeUser("ok", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(ok.getId(), Purpose.MIXED);

            User suspended = seed.user("susp", 27, area.getId(),
                    UserStatus.TEMPORARY, LocalDateTime.now());
            seed.approvedProfile(suspended.getId(), Purpose.MIXED);

            User dormant = seed.activeUser("dorm", 27, area.getId(),
                    LocalDateTime.now().minusDays(40));
            seed.approvedProfile(dormant.getId(), Purpose.MIXED);

            User rejected = seed.activeUser("rej", 27, area.getId(), LocalDateTime.now());
            seed.profile(rejected.getId(), Purpose.MIXED, ProfileStatus.REJECTED);

            seed.flush();

            List<UserContext> viewers = userMgmt.findEligibleViewers(cfg);

            assertTrue(containsId(viewers, ok.getId()));
            assertFalse(containsId(viewers, suspended.getId()));
            assertFalse(containsId(viewers, dormant.getId()));
            assertFalse(containsId(viewers, rejected.getId()));
        }
    }

    /* ─────────── 헬퍼 ─────────── */

    private static boolean containsId(List<UserContext> list, long id) {
        return list.stream().anyMatch(u -> u.id() == id);
    }

    /**
     * findEligible 의 me 파라미터용 UserContext 직접 생성.
     *  실제 운영에선 loadContext 로 만들지만, 통합 테스트는 findEligible 만 검증하므로
     *  필요한 4 필드 (id, age, regionCountry, lat/lon) 만 채움.
     */
    private static UserContext ctxOf(User u, CodeArea area, String country) {
        return UserContextBuilder.builder()
                .id(u.getId())
                .age(u.getAge())
                .regionCountry(country)
                .coord(area.getLatitude().doubleValue(), area.getLongitude().doubleValue())
                .keywords(Set.of())
                .customKeywords(Set.of())
                .build();
    }
}
