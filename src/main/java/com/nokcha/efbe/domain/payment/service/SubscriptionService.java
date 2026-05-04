package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.dto.response.SubscriptionPlanRspDto;
import com.nokcha.efbe.domain.payment.dto.response.UserSubscriptionRspDto;
import com.nokcha.efbe.domain.payment.entity.CodeSubscription;
import com.nokcha.efbe.domain.payment.entity.UserSubscription;
import com.nokcha.efbe.domain.payment.repository.CodeSubscriptionRepository;
import com.nokcha.efbe.domain.payment.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// 구독 서비스 (플랜 조회/구독 시작/자동갱신 토글/조회)
// v1.2 이력형 B: 활성 구독은 user_id + is_active=true 로 조회. 갱신 시 옛 활성 row 비활성화 후 새 row 생성.
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final CodeSubscriptionRepository subscriptionPlanRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    // 활성 플랜 목록 (변경 빈도 낮음 - 캐시)
    @Cacheable("subscriptionPlans")
    @Transactional(readOnly = true)
    public List<SubscriptionPlanRspDto> getPlans() {
        return subscriptionPlanRepository.findByIsActiveTrueOrderByPriceAsc().stream()
                .map(SubscriptionPlanRspDto::from).toList();
    }

    // 내 (활성) 구독 조회
    @Transactional(readOnly = true)
    public UserSubscriptionRspDto getMySubscription(Long userId) {
        return userSubscriptionRepository.findByUserIdAndIsActiveTrue(userId)
                .map(UserSubscriptionRspDto::from)
                .orElse(null);
    }

    // 구독 시작/갱신 (결제 성공 훅에서 호출) — 이력형이므로 활성 구독이 있으면 비활성화 후 새 row 생성
    @Transactional
    public UserSubscriptionRspDto startOrRenew(Long userId, Integer planId) {
        CodeSubscription plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PLAN));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(plan.getDurationDays());

        Optional<UserSubscription> active = userSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        active.ifPresent(UserSubscription::deactivate);  // 옛 활성 구독 비활성화 (이력 보존)

        UserSubscription sub = userSubscriptionRepository.save(UserSubscription.builder()
                .userId(userId).planId(planId).startedAt(now).endDate(endDate).build());
        return UserSubscriptionRspDto.from(sub);
    }

    // 자동 갱신 토글 — 활성 구독에 대해서만 적용
    @Transactional
    public UserSubscriptionRspDto setAutoRenew(Long userId, boolean autoRenew) {
        UserSubscription sub = userSubscriptionRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PLAN));
        if (autoRenew) {
            sub.enableAutoRenew();
        } else {
            sub.cancel(LocalDateTime.now());
        }
        return UserSubscriptionRspDto.from(sub);
    }
}
