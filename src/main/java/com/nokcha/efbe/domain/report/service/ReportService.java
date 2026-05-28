package com.nokcha.efbe.domain.report.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import com.nokcha.efbe.domain.balGame.repository.BalGameCommentRepository;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.repository.PostItRepository;
import com.nokcha.efbe.domain.report.dto.request.ReportCreateReqDto;
import com.nokcha.efbe.domain.report.dto.response.ReportRspDto;
import com.nokcha.efbe.domain.report.entity.Report;
import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import com.nokcha.efbe.domain.report.repository.ReportRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 사용자 신고 등록 서비스.
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostItRepository postItRepository;
    private final BalGameCommentRepository balGameCommentRepository;

    @Transactional
    public ReportRspDto createReport(Long reporterId, ReportCreateReqDto reqDto) {
        // PROFILE 신고는 target_id 가 곧 신고 대상 user.id — 자기 자신 신고 차단.
        if (reqDto.getTargetType() == ReportTargetType.PROFILE
                && reqDto.getTargetId().equals(reporterId)) {
            throw new BusinessException(ErrorCode.SELF_ACTION_FORBIDDEN);
        }

        // 같은 사람이 같은 대상에 중복 신고 차단
        if (reportRepository.existsByTargetTypeAndTargetIdAndReporter_Id(
                reqDto.getTargetType(), reqDto.getTargetId(), reporterId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REPORT);
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        // 다중 선택 사유 코드는 콤마 구분 단일 문자열로 직렬화해 저장 (예: "HATE,SPAM").
        // 255자 컷 안에 들어가도록 trim — enum 코드 길이 합 + 콤마 정도라 일반적으로 안전.
        String joinedCodes = (reqDto.getReasonCodes() == null || reqDto.getReasonCodes().isEmpty())
                ? null
                : String.join(",", reqDto.getReasonCodes());
        if (joinedCodes != null && joinedCodes.length() > 255) {
            joinedCodes = joinedCodes.substring(0, 255);
        }

        Report saved = reportRepository.save(Report.builder()
                .reporter(reporter)
                .targetType(reqDto.getTargetType())
                .targetId(reqDto.getTargetId())
                .reasonCodes(joinedCodes)
                .detail(reqDto.getDetail())
                .build());

        // 신고 누적 카운트  : 임계치(10) 도달 시 자동 hidden 마킹.
        // PROFILE/CHAT/CHAT_IMAGE 는 카운트 컬럼 없음 (현 단계 미지원).
        incrementTargetReportCount(reqDto.getTargetType(), reqDto.getTargetId());

        return ReportRspDto.from(saved);
    }

    private void incrementTargetReportCount(ReportTargetType type, Long targetId) {
        switch (type) {
            case POST_IT -> postItRepository.findById(targetId)
                    .ifPresent(PostIt::increaseReportAndHideIfThreshold);
            case BAL_COMMENT -> balGameCommentRepository.findById(targetId)
                    .ifPresent(BalGameComment::incrementReport);
            case PROFILE, CHAT, CHAT_IMAGE -> { /* 카운트 컬럼 없음 — 미적용 */ }
        }
    }
}
