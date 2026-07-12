package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.PaletteHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaletteHistoryRepository extends JpaRepository<PaletteHistory, Long> {

    List<PaletteHistory> findByUserIdOrderByCreateTimeDesc(Long userId);

    /** 최신 이벤트 1건 — markExpired 멱등 판정용(전체 로드 회피). */
    java.util.Optional<PaletteHistory> findTop1ByUserIdOrderByCreateTimeDesc(Long userId);
}
