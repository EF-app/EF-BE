package com.nokcha.efbe.infra.scheduler.match;

import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.match.model.MatchActionType;
import com.nokcha.efbe.domain.profile.entity.Purpose;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.support.IntegrationTest;
import com.nokcha.efbe.support.MatchTestSeed;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase B(매시간 미니 배치) — RecentNewbieBatch 동작 검증.
 *  - 신규자(가입 < freshNewbieWindowHours)만 fan-out 대상
 *  - 호환 viewer (status / profile_status / age / 그룹 / block / match_actions) 만 INSERT
 *  - daily_feed 끝에 rank=(기존 max+1) 로 추가, slot_type='FRESH_NEWBIE'
 *  - 중복 실행 시 INSERT IGNORE 로 충돌 흡수 (PK = viewer_id + rank 가 같지 않으므로 새 rank 로 또 들어감)
 */
@IntegrationTest
class RecentNewbieBatchIntegrationTest {

    @Autowired EntityManager em;
    @Autowired RecentNewbieBatch batch;
    @Autowired AreaRepository areaRepository;

    private MatchTestSeed seed;
    private CodeArea area;

    @BeforeEach
    void setUp() {
        seed = new MatchTestSeed(em);
        area = areaRepository.findByCountryAndCity("서울특별시", "선택안함").orElseThrow();
    }

