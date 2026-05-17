package com.nokcha.efbe.domain.notice.repository;

import com.nokcha.efbe.domain.notice.entity.Notice;
import com.nokcha.efbe.domain.notice.entity.NoticeCategory;
import com.nokcha.efbe.domain.notice.entity.NoticeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findAllByStatus(NoticeStatus status, Pageable pageable);

    Page<Notice> findAllByStatusAndCategory(NoticeStatus status, NoticeCategory category, Pageable pageable);

    Optional<Notice> findByIdAndStatus(Long id, NoticeStatus status);

    List<Notice> findAllByStatusAndScheduledAtLessThanEqual(NoticeStatus status, LocalDateTime scheduledAt);
}
