package com.nokcha.efbe.infra.scheduler.user;

import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserWithdrawal;
import com.nokcha.efbe.domain.user.entity.WithdrawStatus;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import com.nokcha.efbe.domain.user.repository.UserWithdrawalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserWithdrawalScheduler {

    private final UserRepository userRepository;
    private final UserWithdrawalRepository userWithdrawalRepository;

    @Transactional
//    @Scheduled(cron = "0 0 * * * *")
    public void completeExpiredWithdrawals() {
        LocalDateTime now = LocalDateTime.now();
        List<UserWithdrawal> withdrawals = userWithdrawalRepository
                .findAllByStatusAndScheduledDestroyAtLessThanEqual(WithdrawStatus.REQUESTED, now);

        for (UserWithdrawal withdrawal : withdrawals) {
            User user = userRepository.findById(withdrawal.getUserId())
                    .orElse(null);

            if (user == null) {
                log.warn("withdrawal completion skipped: user not found. withdrawalId={}, userId={}",
                        withdrawal.getId(), withdrawal.getUserId());
                continue;
            }

            user.withdraw();
            withdrawal.complete(null, null, now);
        }
    }
}
