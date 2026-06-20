package com.nokcha.efbe.domain.match.repository;

import com.nokcha.efbe.domain.match.repository.projection.FeedView;

import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.match.model.DailyFeedRow;
import com.nokcha.efbe.domain.match.model.MatchActionType;
import com.nokcha.efbe.domain.profile.entity.ProfileStatus;
import com.nokcha.efbe.domain.profile.entity.Purpose;
import com.nokcha.efbe.domain.profile.entity.UserProfile;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 1 read-time 오버레이 검증.
 *  findVisibleTodayFeed:
 *    - target 의 현재 status≠ACTIVE / profile_status≠APPROVED → 응답 제외
 *    - block 양방향 → 응답 제외
 *    - rank 오름차순 보존
 */
@IntegrationTest
class MatchDailyFeedQueryRepositoryIntegrationTest {

    @Autowired EntityManager em;
    @Autowired MatchDailyFeedQueryRepository dailyFeedQuery;
    @Autowired AreaRepository areaRepository;

    private MatchTestSeed seed;
    private CodeArea area;
    private User viewer;
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        seed = new MatchTestSeed(em);
        area = areaRepository.findByCountryAndCity("서울특별시", "선택안함").orElseThrow();

        viewer = seed.activeUser("viewer", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(viewer.getId(), Purpose.MIXED);
        seed.flush();
    }

    @Nested
    @DisplayName("read-time 오버레이 — target 상태 변화 즉시 반영")
    class TargetStatusOverlay {

