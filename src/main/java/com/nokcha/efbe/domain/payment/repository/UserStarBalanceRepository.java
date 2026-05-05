package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.UserStarBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// 유저 별 잔액 레포지토리 (PESSIMISTIC_WRITE 로 동시 차감 보호)
public interface UserStarBalanceRepository extends JpaRepository<UserStarBalance, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from UserStarBalance b where b.userId = :userId")
    Optional<UserStarBalance> findByIdForUpdate(@Param("userId") Long userId);
}
