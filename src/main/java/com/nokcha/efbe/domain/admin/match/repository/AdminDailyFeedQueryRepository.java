package com.nokcha.efbe.domain.admin.match.repository;

import com.nokcha.efbe.common.util.JdbcTimeMapper;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminDailyFeedItemRspDto;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminDailyFeedPageRspDto;
import com.nokcha.efbe.domain.match.entity.MatchDailyFeed.SlotType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 관리자 일일 피드 조회 데이터 액세스 — match_daily_feed + users JOIN.
 *  - 동적 필터: viewerIdFrom~viewerIdTo (range, 단일이면 from=to) / targetId / feedDate / slotType / rank
 */
@Repository
@RequiredArgsConstructor
public class AdminDailyFeedQueryRepository {

    private final EntityManager em;

    public AdminDailyFeedPageRspDto search(
            Long viewerIdFrom, Long viewerIdTo, Long targetId,
            LocalDate feedDate, SlotType slotType, Short matchRank,
            int page, int size
    ) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        if (viewerIdFrom != null && viewerIdTo != null) {
            // 단일이면 from=to, range 면 from<to
            if (viewerIdFrom.equals(viewerIdTo)) {
                where.append(" AND f.viewer_id = :viewerId ");
            } else {
                where.append(" AND f.viewer_id BETWEEN :viewerIdFrom AND :viewerIdTo ");
            }
        } else if (viewerIdFrom != null) {
            where.append(" AND f.viewer_id >= :viewerIdFrom ");
        } else if (viewerIdTo != null) {
            where.append(" AND f.viewer_id <= :viewerIdTo ");
        }
        if (targetId != null) where.append(" AND f.target_id = :targetId ");
        if (feedDate != null) where.append(" AND f.feed_date = :feedDate ");
        if (slotType != null) where.append(" AND f.slot_type = :slotType ");
        if (matchRank != null) where.append(" AND f.match_rank = :rankVal ");

        int limit = size + 1;
        long offset = (long) page * size;
        String dataSql = """
                SELECT f.feed_date, f.viewer_id, vu.nickname AS viewer_nickname,
                       f.match_rank, f.target_id, tu.nickname AS target_nickname,
                       f.slot_type, f.sort_key, f.tags_json, f.create_time
                  FROM match_daily_feed f
                  JOIN users vu ON vu.id = f.viewer_id
                  JOIN users tu ON tu.id = f.target_id
                """ + where + """
                 ORDER BY f.feed_date DESC, f.viewer_id ASC, f.match_rank ASC
                """ + " LIMIT " + limit + " OFFSET " + offset;

        Query dataQ = em.createNativeQuery(dataSql);
        if (viewerIdFrom != null && viewerIdTo != null) {
            if (viewerIdFrom.equals(viewerIdTo)) {
                dataQ.setParameter("viewerId", viewerIdFrom);
            } else {
                dataQ.setParameter("viewerIdFrom", viewerIdFrom);
                dataQ.setParameter("viewerIdTo", viewerIdTo);
            }
        } else if (viewerIdFrom != null) {
            dataQ.setParameter("viewerIdFrom", viewerIdFrom);
        } else if (viewerIdTo != null) {
            dataQ.setParameter("viewerIdTo", viewerIdTo);
        }
        if (targetId != null) dataQ.setParameter("targetId", targetId);
        if (feedDate != null) dataQ.setParameter("feedDate", feedDate);
        if (slotType != null) dataQ.setParameter("slotType", slotType.name());
        if (matchRank != null) dataQ.setParameter("rankVal", matchRank);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQ.getResultList();
        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        List<AdminDailyFeedItemRspDto> items = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            items.add(AdminDailyFeedItemRspDto.builder()
                    .feedDate(toLocalDate(r[0]))
                    .viewerId(((Number) r[1]).longValue())
                    .viewerNickname((String) r[2])
                    .matchRank(((Number) r[3]).shortValue())
                    .targetId(((Number) r[4]).longValue())
                    .targetNickname((String) r[5])
                    .slotType((String) r[6])
                    .sortKey((BigDecimal) r[7])
                    .tagsJson((String) r[8])
                    .createdAt(JdbcTimeMapper.toLocalDateTime(r[9]))
                    .build());
        }
        return AdminDailyFeedPageRspDto.builder()
                .content(items)
                .page(page)
                .size(size)
                .hasNext(hasNext)
                .build();
    }

    private static LocalDate toLocalDate(Object v) {
        if (v instanceof LocalDate ld) return ld;
        if (v instanceof java.sql.Date d) return d.toLocalDate();
        throw new IllegalStateException("feed_date 컬럼 타입 예상 외: " + v.getClass());
    }
}
