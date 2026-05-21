package com.nokcha.efbe.domain.notice.repository;

import com.nokcha.efbe.domain.notice.entity.Notice;
import com.nokcha.efbe.domain.notice.entity.NoticeCategory;
import com.nokcha.efbe.domain.notice.entity.NoticeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
            select n
            from Notice n
            where n.status = :status
            order by
                case when n.sortOrder is null then 1 else 0 end asc,
                n.sortOrder asc,
                n.createTime desc
            """)
    Page<Notice> findPublicNoticesByStatus(NoticeStatus status, Pageable pageable);

    @Query("""
            select n
            from Notice n
            where n.status = :status
              and n.category = :category
            order by
                case when n.sortOrder is null then 1 else 0 end asc,
                n.sortOrder asc,
                n.createTime desc
            """)
    Page<Notice> findPublicNoticesByStatusAndCategory(NoticeStatus status, NoticeCategory category, Pageable pageable);

    Optional<Notice> findByIdAndStatus(Long id, NoticeStatus status);

    List<Notice> findAllByStatusAndScheduledAtLessThanEqual(NoticeStatus status, LocalDateTime scheduledAt);

    @Query("select coalesce(max(n.sortOrder), 0) from Notice n where n.sortOrder is not null")
    int findMaxSortOrder();

    @Modifying
    @Query("update Notice n set n.sortOrder = n.sortOrder + 1 where n.sortOrder is not null and n.sortOrder >= :fromOrder")
    void incrementSortOrdersFrom(int fromOrder);

    @Modifying
    @Query("update Notice n set n.sortOrder = n.sortOrder - 1 where n.sortOrder is not null and n.sortOrder > :fromOrder")
    void decrementSortOrdersAfter(int fromOrder);

    @Modifying
    @Query("update Notice n set n.sortOrder = n.sortOrder + 1 where n.sortOrder is not null and n.sortOrder >= :fromOrder and n.sortOrder < :toOrder")
    void incrementSortOrdersBetween(int fromOrder, int toOrder);

    @Modifying
    @Query("update Notice n set n.sortOrder = n.sortOrder - 1 where n.sortOrder is not null and n.sortOrder <= :toOrder and n.sortOrder > :fromOrder")
    void decrementSortOrdersBetween(int fromOrder, int toOrder);
}
