package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.domain.payment.entity.PaletteHistory;
import com.nokcha.efbe.domain.payment.entity.UserPalette;
import com.nokcha.efbe.domain.payment.model.PaletteEventType;
import com.nokcha.efbe.domain.payment.repository.PaletteHistoryRepository;
import com.nokcha.efbe.domain.payment.repository.UserPaletteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 팔레트(구독) 도메인 — 상태(user_palette) + 이력(palette_history)을 한 트랜잭션으로.
 *
 * 구매/연장은 현재 만료가 미래면 그 위에 누적(EXTEND), 아니면 지금부터 시작(START).
 * 해지는 premium_until 을 유지한 채 auto_renew 만 끔.
 */
@Service
@RequiredArgsConstructor
public class PaletteService {

    private final UserPaletteRepository paletteRepository;
    private final PaletteHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public boolean isPremium(Long userId) {
        return paletteRepository.findById(userId)
                .map(p -> p.isPremium(LocalDateTime.now()))
                .orElse(false);
    }

    /** 결제 PAID 시 구독 부여/연장. paymentId null 이면 무료/관리자 지급(GIFT). */
    @Transactional
    public void applyPurchase(Long userId, int durationDays, Long paymentId) {
        LocalDateTime now = LocalDateTime.now();
        UserPalette palette = lockOrCreate(userId);

        LocalDateTime before = palette.getPremiumUntil();
        boolean wasActive = palette.isPremium(now);
        LocalDateTime base = wasActive ? before : now;   // 활성이면 끝에 누적, 아니면 지금부터
        LocalDateTime newUntil = base.plusDays(durationDays);

        palette.applyPurchase(newUntil);

        PaletteEventType event = (paymentId == null) ? PaletteEventType.GIFT
                : (wasActive ? PaletteEventType.EXTEND : PaletteEventType.START);
        record(userId, event, paymentId, before, newUntil, null);
    }

    /** 자동갱신 해지 — 만료일까지는 프리미엄 유지. */
    @Transactional
    public void cancel(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        UserPalette palette = lockOrCreate(userId);
        palette.cancel(now);
        record(userId, PaletteEventType.CANCEL, null, palette.getPremiumUntil(), palette.getPremiumUntil(), null);
    }

    /** 해지 철회 — 자동갱신 재개. */
    @Transactional
    public void reactivate(Long userId) {
        UserPalette palette = lockOrCreate(userId);
        palette.reactivate();
    }

    /** 만료 배치 — 이력에 EXPIRE 남김 (상태는 조회 시 until<now 로 자동 무료 판정). */
    @Transactional
    public void markExpired(Long userId) {
        paletteRepository.findByUserIdForUpdate(userId).ifPresent(palette ->
                record(userId, PaletteEventType.EXPIRE, null,
                        palette.getPremiumUntil(), palette.getPremiumUntil(), "구독 만료"));
    }

    private UserPalette lockOrCreate(Long userId) {
        return paletteRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> paletteRepository.save(UserPalette.builder().userId(userId).build()));
    }

    private void record(Long userId, PaletteEventType event, Long paymentId,
                        LocalDateTime before, LocalDateTime after, String description) {
        historyRepository.save(PaletteHistory.builder()
                .userId(userId)
                .eventType(event)
                .paymentId(paymentId)
                .beforeUntil(before)
                .afterUntil(after)
                .description(description)
                .build());
    }
}
