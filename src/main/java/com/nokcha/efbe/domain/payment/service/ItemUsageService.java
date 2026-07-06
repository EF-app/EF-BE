package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.entity.CodeItem;
import com.nokcha.efbe.domain.payment.entity.InkHistory;
import com.nokcha.efbe.domain.payment.entity.ItemUsageHistory;
import com.nokcha.efbe.domain.payment.model.ItemCodes;
import com.nokcha.efbe.domain.payment.model.ItemValueType;
import com.nokcha.efbe.domain.payment.model.UsageResult;
import com.nokcha.efbe.domain.payment.model.UsageSource;
import com.nokcha.efbe.domain.payment.model.UserTier;
import com.nokcha.efbe.domain.payment.repository.ItemUsageCounterRepository;
import com.nokcha.efbe.domain.payment.repository.ItemUsageHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 아이템 사용 집행 — 무료 한도(카운터) → 소진 시 잉크 폴백을 한 트랜잭션으로.

 * 무료·유료 전건은 {@code item_usage_history} 에 적재(통합형).
 */
@Service
@RequiredArgsConstructor
public class ItemUsageService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final PlanLimitResolver planLimitResolver;
    private final PeriodKeyResolver periodKeyResolver;
    private final ItemUsageCounterRepository counterRepository;
    private final ItemUsageHistoryRepository usageHistoryRepository;
    private final InkService inkService;

    /**
     * COUNT 아이템 1회 사용. 무료 한도 내면 카운터 +1(FREE), 소진되면 잉크 차감(INK) 폴백,
     * 구매 불가 아이템이면 DAILY_LIMIT_EXCEEDED.
     */
    @Transactional
    public UsageResult consume(Long userId, String itemCode, Long targetId) {
        ItemPolicy policy = planLimitResolver.resolvePolicy(userId, itemCode);
        CodeItem item = policy.item();
        requireCount(item);

        // 무제한 등급 — 카운터/잉크 없이 무료 기록
        if (policy.isUnlimited()) {
            return record(userId, itemCode, UsageSource.FREE, 0, policy.tier(), null, targetId);
        }

        // 무료 몫 원자 차감 시도 (limit > 0 일 때만; limit == 0 은 바로 유료/불가)
        if (policy.value() > 0 && tryConsumeFree(userId, item, policy.value())) {
            return record(userId, itemCode, UsageSource.FREE, 0, policy.tier(), null, targetId);
        }

        // 무료 소진 → 잉크 폴백
        if (!item.isPurchasable()) {
            throw new BusinessException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }
        int cost = item.getInkCost();
        InkHistory ink = inkService.use(userId, cost, itemCode, item.getName());
        return record(userId, itemCode, UsageSource.INK, cost, policy.tier(), ink.getInkHistoryId(), targetId);
    }

    /**
     * 번개 포스트 — post_write + post_flash 이중 카운터. 둘 다 무료 한도 내여야 성공.
     * 하나라도 소진이면 throw → 트랜잭션 롤백(둘 다 유료 폴백 없음).
     */
    @Transactional
    public void consumeLightningPost(Long userId) {
        consumeFreeOnly(userId, ItemCodes.POST_WRITE);
        consumeFreeOnly(userId, ItemCodes.POST_FLASH);
    }

    /** 무료 전용 소비(ink_cost 없는 COUNT: 글쓰기·번개·되돌리기·매칭좋아요). 소진이면 throw. */
    @Transactional
    public void consumeFreeOnly(Long userId, String itemCode) {
        ItemPolicy policy = planLimitResolver.resolvePolicy(userId, itemCode);
        requireCount(policy.item());
        if (policy.isUnlimited()) {
            record(userId, itemCode, UsageSource.FREE, 0, policy.tier(), null, null);
            return;
        }
        if (policy.value() <= 0 || !tryConsumeFree(userId, policy.item(), policy.value())) {
            throw new BusinessException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }
        record(userId, itemCode, UsageSource.FREE, 0, policy.tier(), null, null);
    }

    private boolean tryConsumeFree(Long userId, CodeItem item, int limit) {
        String periodKey = periodKeyResolver.resolve(item.getResetPeriod(), LocalDate.now(KST));
        counterRepository.ensureRow(userId, item.getItemCode(), periodKey);
        return counterRepository.tryConsume(userId, item.getItemCode(), periodKey, limit) == 1;
    }

    private void requireCount(CodeItem item) {
        if (item.getValueType() != ItemValueType.COUNT) {
            throw new IllegalStateException("consume 는 COUNT 아이템 전용: " + item.getItemCode());
        }
    }

    private UsageResult record(Long userId, String itemCode, UsageSource source, int inkCost,
                               UserTier tier, Long inkHistoryId, Long targetId) {
        usageHistoryRepository.save(ItemUsageHistory.builder()
                .userId(userId)
                .itemCode(itemCode)
                .source(source)
                .inkCost(inkCost)
                .tierAtUse(tier)
                .inkHistoryId(inkHistoryId)
                .targetId(targetId)
                .build());
        return new UsageResult(source, inkCost);
    }
}
