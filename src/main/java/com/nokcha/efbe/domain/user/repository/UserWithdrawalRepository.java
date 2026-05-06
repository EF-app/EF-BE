package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.user.entity.WithdrawStatus;
import com.nokcha.efbe.domain.user.entity.UserWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserWithdrawalRepository extends JpaRepository<UserWithdrawal, Long> {

    Optional<UserWithdrawal> findByUserId(Long userId);

    List<UserWithdrawal> findAllByStatusAndScheduledDestroyAtLessThanEqual(WithdrawStatus status, LocalDateTime scheduledDestroyAt);
}
