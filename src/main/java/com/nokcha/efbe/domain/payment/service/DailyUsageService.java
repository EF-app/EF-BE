package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.entity.UserMonthlyUsage;
import com.nokcha.efbe.domain.payment.repository.UserMonthlyUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

// 무료 사용 한도 카운터 서비스 (v1.2: UserMonthlyUsage 기반 — 월 단위 누적)
// 메서드 시그니처(consume / getUsedCount) 는 호출자 무영향 위해 그대로 유지
// "Daily" 라는 이름은 호환을 위해 유지 — 내부 카운터는 월 단위로 변경.
@Service
@RequiredArgsConstructor
public class DailyUsageService {

    private final UserMonthlyUsageRepository userMonthlyUsageRepository;

    // 사용량 증가, 한도 초과 시 예외
    @Transactional
    public int consume(Long userId, String actionType, int limit) {
        LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
        UserMonthlyUsage usage = userMonthlyUsageRepository
                .findByUserIdAndPeriodStartAndFeatureCode(userId, periodStart, actionType)
                .orElseGet(() -> userMonthlyUsageRepository.save(UserMonthlyUsage.builder()
                        .userId(userId).periodStart(periodStart).featureCode(actionType).usedCount(0).build()));
        if (usage.getUsedCount() >= limit) {
            throw new BusinessException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }
        usage.increase();
        return usage.getUsedCount();
    }

    // 현재 사용량 조회 (한도 초과 여부 미리 확인용)
    @Transactional(readOnly = true)
    public int getUsedCount(Long userId, String actionType) {
        LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
        return userMonthlyUsageRepository
                .findByUserIdAndPeriodStartAndFeatureCode(userId, periodStart, actionType)
                .map(UserMonthlyUsage::getUsedCount)
                .orElse(0);
    }
}
