package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.entity.CodeItem;
import com.nokcha.efbe.domain.payment.model.UserTier;
import com.nokcha.efbe.domain.payment.repository.CodeItemRepository;
import com.nokcha.efbe.domain.payment.repository.UserPaletteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 등급 판정 + 한도 조회의 단일 진입점.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanLimitResolver {

    private final UserPaletteRepository userPaletteRepository;
    private final CodeItemRepository codeItemRepository;

    /** 유저 현재 등급 — 활성·미만료 구독이 있으면 PALETTE, 아니면 NORMAL. */
    public UserTier resolveTier(Long userId) {
        return userPaletteRepository.findById(userId)
                .filter(palette -> palette.isPremium(LocalDateTime.now()))
                .map(palette -> UserTier.PALETTE)
                .orElse(UserTier.NORMAL);
    }

    /** 등급 + 아이템 → 해석된 정책(마스터·등급·값). 아이템 없으면 NOT_FOUND_ITEM. */
    public ItemPolicy resolvePolicy(Long userId, String itemCode) {
        CodeItem item = codeItemRepository.findById(itemCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ITEM));
        UserTier tier = resolveTier(userId);
        return new ItemPolicy(item, tier, item.resolveValue(tier));
    }

    /** 한도/수치값 (-1 = 무제한). COUNT·DURATION·PARAM·COOLDOWN 공용. */
    public int resolveValue(Long userId, String itemCode) {
        return resolvePolicy(userId, itemCode).value();
    }

    /** CAPABILITY 가능 여부 (value >= 1). */
    public boolean isCapable(Long userId, String itemCode) {
        return resolveValue(userId, itemCode) >= 1;
    }

    /** CAPABILITY 게이트 — 불가 등급이면 throw (예: 기본 유저의 고정핀·배지). */
    public void assertCapable(Long userId, String itemCode) {
        if (!isCapable(userId, itemCode)) {
            throw new BusinessException(ErrorCode.FEATURE_NOT_AVAILABLE);
        }
    }
}
