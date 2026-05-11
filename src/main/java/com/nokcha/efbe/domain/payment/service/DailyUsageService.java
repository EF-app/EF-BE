package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.entity.UserDailyUsage;
import com.nokcha.efbe.domain.payment.repository.UserDailyUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

// 일일 무료 사용 한도 카운터 서비스 (user_daily_usage 기반)
// (user_id, usage_date, action_code) 단위로 누적, 자정 0시에 새 row 생성으로 자동 리셋.
@Service
@RequiredArgsConstructor
public class DailyUsageService {

    private final UserDailyUsageRepository userDailyUsageRepository;

    // 사용량 증가, 한도 초과 시 예외
    @Transactional
    public int consume(Long userId, String actionCode, int limit) {
        LocalDate today = LocalDate.now();
        UserDailyUsage usage = userDailyUsageRepository
                .findByUserIdAndUsageDateAndActionCode(userId, today, actionCode)
                .orElseGet(() -> userDailyUsageRepository.save(UserDailyUsage.builder()
                        .userId(userId).usageDate(today).actionCode(actionCode).usedCount(0).build()));
        if (usage.getUsedCount() >= limit) {
            throw new BusinessException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }
        usage.increase();
        return usage.getUsedCount();
    }

    // 현재 사용량 조회 (한도 초과 여부 미리 확인용)
    @Transactional(readOnly = true)
    public int getUsedCount(Long userId, String actionCode) {
        LocalDate today = LocalDate.now();
        return userDailyUsageRepository
                .findByUserIdAndUsageDateAndActionCode(userId, today, actionCode)
                .map(UserDailyUsage::getUsedCount)
                .orElse(0);
    }
}
