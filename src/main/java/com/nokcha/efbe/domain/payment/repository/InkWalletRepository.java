package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.InkWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InkWalletRepository extends JpaRepository<InkWallet, Long> {

    /**
     * 행 락 조회 (SELECT ... FOR UPDATE) — 지갑 변경(충전/차감)의 동시성 방어
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM InkWallet w WHERE w.userId = :userId")
    Optional<InkWallet> findByUserIdForUpdate(@Param("userId") Long userId);
}
