package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.UserPalette;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserPaletteRepository extends JpaRepository<UserPalette, Long> {

    /** 행 락 조회 — 구독 상태 변경(구매/연장/해지)의 동시성 방어. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM UserPalette p WHERE p.userId = :userId")
    Optional<UserPalette> findByUserIdForUpdate(@Param("userId") Long userId);

    /** 자동갱신 배치 — auto_renew=true & 곧 만료 대상. */
    List<UserPalette> findByAutoRenewTrueAndPremiumUntilBetween(LocalDateTime from, LocalDateTime to);

    /** 만료 배치 — premium_until 이 기간 내(최근 만료)인 구독. */
    List<UserPalette> findByPremiumUntilBetween(LocalDateTime from, LocalDateTime to);
}
