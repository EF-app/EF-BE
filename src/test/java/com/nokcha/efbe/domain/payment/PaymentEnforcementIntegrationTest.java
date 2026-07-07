package com.nokcha.efbe.domain.payment;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.domain.payment.dto.request.RcWebhookReqDto;
import com.nokcha.efbe.domain.payment.entity.ItemUsageCounter;
import com.nokcha.efbe.domain.payment.model.ItemCodes;
import com.nokcha.efbe.domain.payment.model.ItemResetPeriod;
import com.nokcha.efbe.domain.payment.model.UsageSource;
import com.nokcha.efbe.domain.payment.model.UsageResult;
import com.nokcha.efbe.domain.payment.model.UserTier;
import com.nokcha.efbe.domain.payment.repository.CodeItemRepository;
import com.nokcha.efbe.domain.payment.repository.ItemUsageCounterRepository;
import com.nokcha.efbe.domain.payment.service.InkService;
import com.nokcha.efbe.domain.payment.service.ItemUsageService;
import com.nokcha.efbe.domain.payment.service.PaletteService;
import com.nokcha.efbe.domain.payment.service.PeriodKeyResolver;
import com.nokcha.efbe.domain.payment.service.PlanLimitResolver;
import com.nokcha.efbe.domain.payment.service.RevenueCatEventService;
import com.nokcha.efbe.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 결제 집행 통합 테스트 — 실제 MariaDB + 전체 컨텍스트(StubPaymentGateway 포함).
 * code_item 시드는 CodeItemInitializer 가 컨텍스트 기동 시 적재. payment 테이블은 논리 FK라 임의 userId 사용.
 */
@IntegrationTest
class PaymentEnforcementIntegrationTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Autowired PlanLimitResolver planLimitResolver;
    @Autowired ItemUsageService itemUsageService;
    @Autowired InkService inkService;
    @Autowired PaletteService paletteService;
    @Autowired RevenueCatEventService revenueCatEventService;
    @Autowired ItemUsageCounterRepository counterRepository;
    @Autowired PeriodKeyResolver periodKeyResolver;
    @Autowired CodeItemRepository codeItemRepository;

    @Test
    void 시드가_적재된다() {
        assertThat(codeItemRepository.findById("super_like")).isPresent();
        assertThat(codeItemRepository.findById("post_reply")).isPresent();
    }

    @Test
    void 등급_판정과_한도조회() {
        long u = 90001L;
        assertThat(planLimitResolver.resolveTier(u)).isEqualTo(UserTier.NORMAL);
        assertThat(planLimitResolver.resolveValue(u, ItemCodes.SUPER_LIKE)).isEqualTo(1);

        paletteService.applyPurchase(u, 30, null); // 프리미엄 부여

        assertThat(planLimitResolver.resolveTier(u)).isEqualTo(UserTier.PALETTE);
        assertThat(planLimitResolver.resolveValue(u, ItemCodes.SUPER_LIKE)).isEqualTo(10);
    }

    @Test
    void 슈퍼좋아요_무료소진_후_잉크폴백() {
        long u = 90002L;

        // 1회차 — 무료(기본 한도 1)
        UsageResult r1 = itemUsageService.consume(u, ItemCodes.SUPER_LIKE, 1L);
        assertThat(r1.source()).isEqualTo(UsageSource.FREE);

        // 2회차 — 무료 소진 + 잉크 없음 → 부족 예외
        assertThatThrownBy(() -> itemUsageService.consume(u, ItemCodes.SUPER_LIKE, 2L))
                .isInstanceOf(BusinessException.class);

        // 잉크 충전 후 2회차 — 잉크 차감(2방울)
        inkService.charge(u, 10, null, "테스트 충전");
        UsageResult r2 = itemUsageService.consume(u, ItemCodes.SUPER_LIKE, 3L);
        assertThat(r2.source()).isEqualTo(UsageSource.INK);
        assertThat(r2.inkCost()).isEqualTo(2);
        assertThat(inkService.getBalance(u)).isEqualTo(8); // 10 - 2
    }

    @Test
    void 매칭좋아요_일일한도_초과시_차단() {
        long u = 90003L;
        for (int i = 0; i < 30; i++) { // 기본 한도 30
            itemUsageService.consume(u, ItemCodes.MATCH_LIKE, 100L + i);
        }
        // 31회차 — 구매 불가(ink_cost NULL) → 일일 한도 초과
        assertThatThrownBy(() -> itemUsageService.consume(u, ItemCodes.MATCH_LIKE, 999L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 번개글_이중카운터() {
        long u = 90004L;
        itemUsageService.consumeLightningPost(u);

        String pk = periodKeyResolver.resolve(ItemResetPeriod.DAILY, LocalDate.now(KST));
        ItemUsageCounter write = counterRepository
                .findByUserIdAndItemCodeAndPeriodKey(u, ItemCodes.POST_WRITE, pk).orElseThrow();
        ItemUsageCounter flash = counterRepository
                .findByUserIdAndItemCodeAndPeriodKey(u, ItemCodes.POST_FLASH, pk).orElseThrow();

        assertThat(write.getUsedCount()).isEqualTo(1);
        assertThat(flash.getUsedCount()).isEqualTo(1);
    }

    @Test
    void 잉크충전_RevenueCat_webhook_지갑반영() {
        long u = 90005L;
        // RevenueCat NON_RENEWING_PURCHASE(잉크팩) webhook → 지갑 충전
        RcWebhookReqDto.RcEvent event = new RcWebhookReqDto.RcEvent(
                "evt-1", "NON_RENEWING_PURCHASE", String.valueOf(u), "INK_10",
                "APP_STORE", "SANDBOX", null, "tx-1", null);
        revenueCatEventService.handle(event);

        assertThat(inkService.getBalance(u)).isEqualTo(10); // INK_10 = 10방울
    }

    @Test
    void RevenueCat_webhook_멱등() {
        long u = 90007L;
        RcWebhookReqDto.RcEvent event = new RcWebhookReqDto.RcEvent(
                "evt-dup", "NON_RENEWING_PURCHASE", String.valueOf(u), "INK_10",
                "APP_STORE", "SANDBOX", null, "tx-dup", null);
        revenueCatEventService.handle(event);
        revenueCatEventService.handle(event); // 같은 event.id 재전송 → 스킵(멱등)

        assertThat(inkService.getBalance(u)).isEqualTo(10); // 두 번 지급 안 됨
    }

    @Test
    void 팔레트_구매시_프리미엄_전환() {
        long u = 90006L;
        assertThat(paletteService.isPremium(u)).isFalse();

        paletteService.applyPurchase(u, 30, null);

        assertThat(paletteService.isPremium(u)).isTrue();
    }
}
