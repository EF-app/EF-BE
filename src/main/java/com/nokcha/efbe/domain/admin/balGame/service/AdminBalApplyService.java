package com.nokcha.efbe.domain.admin.balGame.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.balGame.dto.request.AdminBalApplyRejectReqDto;
import com.nokcha.efbe.domain.balGame.dto.response.BalApplyRspDto;
import com.nokcha.efbe.domain.balGame.entity.BalApply;
import com.nokcha.efbe.domain.balGame.entity.BalApplyStatus;
import com.nokcha.efbe.domain.balGame.repository.BalApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminBalApplyService {

    private final BalApplyRepository balApplyRepository;

    // 밸런스 게임 신청 목록
    @Transactional(readOnly = true)
    public Page<BalApplyRspDto> getApplies(BalApplyStatus statusFilter, Pageable pageable) {
        Page<BalApply> page = (statusFilter == null)
                ? balApplyRepository.findAll(pageable)
                : balApplyRepository.findByStatusOrderByCreateTimeDesc(statusFilter, pageable);
        return page.map(BalApplyRspDto::from);
    }

    // 밸런스 게임 신청 거절
    @Transactional
    public BalApplyRspDto rejectApply(Long applyId, AdminBalApplyRejectReqDto req) {
        BalApply apply = balApplyRepository.findById(applyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_APPLY));
        if (apply.getStatus() != BalApplyStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_GAME_STATUS);
        }
        apply.decide(BalApplyStatus.REJECTED, req == null ? null : req.getAdminMemo());
        return BalApplyRspDto.from(apply);
    }
}
