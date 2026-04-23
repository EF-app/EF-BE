package com.nokcha.efbe.domain.notice.repository;

import com.nokcha.efbe.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
