package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.dto.response.ItemQuotaRspDto;
import com.nokcha.efbe.domain.payment.entity.CodeItem;
import com.nokcha.efbe.domain.payment.entity.ItemUsageCounter;
import com.nokcha.efbe.domain.payment.model.ItemValueType;
import com.nokcha.efbe.domain.payment.repository.ItemUsageCounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 아이템 한도 잔여 조회(읽기 전용).
 *
 *  remaining = 등급별 limit − 현재 주기 used_count. remaining 은 저장하지 않고 읽을 때 계산 —
 *  limit 이 등급(가변)에 의존하므로, used_count(등급 불변)만 진실로 두고 매 조회 시 파생한다.
 *  집행(차감·차단)은 여전히 {@link ItemUsageService} 의 카운터 가드가 담당하고, 이 값은 표시용 스냅샷.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemQuotaService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final PlanLimitResolver planLimitResolver;
    private final PeriodKeyResolver periodKeyResolver;
    private final ItemUsageCounterRepository counterRepository;

    /** 단일 COUNT 아이템의 현재 주기 잔여. 존재하지 않으면 NOT_FOUND_ITEM, COUNT 아니면 INVALID_REQUEST. */
    public ItemQuotaRspDto getQuota(Long userId, String itemCode) {
        ItemPolicy policy = planLimitResolver.resolvePolicy(userId, itemCode);
        CodeItem item = policy.item();
        if (item.getValueType() != ItemValueType.COUNT) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        int limit = policy.value();
        String periodKey = periodKeyResolver.resolve(item.getResetPeriod(), LocalDate.now(KST));
        int used = counterRepository.findByUserIdAndItemCodeAndPeriodKey(userId, itemCode, periodKey)
                .map(ItemUsageCounter::getUsedCount)
                .orElse(0);
        boolean unlimited = limit < 0;
        Integer remaining = unlimited ? null : Math.max(0, limit - used);
        return ItemQuotaRspDto.of(item, limit, used, remaining, unlimited, periodKey);
    }
}
