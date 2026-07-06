package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.entity.InkHistory;
import com.nokcha.efbe.domain.payment.entity.InkWallet;
import com.nokcha.efbe.domain.payment.model.InkTxType;
import com.nokcha.efbe.domain.payment.repository.InkHistoryRepository;
import com.nokcha.efbe.domain.payment.repository.InkWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 잉크 지갑 도메인 — 원장(ink_history)에 append + 지갑(ink_wallet) 캐시 갱신을 한 트랜잭션으로
 */
@Service
@RequiredArgsConstructor
public class InkService {

    private final InkWalletRepository walletRepository;
    private final InkHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public int getBalance(Long userId) {
        return walletRepository.findById(userId).map(InkWallet::getBalance).orElse(0);
    }

    /** 결제 충전 — PAID 전이 시 호출. 지갑 없으면 생성. */
    @Transactional
    public InkHistory charge(Long userId, int amount, Long paymentId, String description) {
        InkWallet wallet = lockOrCreate(userId);
        wallet.charge(amount);
        return record(userId, InkTxType.CHARGE, amount, wallet.getBalance(), null, paymentId, description);
    }

    /** 아이템 유료 사용 차감 — 잔액 부족 시 INSUFFICIENT_STAR. */
    @Transactional
    public InkHistory use(Long userId, int amount, String itemCode, String description) {
        InkWallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_STAR));
        wallet.use(amount); // 잔액 부족이면 INSUFFICIENT_STAR
        return record(userId, InkTxType.USE, -amount, wallet.getBalance(), itemCode, null, description);
    }

    /** 관리자/이벤트 지급. */
    @Transactional
    public InkHistory grant(Long userId, int amount, String description) {
        InkWallet wallet = lockOrCreate(userId);
        wallet.charge(amount);
        return record(userId, InkTxType.GRANT, amount, wallet.getBalance(), null, null, description);
    }

    /** 환불 복원 — 잔액만 되돌림. */
    @Transactional
    public InkHistory refund(Long userId, int amount, Long paymentId, String description) {
        InkWallet wallet = lockOrCreate(userId);
        wallet.refund(amount);
        return record(userId, InkTxType.REFUND, amount, wallet.getBalance(), null, paymentId, description);
    }

    private InkWallet lockOrCreate(Long userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> walletRepository.save(InkWallet.builder().userId(userId).build()));
    }

    private InkHistory record(Long userId, InkTxType type, int amount, int balanceAfter,
                              String itemCode, Long paymentId, String description) {
        return historyRepository.save(InkHistory.builder()
                .userId(userId)
                .txType(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .itemCode(itemCode)
                .paymentId(paymentId)
                .description(description)
                .build());
    }
}
