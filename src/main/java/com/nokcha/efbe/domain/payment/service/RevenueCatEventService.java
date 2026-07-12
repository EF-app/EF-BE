package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.domain.payment.dto.request.RcWebhookReqDto;
import com.nokcha.efbe.domain.payment.entity.CodePaymentProduct;
import com.nokcha.efbe.domain.payment.entity.PaymentHistory;
import com.nokcha.efbe.domain.payment.entity.RcProcessedEvent;
import com.nokcha.efbe.domain.payment.model.PaymentEnvironment;
import com.nokcha.efbe.domain.payment.model.ProductType;
import com.nokcha.efbe.domain.payment.model.StoreType;
import com.nokcha.efbe.domain.payment.repository.CodePaymentProductRepository;
import com.nokcha.efbe.domain.payment.repository.PaymentHistoryRepository;
import com.nokcha.efbe.domain.payment.repository.RcProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * RevenueCat webhook 이벤트 처리 — 멱등(event.id) + 지급 디스패치. 기존 InkService/PaletteService 재사용.
 * app_user_id = 우리 userId (FE 에서 Purchases.logIn 으로 심음).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueCatEventService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RcProcessedEventRepository processedRepository;
    private final CodePaymentProductRepository productRepository;
    private final PaymentHistoryRepository paymentRepository;
    private final InkService inkService;
    private final PaletteService paletteService;

    @Transactional
    public void handle(RcWebhookReqDto.RcEvent event) {
        if (event == null || event.id() == null || event.type() == null) {
            return;
        }
        if (processedRepository.existsByEventId(event.id())) {
            return; // 멱등 — 재전송 중복 지급 방지
        }
        processedRepository.save(RcProcessedEvent.builder()
                .eventId(event.id())
                .eventType(event.type())
                .appUserId(event.appUserId())
                .build());

        Long userId = parseUserId(event.appUserId());
        if (userId == null) {
            log.warn("[RC] app_user_id 파싱 실패 — event={}, appUserId={}", event.type(), event.appUserId());
            return;
        }

        switch (event.type()) {
            case "NON_RENEWING_PURCHASE" -> grantInk(userId, event);
            case "INITIAL_PURCHASE", "RENEWAL", "PRODUCT_CHANGE", "UNCANCELLATION" -> grantPremium(userId, event);
            case "CANCELLATION" -> paletteService.cancel(userId);               // 자동갱신 해지 — 만료까지 유지
            case "EXPIRATION" -> paletteService.markExpired(userId);
            case "REFUND" -> handleRefund(userId, event);
            default -> log.info("[RC] 처리 안 함 event={}", event.type());       // BILLING_ISSUE / TRANSFER 등
        }
    }

    private void grantInk(Long userId, RcWebhookReqDto.RcEvent e) {
        CodePaymentProduct product = resolveProduct(e);
        int amount = (product != null && product.getInkAmount() != null) ? product.getInkAmount() : 0;
        String name = product != null ? product.getName() : e.productId();
        inkService.charge(userId, amount, null, "IAP 충전: " + name);
        recordPayment(userId, product, e, ProductType.INK);
    }

    private void grantPremium(Long userId, RcWebhookReqDto.RcEvent e) {
        LocalDateTime until = e.expirationAtMs() != null
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(e.expirationAtMs()), KST)
                : LocalDateTime.now();
        paletteService.applyStoreSubscription(userId, until, true, e.originalTransactionId());
        recordPayment(userId, resolveProduct(e), e, ProductType.PALETTE);
    }

    private void handleRefund(Long userId, RcWebhookReqDto.RcEvent e) {
        CodePaymentProduct product = resolveProduct(e);
        // 구독 환불 → 즉시 만료. 잉크 환불 회수는 정책 확정 후(현재 기록만).
        if (product != null && product.getProductType() == ProductType.PALETTE) {
            paletteService.markExpired(userId);
        }
        log.info("[RC] REFUND user={} product={}", userId, e.productId());
    }

    /** store_product_id → code_payment_product. 애플/구글 매핑 우선, 없으면 product_code 폴백. */
    private CodePaymentProduct resolveProduct(RcWebhookReqDto.RcEvent e) {
        StoreType store = mapStore(e.store());
        if (store == StoreType.APPLE) {
            var byApple = productRepository.findByAppleProductId(e.productId());
            if (byApple.isPresent()) return byApple.get();
        } else if (store == StoreType.GOOGLE) {
            var byGoogle = productRepository.findByGoogleProductId(e.productId());
            if (byGoogle.isPresent()) return byGoogle.get();
        }
        return productRepository.findByProductCode(e.productId()).orElse(null);
    }

    private void recordPayment(Long userId, CodePaymentProduct product, RcWebhookReqDto.RcEvent e, ProductType type) {
        PaymentHistory payment = PaymentHistory.builder()
                .userId(userId)
                .productId(product != null ? product.getProductId() : 0L)
                .productCode(product != null ? product.getProductCode() : e.productId())
                .productName(product != null ? product.getName() : e.productId())
                .productType(type)
                .inkAmount(product != null ? product.getInkAmount() : null)
                .durationDays(product != null ? product.getDurationDays() : null)
                .amount(product != null ? product.getPrice() : 0)
                .build();
        payment.applyStorePaid(mapStore(e.store()), e.productId(), e.transactionId(),
                e.originalTransactionId(), mapEnv(e.environment()), e.type(), LocalDateTime.now());
        paymentRepository.save(payment);
    }

    private static Long parseUserId(String appUserId) {
        try {
            return appUserId == null ? null : Long.parseLong(appUserId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static StoreType mapStore(String store) {
        if ("APP_STORE".equals(store)) return StoreType.APPLE;
        if ("PLAY_STORE".equals(store)) return StoreType.GOOGLE;
        return null;
    }

    private static PaymentEnvironment mapEnv(String env) {
        return "SANDBOX".equals(env) ? PaymentEnvironment.SANDBOX : PaymentEnvironment.PRODUCTION;
    }
}
