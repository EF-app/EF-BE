package com.nokcha.efbe.domain.match.repository;

import com.nokcha.efbe.domain.profile.entity.ProfileStatus;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RecentNewbieBatch 전용 데이터 액세스 — 신규자 조회 + 예약 rank 점유 SELECT + FRESH_NEWBIE 다건 INSERT.
 *
 * 스케줄러(RecentNewbieBatch)에서 EntityManager 를 직접 쓰던 native/JPQL 을 이 Repository 로 분리
 * (컨벤션: EntityManager 데이터 액세스는 Repository 계층에만).
 */
@Repository
@RequiredArgsConstructor
public class RecentNewbieFanoutQueryRepository {

    private final EntityManager em;

    /** FRESH_NEWBIE INSERT 1행 캐리어 — 배치가 점수/태그 계산 후 채워 넘긴다. */
    public record FreshNewbieInsert(long viewerId, int matchRank, double sortKey, String tagsJson) {}

    /**
     * 지난 since 이후 가입한 ACTIVE + APPROVED 신규자 id (최신순).
     * 최신 가입자 우선 처리 — reserved 자리 first-fit 선점 순서를 위해 DESC.
     */
    public List<Long> findRecentNewcomerIds(LocalDateTime since) {
        return em.createQuery("""
                SELECT u.id FROM User u
                  JOIN UserProfile up ON up.userId = u.id
                 WHERE u.createTime >= :since
                   AND u.status = :active
                   AND up.profileStatus = :approved
                 ORDER BY u.createTime DESC
                """, Long.class)
                .setParameter("since", since)
                .setParameter("active", UserStatus.ACTIVE)
                .setParameter("approved", ProfileStatus.APPROVED)
                .getResultList();
    }

    /**
     * viewer 들의 오늘 daily_feed reserved rank 점유 상태 → Map&lt;viewerId, 점유된 rank Set&gt;.
     * IN 절 1회로 N명 × M자리를 한 번에 조회.
     */
    @SuppressWarnings("unchecked")
    public Map<Long, Set<Integer>> findOccupiedReservedRanks(List<Long> viewerIds, List<Integer> reservedRanks) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT viewer_id, match_rank FROM match_daily_feed " +
                " WHERE feed_date = CURDATE() AND viewer_id IN (:vs) AND match_rank IN (:rs)")
                .setParameter("vs", viewerIds)
                .setParameter("rs", reservedRanks)
                .getResultList();

        Map<Long, Set<Integer>> occupiedByViewer = new HashMap<>(viewerIds.size() * 2);
        for (Object[] r : rows) {
            long v = ((Number) r[0]).longValue();
            int rank = ((Number) r[1]).intValue();
            occupiedByViewer.computeIfAbsent(v, k -> new HashSet<>()).add(rank);
        }
        return occupiedByViewer;
    }

    /**
     * FRESH_NEWBIE 슬롯 다건 INSERT IGNORE (한 newcomer 를 여러 viewer 의 빈 reserved 자리에 배치).
     * (viewer, rank) 별 한 줄. 동시성 race 는 INSERT IGNORE 로 흡수. 반환 = 실제 insert 된 행 수.
     */
    public int insertFreshNewbieRows(long newcomerId, List<FreshNewbieInsert> rows) {
        if (rows.isEmpty()) return 0;

        StringBuilder sql = new StringBuilder(
                "INSERT IGNORE INTO match_daily_feed " +
                "(feed_date, viewer_id, match_rank, target_id, sort_key, slot_type, tags_json, create_time) VALUES ");
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("(CURDATE(), ?, ?, ?, ?, 'FRESH_NEWBIE', ?, NOW())");
        }

        Query insertQ = em.createNativeQuery(sql.toString());
        int p = 1;
        for (FreshNewbieInsert r : rows) {
            insertQ.setParameter(p++, r.viewerId());
            insertQ.setParameter(p++, r.matchRank());
            insertQ.setParameter(p++, newcomerId);
            insertQ.setParameter(p++, r.sortKey());
            insertQ.setParameter(p++, r.tagsJson());
        }
        return insertQ.executeUpdate();
    }
}
