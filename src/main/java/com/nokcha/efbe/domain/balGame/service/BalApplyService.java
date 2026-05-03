package com.nokcha.efbe.domain.balGame.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.balGame.dto.request.BalApplyCreateReqDto;
import com.nokcha.efbe.domain.balGame.dto.request.BalApplyDecisionReqDto;
import com.nokcha.efbe.domain.balGame.dto.response.BalApplyRspDto;
import com.nokcha.efbe.domain.balGame.dto.response.BalGameSummaryRspDto;
import com.nokcha.efbe.domain.balGame.entity.BalApply;
import com.nokcha.efbe.domain.balGame.entity.BalApplyStatus;
import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.repository.BalApplyRepository;
import com.nokcha.efbe.domain.balGame.repository.BalGameRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 밸런스 게임 신청/승인 서비스
@Service
@RequiredArgsConstructor
public class BalApplyService {

    private final BalApplyRepository balApplyRepository;
    private final BalGameRepository balGameRepository;
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

    // 관리자: 신청 목록 (최신순)
    @Transactional(readOnly = true)
    public Page<BalApplyRspDto> getApplies(BalApplyStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BalApply> result = (status == null)
                ? balApplyRepository.findAll(pageable)
                : balApplyRepository.findByStatusOrderByCreateTimeDesc(status, pageable);
        return result.map(BalApplyRspDto::from);
    }

    // 유저: 내 신청 목록 (최신순)
    @Transactional(readOnly = true)
    public Page<BalApplyRspDto> getMyApplies(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return balApplyRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable)
                .map(BalApplyRspDto::from);
    }

    // 관리자: 승인/반려. 승인 시 bal_game 테이블에 DRAFT 상태로 등록
    @Transactional
    public BalGameSummaryRspDto decideApply(Long applyId, BalApplyDecisionReqDto req) {
        BalApply apply = balApplyRepository.findById(applyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_APPLY));
        if (apply.getStatus() != BalApplyStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_GAME_STATUS);
        }

        apply.decide(req.getStatus(), req.getAdminMemo());
        if (req.getStatus() == BalApplyStatus.APPROVED) {
            BalGame game = BalGame.builder()
                    .uuid(java.util.UUID.randomUUID().toString())
                    .optionA(apply.getOptionA())
                    .optionB(apply.getOptionB())
                    .optionAEmoji(apply.getOptionAEmoji())
                    .optionBEmoji(apply.getOptionBEmoji())
                    .description(apply.getDescription())
                    .categoryCode(apply.getCategoryCode())
                    .status(BalGameStatus.DRAFT)
                    .applicant(apply.getUser())
                    .build();
            return BalGameSummaryRspDto.from(balGameRepository.save(game));
        }
        return null;
    }
}
