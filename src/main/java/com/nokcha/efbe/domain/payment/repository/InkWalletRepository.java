package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.InkWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InkWalletRepository extends JpaRepository<InkWallet, Long> {

    /**
     * 지갑 행 없으면 0잔액으로 생성(멱등), 있으면 no-op. 감사 컬럼은 DB DEFAULT 사용.
     * lockOrCreate 의 "없는 행은 FOR UPDATE 로 못 잠근다" 레이스를 원자 upsert 로 제거.
     */
    @Modifying
    @Query(value = "INSERT INTO ink_wallet (user_id, balance, total_charged, total_used) " +
            "VALUES (:userId, 0, 0, 0) ON DUPLICATE KEY UPDATE user_id = user_id", nativeQuery = true)
    void ensureWallet(@Param("userId") Long userId);

    /**
     * 행 락 조회 (SELECT ... FOR UPDATE) — 지갑 변경(충전/차감)의 동시성 방어.
     * 같은 유저 동시 요청을 직렬화해 lost update 방지 + 변경 후 balance_after 를 바로 얻음.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM InkWallet w WHERE w.userId = :userId")
    Optional<InkWallet> findByUserIdForUpdate(@Param("userId") Long userId);
}
