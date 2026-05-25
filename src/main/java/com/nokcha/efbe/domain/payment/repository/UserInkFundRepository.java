package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.UserInkFund;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserInkFundRepository extends JpaRepository<UserInkFund, Long> {

    Optional<UserInkFund> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from UserInkFund b where b.userId = :userId")
    Optional<UserInkFund> findByIdForUpdate(@Param("userId") Long userId);
}
