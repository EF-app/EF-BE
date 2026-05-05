package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.dto.response.StarTransactionRspDto;
import com.nokcha.efbe.domain.payment.dto.response.UserStarBalanceRspDto;
import com.nokcha.efbe.domain.payment.entity.StarTransaction;
import com.nokcha.efbe.domain.payment.entity.StarTxType;
import com.nokcha.efbe.domain.payment.entity.UserStarBalance;
import com.nokcha.efbe.domain.payment.repository.StarTransactionRepository;
import com.nokcha.efbe.domain.payment.repository.UserStarBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 별 재화 서비스 (충전/사용/환불 원자 처리 + 거래 원장 기록)
@Service
@RequiredArgsConstructor
public class StarService {

    private final UserStarBalanceRepository userStarBalanceRepository;
    private final StarTransactionRepository starTransactionRepository;

    // 내 별 잔액 조회 (없으면 0 잔액으로 초기화)
    @Transactional
    public UserStarBalanceRspDto getMyBalance(Long userId) {
        UserStarBalance balance = ensureBalance(userId);
        return UserStarBalanceRspDto.from(balance);
    }

    // 별 충전 (결제 성공 시 내부 호출)
    @Transactional
    public UserStarBalanceRspDto charge(Long userId, int amount, String refType, Long refId, String memo) {
        UserStarBalance b = ensureBalance(userId);
        b.charge(amount);
        writeTx(userId, StarTxType.CHARGE, amount, b.getBalance(), refType, refId, memo);
        return UserStarBalanceRspDto.from(b);
    }

    // 별 차감 (아이템 구매 등 내부 호출)
    @Transactional
    public UserStarBalanceRspDto use(Long userId, int amount, String refType, Long refId, String memo) {
        UserStarBalance b = userStarBalanceRepository.findByIdForUpdate(userId)
                .orElseGet(() -> userStarBalanceRepository.save(UserStarBalance.builder().userId(userId).build()));
        if (b.getBalance() < amount) throw new BusinessException(ErrorCode.INSUFFICIENT_STAR);
        b.use(amount);
        writeTx(userId, StarTxType.USE, -amount, b.getBalance(), refType, refId, memo);
        return UserStarBalanceRspDto.from(b);
    }

    // 관리자 지급
    @Transactional
    public UserStarBalanceRspDto grant(Long userId, int amount, String memo) {
        UserStarBalance b = ensureBalance(userId);
        b.charge(amount);
        writeTx(userId, StarTxType.ADMIN_GRANT, amount, b.getBalance(), null, null, memo);
        return UserStarBalanceRspDto.from(b);
    }

    // 환불
    @Transactional
    public UserStarBalanceRspDto refund(Long userId, int amount, String refType, Long refId, String memo) {
        UserStarBalance b = ensureBalance(userId);
        b.refund(amount);
        writeTx(userId, StarTxType.REFUND, amount, b.getBalance(), refType, refId, memo);
        return UserStarBalanceRspDto.from(b);
    }

    // 내 거래 내역
    @Transactional(readOnly = true)
    public Page<StarTransactionRspDto> getTransactions(Long userId, int page, int size) {
        return starTransactionRepository.findByUserIdOrderByCreateTimeDesc(userId, PageRequest.of(page, size))
                .map(StarTransactionRspDto::from);
    }

    // 잔액 row 없으면 생성 (동시성 보호 위해 LOCK)
    private UserStarBalance ensureBalance(Long userId) {
        return userStarBalanceRepository.findByIdForUpdate(userId)
                .orElseGet(() -> userStarBalanceRepository.save(UserStarBalance.builder().userId(userId).build()));
    }

    // 거래 원장 append
    private void writeTx(Long userId, StarTxType type, int amount, int balanceAfter,
                         String refType, Long refId, String memo) {
        starTransactionRepository.save(StarTransaction.builder()
                .userId(userId).txType(type).amount(amount).balanceAfter(balanceAfter)
                .refType(refType).refId(refId).memo(memo).build());
    }
}
