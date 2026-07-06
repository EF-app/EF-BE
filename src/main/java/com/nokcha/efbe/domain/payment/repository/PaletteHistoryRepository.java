package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.PaletteHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaletteHistoryRepository extends JpaRepository<PaletteHistory, Long> {

    List<PaletteHistory> findByUserIdOrderByCreateTimeDesc(Long userId);
}
