package com.nokcha.efbe.domain.balGame.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.balGame.dto.request.BalApplyCreateReqDto;
import com.nokcha.efbe.domain.balGame.dto.response.BalApplyRspDto;
import com.nokcha.efbe.domain.balGame.entity.BalApply;
import com.nokcha.efbe.domain.balGame.entity.BalApplyStatus;
import com.nokcha.efbe.domain.balGame.repository.BalApplyRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 밸런스 게임 신청 서비스 (유저).
@Service
@RequiredArgsConstructor
public class BalApplyService {

    private final BalApplyRepository balApplyRepository;
    private final UserRepository userRepository;

    // 유저: 신청 등록
    @Transactional
    public BalApplyRspDto createApply(Long userId, BalApplyCreateReqDto req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        BalApply apply = BalApply.builder()
                .user(user)
                .optionA(req.getOptionA())
                .optionB(req.getOptionB())
                .optionAEmoji(req.getOptionAEmoji())
                .optionBEmoji(req.getOptionBEmoji())
                .description(req.getDescription())
                .categoryCode(req.getCategoryCode())
                .status(BalApplyStatus.PENDING)
                .build();
        return BalApplyRspDto.from(balApplyRepository.save(apply));
    }


    // 유저: 내 신청 목록 (최신순)
    @Transactional(readOnly = true)
    public Page<BalApplyRspDto> getMyApplies(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return balApplyRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable)
                .map(BalApplyRspDto::from);
    }
}
