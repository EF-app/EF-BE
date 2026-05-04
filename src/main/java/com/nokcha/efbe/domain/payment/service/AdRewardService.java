package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.dto.request.AdRewardReqDto;
import com.nokcha.efbe.domain.payment.entity.AdRewardLog;
import com.nokcha.efbe.domain.payment.repository.AdRewardLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

// 광고 보상 서비스 (ad_tx_id 기반 멱등 처리)
@Service
@RequiredArgsConstructor
public class AdRewardService {

    private static final int DAILY_MAX_PER_TYPE = 5;

    private final AdRewardLogRepository adRewardLogRepository;
    private final StarService starService;

    // 광고 보상 수령 (별 보상만 처리 - 티켓 등 타입은 확장 지점)
    @Transactional
    public void claim(Long userId, AdRewardReqDto req) {
        if (adRewardLogRepository.existsByAdTxId(req.getAdTxId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_AD_REWARD);
        }
        long todayCount = adRewardLogRepository.countByUserIdAndRewardDateAndRewardType(
                userId, LocalDate.now(), req.getRewardType());
        if (todayCount >= DAILY_MAX_PER_TYPE) {
            throw new BusinessException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }
        try {
            adRewardLogRepository.save(AdRewardLog.builder()
                    .userId(userId).rewardType(req.getRewardType()).rewardAmount(req.getRewardAmount())
                    .rewardDate(LocalDate.now()).adNetwork(req.getAdNetwork()).adTxId(req.getAdTxId())
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_AD_REWARD, e);
        }

        // 별 보상 타입은 별 적립
        if ("AD_STAR".equals(req.getRewardType())) {
            starService.charge(userId, req.getRewardAmount(), "AD_REWARD", null,
                    "ad-tx:" + req.getAdTxId());
        }
    }
}
