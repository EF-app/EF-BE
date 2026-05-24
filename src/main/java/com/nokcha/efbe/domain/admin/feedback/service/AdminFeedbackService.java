package com.nokcha.efbe.domain.admin.feedback.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import com.nokcha.efbe.domain.admin.feedback.dto.request.AdminFeedbackUpdateReqDto;
import com.nokcha.efbe.domain.admin.feedback.dto.response.AdminFeedbackRspDto;
import com.nokcha.efbe.domain.feedback.entity.Feedback;
import com.nokcha.efbe.domain.feedback.entity.FeedbackCategoryCode;
import com.nokcha.efbe.domain.feedback.entity.FeedbackImage;
import com.nokcha.efbe.domain.feedback.entity.FeedbackStatus;
import com.nokcha.efbe.domain.feedback.entity.FeedbackType;
import com.nokcha.efbe.domain.feedback.repository.FeedbackImageRepository;
import com.nokcha.efbe.domain.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 어드민 피드백 관리 — 목록 / 상세 / 처리.
@Service
@RequiredArgsConstructor
public class AdminFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackImageRepository feedbackImageRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final SecurityUtil securityUtil;

    // 목록 — 동적 필터
    @Transactional(readOnly = true)
    public Page<AdminFeedbackRspDto> getFeedbacks(FeedbackType feedbackType,
                                                  FeedbackStatus status,
                                                  FeedbackCategoryCode categoryCode,
                                                  String keyword,
                                                  Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return feedbackRepository.searchForAdmin(feedbackType, status, categoryCode, kw, pageable)
                .map(f -> AdminFeedbackRspDto.of(f, List.of()));
    }

    // 단건 상세 — 첨부 이미지 포함.
    @Transactional(readOnly = true)
    public AdminFeedbackRspDto getFeedback(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_FEEDBACK));
        List<FeedbackImage> images = feedbackImageRepository.findByFeedbackIdOrderBySortOrderAsc(id);
        return AdminFeedbackRspDto.of(feedback, images);
    }

    // 처리 — 상태/답변/내부메모 갱신 + 담당 관리자를 처리한 관리자로 지정.
    @Transactional
    public AdminFeedbackRspDto updateFeedback(Long id, AdminFeedbackUpdateReqDto req) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_FEEDBACK));

        feedback.applyAdminProcess(req.getStatus(), req.getAdminReply(),
                req.getAdminInternalMemo(), resolveCurrentAdmin());

        List<FeedbackImage> images = feedbackImageRepository.findByFeedbackIdOrderBySortOrderAsc(id);
        return AdminFeedbackRspDto.of(feedback, images);
    }

    // 처리 중인 관리자
    private AdminAccount resolveCurrentAdmin() {
        Long adminId = securityUtil.getCurrentUserIdOrNull();
        if (adminId == null) return null;
        return adminAccountRepository.findById(adminId).orElse(null);
    }
}