        @Test
        @DisplayName("target.status≠ACTIVE → 응답 제외 (배치 안 기다림)")
        void targetSuspendedExcluded() {
            User active = seed.activeUser("a", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(active.getId(), Purpose.MIXED);
            User suspended = seed.user("s", 27, area.getId(),
                    UserStatus.TEMPORARY, LocalDateTime.now());
            seed.approvedProfile(suspended.getId(), Purpose.MIXED);
            User withdrawing = seed.user("w", 27, area.getId(),
                    UserStatus.WITHDRAWING, LocalDateTime.now());
            seed.approvedProfile(withdrawing.getId(), Purpose.MIXED);
            seed.flush();

            saveFeed(viewer.getId(), List.of(
                    row(1, active.getId(),      "SCORE"),
                    row(2, suspended.getId(),   "SCORE"),
                    row(3, withdrawing.getId(), "SCORE")
            ));

            List<FeedView> result =
                    dailyFeedQuery.findCurrentFeed(viewer.getId());

            assertEquals(1, result.size(), "ACTIVE 만 응답: " + result);
            assertEquals(active.getId(), result.get(0).targetId());
        }

        @Test
        @DisplayName("target profile_status≠APPROVED → 응답 제외")
        void targetProfileNotApprovedExcluded() {
            User approved = seed.activeUser("ap", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(approved.getId(), Purpose.MIXED);
            User rejected = seed.activeUser("rj", 27, area.getId(), LocalDateTime.now());
            seed.profile(rejected.getId(), Purpose.MIXED, ProfileStatus.REJECTED);
            seed.flush();

            saveFeed(viewer.getId(), List.of(
                    row(1, approved.getId(), "SCORE"),
                    row(2, rejected.getId(), "SCORE")
            ));

            List<FeedView> result =
                    dailyFeedQuery.findCurrentFeed(viewer.getId());

            assertEquals(1, result.size());
            assertEquals(approved.getId(), result.get(0).targetId());
        }
    }

    @Nested
    @DisplayName("read-time 오버레이 — 차단 양방향 즉시 반영")
    class BlockOverlay {

        @Test
        @DisplayName("viewer 가 target 차단 → 응답 제외")
        void viewerBlocksTarget() {
            User normal = seed.activeUser("n", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(normal.getId(), Purpose.MIXED);
            User blocked = seed.activeUser("b", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(blocked.getId(), Purpose.MIXED);
            seed.block(viewer, blocked);
            seed.flush();

            saveFeed(viewer.getId(), List.of(
                    row(1, normal.getId(),  "SCORE"),
                    row(2, blocked.getId(), "SCORE")
            ));

            List<FeedView> result =
                    dailyFeedQuery.findCurrentFeed(viewer.getId());

            assertEquals(1, result.size());
            assertFalse(result.stream().anyMatch(r -> r.targetId() == blocked.getId()));
        }

        @Test
        @DisplayName("target 이 viewer 차단 → 응답 제외 (역방향도)")
        void targetBlocksViewer() {
            User normal = seed.activeUser("n", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(normal.getId(), Purpose.MIXED);
            User blocker = seed.activeUser("bk", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(blocker.getId(), Purpose.MIXED);
            seed.block(blocker, viewer);  // 역방향
            seed.flush();

            saveFeed(viewer.getId(), List.of(
                    row(1, normal.getId(),  "SCORE"),
                    row(2, blocker.getId(), "SCORE")
            ));

            List<FeedView> result =
                    dailyFeedQuery.findCurrentFeed(viewer.getId());

            assertEquals(1, result.size());
            assertFalse(result.stream().anyMatch(r -> r.targetId() == blocker.getId()));
        }
    }

    @Nested
    @DisplayName("read-time 오버레이 — 본인이 액션한 카드 자동 제외")
    class ActionOverlay {

        @Test
        @DisplayName("viewer 가 LIKE 한 target → 응답 제외")
        void viewerAlreadyLikedExcluded() {
            User normal = seed.activeUser("n", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(normal.getId(), Purpose.MIXED);
            User liked = seed.activeUser("l", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(liked.getId(), Purpose.MIXED);
            seed.action(viewer.getId(), liked.getId(),
                    MatchActionType.LIKE, null);
            seed.flush();

            saveFeed(viewer.getId(), List.of(
                    row(1, normal.getId(), "SCORE"),
                    row(2, liked.getId(),  "SCORE")
            ));

            List<FeedView> result =
                    dailyFeedQuery.findCurrentFeed(viewer.getId());

            assertEquals(1, result.size());
            assertFalse(result.stream().anyMatch(r -> r.targetId() == liked.getId()),
                    "LIKE 한 카드는 자동 제외");
        }

        @Test
        @DisplayName("PASS 미만료는 제외, 만료된 PASS 는 다시 노출")
        void passExpirationHandled() {
            User passActive = seed.activeUser("pa", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(passActive.getId(), Purpose.MIXED);
            User passExpired = seed.activeUser("pe", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(passExpired.getId(), Purpose.MIXED);
            seed.action(viewer.getId(), passActive.getId(),
                    MatchActionType.PASS,
                    LocalDateTime.now().plusDays(10));
            seed.action(viewer.getId(), passExpired.getId(),
                    MatchActionType.PASS,
                    LocalDateTime.now().minusDays(1));
            seed.flush();

            saveFeed(viewer.getId(), List.of(
                    row(1, passActive.getId(),  "SCORE"),
                    row(2, passExpired.getId(), "SCORE")
            ));

            List<FeedView> result =
                    dailyFeedQuery.findCurrentFeed(viewer.getId());

            assertEquals(1, result.size());
            assertEquals(passExpired.getId(), result.get(0).targetId(),
                    "PASS 만료된 카드는 다시 노출");
        }
    }

    @Nested
    @DisplayName("rank 보존 / 빈 결과")
    class OrderingAndEmpty {

        @Test
        @DisplayName("rank 오름차순 정렬")
        void rankOrderingPreserved() {
            User u1 = seed.activeUser("u1", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(u1.getId(), Purpose.MIXED);
            User u2 = seed.activeUser("u2", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(u2.getId(), Purpose.MIXED);
            User u3 = seed.activeUser("u3", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(u3.getId(), Purpose.MIXED);
            seed.flush();

            // 일부러 등록은 뒤섞임
            saveFeed(viewer.getId(), List.of(
                    row(3, u3.getId(), "SCORE"),
                    row(1, u1.getId(), "SCORE"),
                    row(2, u2.getId(), "SCORE")
            ));

            List<FeedView> result =
                    dailyFeedQuery.findCurrentFeed(viewer.getId());

            assertEquals(3, result.size());
            assertEquals(1, result.get(0).matchRank());
            assertEquals(2, result.get(1).matchRank());
            assertEquals(3, result.get(2).matchRank());
        }

        @Test
        @DisplayName("오늘 피드 없음 → 빈 리스트")
        void emptyWhenNoFeed() {
            List<FeedView> result =
                    dailyFeedQuery.findCurrentFeed(viewer.getId());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Phase 5 — 카드 표시 데이터 join")
    class CardDisplay {

        @Test
        @DisplayName("닉네임/나이/지역/대표사진/bioMessage 까지 한 번에 채워짐")
        void joinedDisplayFields() {
            User target = seed.activeUser("disp", 29, area.getId(), LocalDateTime.now());
            UserProfile profile = seed.approvedProfile(target.getId(), Purpose.LOVE);
            profile.updateBio("안녕하세요 :)");
            seed.profileImage(target.getId(), "https://cdn.example.com/p1.jpg", 0);
            seed.profileImage(target.getId(), "https://cdn.example.com/p2.jpg", 1);
            seed.flush();

            saveFeed(viewer.getId(), List.of(row(1, target.getId(), "SCORE")));

            List<FeedView> result =
                    dailyFeedQuery.findCurrentFeed(viewer.getId());

            assertEquals(1, result.size());
            FeedView v = result.get(0);
            assertEquals("닉_disp", v.nickname());
            assertEquals(29, v.age());
            assertEquals("서울특별시", v.country());
            assertEquals("선택안함", v.city());
            assertEquals("https://cdn.example.com/p1.jpg", v.mainPhotoUrl(),
                    "sort_order 가장 낮은(0번) 사진이 대표");
            assertEquals("안녕하세요 :)", v.bioMessage());
        }

        @Test
        @DisplayName("사진 없으면 mainPhotoUrl null")
        void noPhotoNullable() {
            User target = seed.activeUser("nopic", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(target.getId(), Purpose.MIXED);
            seed.flush();

            saveFeed(viewer.getId(), List.of(row(1, target.getId(), "SCORE")));

            List<FeedView> result =
                    dailyFeedQuery.findCurrentFeed(viewer.getId());

            assertEquals(1, result.size());
            assertNull(result.get(0).mainPhotoUrl());
        }
    }

    @Nested
    @DisplayName("Phase 6 — 거리(km) 표시")
    class DistanceCalc {

        @Test
        @DisplayName("같은 area(서울) → 거리 ≈ 0")
        void sameAreaZeroDistance() {
            User target = seed.activeUser("near", 27, area.getId(), LocalDateTime.now());
            seed.approvedProfile(target.getId(), Purpose.MIXED);
            seed.flush();

            saveFeed(viewer.getId(), List.of(row(1, target.getId(), "SCORE")));

            List<FeedView> result =
                    dailyFeedQuery.findCurrentFeed(viewer.getId());

            assertEquals(1, result.size());
            Double d = result.get(0).distanceKm();
            assertEquals(0.0, d, 0.01, "동일 좌표 → ~0km");
        }

        @Test
        @DisplayName("마포 (37.5663,126.9019) ↔ 인천 중구 (37.4738,126.6216) ≈ 26.8km")
        void mapoToIncheonAround26km() {
            CodeArea mapo = areaRepository.findByCountryAndCity("서울특별시", "마포구").orElseThrow();
            CodeArea incheon = areaRepository.findByCountryAndCity("인천광역시", "중구").orElseThrow();
            viewer.updateAreaId(mapo.getId());
            em.flush();

            User target = seed.activeUser("inc", 27, incheon.getId(), LocalDateTime.now());
            seed.approvedProfile(target.getId(), Purpose.MIXED);
            seed.flush();

            saveFeed(viewer.getId(), List.of(row(1, target.getId(), "SCORE")));

            List<FeedView> result =
                    dailyFeedQuery.findCurrentFeed(viewer.getId());

            assertEquals(1, result.size());
            Double d = result.get(0).distanceKm();
            assertTrue(d != null && d > 25.0 && d < 28.5,
                    "마포↔인천 중구 ~26.8km, 실제=" + d);
        }

        // 좌표 NULL 시나리오는 운영 schema(NOT NULL constraint) 와 충돌 → 제거.
        // distanceKm null 처리는 SQL CASE WHEN 으로 안전. 운영에서 좌표 누락된 area 가 만들어질 수 없음.
    }

    /* ─── 헬퍼 ─── */

    private void saveFeed(long viewerId, List<DailyFeedRow> rows) {
        dailyFeedQuery.replaceDailyFeed(viewerId, today, rows);
        em.flush();
        em.clear();
    }

    private static DailyFeedRow row(int matchRank, long targetId, String slotType) {
        return new DailyFeedRow(matchRank, targetId, 0.5, slotType, "[]");
    }
}