    @Test
    @DisplayName("신규자 fan-out — 호환 viewer 의 daily_feed 끝에 FRESH_NEWBIE row 추가")
    void fanOutHappyPath() {
        // 활성 viewer 2명
        User viewer1 = seed.activeUser("v1", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(viewer1.getId(), Purpose.MIXED);
        User viewer2 = seed.activeUser("v2", 28, area.getId(), LocalDateTime.now());
        seed.approvedProfile(viewer2.getId(), Purpose.MIXED);

        // 신규자 1명 (오늘 가입 — BaseEntity.createTime 이 now)
        User newcomer = seed.activeUser("nc", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(newcomer.getId(), Purpose.MIXED);
        seed.flush();

        batch.run();

        // viewer1 / viewer2 의 daily_feed 에 FRESH_NEWBIE row 1개씩
        Long c1 = countFresh(viewer1.getId(), newcomer.getId());
        Long c2 = countFresh(viewer2.getId(), newcomer.getId());
        assertEquals(1L, c1, "viewer1 의 피드에 newcomer 등장");
        assertEquals(1L, c2, "viewer2 의 피드에 newcomer 등장");
    }

    @Test
    @DisplayName("이미 LIKE 한 페어 → fan-out 제외")
    void alreadyLikedExcluded() {
        User viewer = seed.activeUser("v", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(viewer.getId(), Purpose.MIXED);
        User newcomer = seed.activeUser("nc", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(newcomer.getId(), Purpose.MIXED);
        // viewer 가 newcomer 를 이미 LIKE
        seed.action(viewer.getId(), newcomer.getId(), MatchActionType.LIKE, null);
        seed.flush();

        batch.run();

        assertEquals(0L, countFresh(viewer.getId(), newcomer.getId()),
                "이미 LIKE 한 경우 신규자가 안 보여야");
    }

    @Test
    @DisplayName("age 차 9 → 그룹 제외")
    void ageMismatchExcluded() {
        User viewer = seed.activeUser("v", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(viewer.getId(), Purpose.MIXED);
        User newcomer = seed.activeUser("nc", 37, area.getId(), LocalDateTime.now());
        seed.approvedProfile(newcomer.getId(), Purpose.MIXED);
        seed.flush();

        batch.run();

        assertEquals(0L, countFresh(viewer.getId(), newcomer.getId()),
                "ageMaxDiff(8) 초과 → 제외");
    }

    @Test
    @DisplayName("국내 viewer ↔ 해외 신규자 → 그룹 분리로 제외")
    void countryGroupMismatch() {
        CodeArea overseas = areaRepository.findByCountryAndCity("해외", "일본").orElseThrow();

        User viewer = seed.activeUser("v", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(viewer.getId(), Purpose.MIXED);
        User newcomer = seed.activeUser("nc", 27, overseas.getId(), LocalDateTime.now());
        seed.approvedProfile(newcomer.getId(), Purpose.MIXED);
        seed.flush();

        batch.run();

        assertEquals(0L, countFresh(viewer.getId(), newcomer.getId()),
                "국내·해외 그룹 다르면 제외");
    }

    @Test
    @DisplayName("예약 rank (5,10,15,20,25) 중 비어있는 첫 자리 = rank 5 에 INSERT")
    void firstEmptyReservedRank() {
        User viewer = seed.activeUser("v", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(viewer.getId(), Purpose.MIXED);
        User newcomer = seed.activeUser("nc", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(newcomer.getId(), Purpose.MIXED);
        seed.flush();

        batch.run();

        Number rank = (Number) em.createNativeQuery(
                "SELECT match_rank FROM match_daily_feed WHERE viewer_id = :v AND target_id = :t")
                .setParameter("v", viewer.getId())
                .setParameter("t", newcomer.getId())
                .getSingleResult();
        assertEquals(5, rank.intValue(),
                "첫 비어있는 예약 자리 (rank=5) 에 INSERT, 실제=" + rank);
    }

    @Test
    @DisplayName("rank 5 이 이미 차있으면 rank 10 으로 이동")
    void skipsFilledReservedRank() {
        User viewer = seed.activeUser("v", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(viewer.getId(), Purpose.MIXED);
        User newcomer = seed.activeUser("nc", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(newcomer.getId(), Purpose.MIXED);
        User filler = seed.activeUser("f", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(filler.getId(), Purpose.MIXED);
        seed.flush();
        // rank=5 이 이미 채워진 상태
        em.createNativeQuery("""
                INSERT INTO match_daily_feed
                       (feed_date, viewer_id, match_rank, target_id, sort_key, slot_type, tags_json)
                VALUES (CURDATE(), :v, 5, :t, 0.5, 'SCORE', '[]')
                """)
                .setParameter("v", viewer.getId())
                .setParameter("t", filler.getId())
                .executeUpdate();
        em.flush();

        batch.run();

        Number rank = (Number) em.createNativeQuery(
                "SELECT match_rank FROM match_daily_feed WHERE viewer_id = :v AND target_id = :t")
                .setParameter("v", viewer.getId())
                .setParameter("t", newcomer.getId())
                .getSingleResult();
        assertEquals(10, rank.intValue(),
                "rank=5 차있으니 다음 예약 자리 (rank=10) 에 INSERT, 실제=" + rank);
    }

    @Test
    @DisplayName("예약 자리 (5,10,15,20,25) 전부 차있으면 신규자 INSERT 0")
    void skipWhenAllReservedFilled() {
        User viewer = seed.activeUser("v", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(viewer.getId(), Purpose.MIXED);
        User newcomer = seed.activeUser("nc", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(newcomer.getId(), Purpose.MIXED);
        User filler = seed.activeUser("f", 27, area.getId(), LocalDateTime.now());
        seed.approvedProfile(filler.getId(), Purpose.MIXED);
        seed.flush();
        // 예약 자리 다섯 자리 모두 차있음 (reservedSlots=5, reservedStep=5 → {5,10,15,20,25})
        for (int r : new int[]{5, 10, 15, 20, 25}) {
            em.createNativeQuery("""
                    INSERT INTO match_daily_feed
                           (feed_date, viewer_id, match_rank, target_id, sort_key, slot_type, tags_json)
                    VALUES (CURDATE(), :v, :r, :t, 0.5, 'SCORE', '[]')
                    """)
                    .setParameter("v", viewer.getId())
                    .setParameter("r", r)
                    .setParameter("t", filler.getId())
                    .executeUpdate();
        }
        em.flush();

        batch.run();

        assertEquals(0L, countFresh(viewer.getId(), newcomer.getId()),
                "예약 5 자리 다 차있으면 신규자 INSERT 0");
    }

    /* ─── 헬퍼 ─── */

    private Long countFresh(Long viewerId, Long targetId) {
        return ((Number) em.createNativeQuery("""
                SELECT COUNT(*) FROM match_daily_feed
                 WHERE viewer_id = :v AND target_id = :t AND slot_type = 'FRESH_NEWBIE'
                """)
                .setParameter("v", viewerId)
                .setParameter("t", targetId)
                .getSingleResult()).longValue();
    }
}
