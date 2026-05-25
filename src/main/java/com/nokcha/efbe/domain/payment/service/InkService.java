package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.dto.response.StarTransactionRspDto;
import com.nokcha.efbe.domain.payment.dto.response.UserInkFundRspDto;
import com.nokcha.efbe.domain.payment.entity.InkTransaction;
import com.nokcha.efbe.domain.payment.entity.InkTxType;
import com.nokcha.efbe.domain.payment.entity.UserInkFund;
import com.nokcha.efbe.domain.payment.repository.InkTransactionRepository;
import com.nokcha.efbe.domain.payment.repository.UserInkFundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InkService {

    private final UserInkFundRepository userInkFundRepository;
    private final InkTransactionRepository inkTransactionRepository;

    // 내 별 잔액 조회 (없으면 0 잔액으로 초기화)
    @Transactional
    public UserInkFundRspDto getMyBalance(Long userId) {
        UserInkFund fund = ensureBalance(userId);
        return UserInkFundRspDto.from(fund);
    }

    // 별 충전 (결제 성공 시 내부 호출)
    @Transactional
    public UserInkFundRspDto charge(Long userId, int amount, String refType, Long refId, String memo) {
        UserInkFund fund = ensureBalance(userId);
        fund.charge(amount);
        writeTx(userId, InkTxType.CHARGE, amount, fund.getFund(), refType, refId, memo);
        return UserInkFundRspDto.from(fund);
    }

    // 별 차감 (아이템 구매 등 내부 호출)
    @Transactional
    public UserInkFundRspDto use(Long userId, int amount, String refType, Long refId, String memo) {
        UserInkFund fund = userInkFundRepository.findByIdForUpdate(userId)
                .orElseGet(() -> userInkFundRepository.save(UserInkFund.builder().userId(userId).build()));
        if (fund.getFund() < amount) throw new BusinessException(ErrorCode.INSUFFICIENT_STAR);
        fund.use(amount);
        writeTx(userId, InkTxType.USE, -amount, fund.getFund(), refType, refId, memo);
        return UserInkFundRspDto.from(fund);
    }

    // 관리자 지급
    @Transactional
    public UserInkFundRspDto grant(Long userId, int amount, String memo) {
        UserInkFund fund = ensureBalance(userId);
        fund.charge(amount);
        writeTx(userId, InkTxType.ADMIN_GRANT, amount, fund.getFund(), null, null, memo);
        return UserInkFundRspDto.from(fund);
    }

    // 환불
    @Transactional
    public UserInkFundRspDto refund(Long userId, int amount, String refType, Long refId, String memo) {
        UserInkFund fund = ensureBalance(userId);
        fund.refund(amount);
        writeTx(userId, InkTxType.REFUND, amount, fund.getFund(), refType, refId, memo);
        return UserInkFundRspDto.from(fund);
    }

    // 내 거래 내역
    @Transactional(readOnly = true)
    public Page<StarTransactionRspDto> getTransactions(Long userId, int page, int size) {
        return inkTransactionRepository.findByUserIdOrderByCreateTimeDesc(userId, PageRequest.of(page, size))
                .map(StarTransactionRspDto::from);
    }

    // 잔액 row 없으면 생성 (동시성 보호 위해 LOCK)
    private UserInkFund ensureBalance(Long userId) {
        return userInkFundRepository.findByIdForUpdate(userId)
                .orElseGet(() -> userInkFundRepository.save(UserInkFund.builder().userId(userId).build()));
    }

    // 거래 원장 append
    private void writeTx(Long userId, InkTxType type, int amount, int balanceAfter,
                         String refType, Long refId, String memo) {
        inkTransactionRepository.save(InkTransaction.builder()
                .userId(userId).txType(type).amount(amount).balanceAfter(balanceAfter)
                .refType(refType).refId(refId).memo(memo).build());
    }
}
