package com.nokcha.efbe.domain.admin.report.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import com.nokcha.efbe.domain.admin.report.dto.request.AdminReportProcessReqDto;
import com.nokcha.efbe.domain.admin.report.dto.response.AdminReportDetailRspDto;
import com.nokcha.efbe.domain.admin.report.dto.response.AdminReportGroupKey;
import com.nokcha.efbe.domain.admin.report.dto.response.AdminReportGroupRspDto;
import com.nokcha.efbe.domain.admin.report.dto.response.AdminReportSummaryRspDto;
import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import com.nokcha.efbe.domain.balGame.repository.BalGameCommentRepository;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.repository.PostItRepository;
import com.nokcha.efbe.domain.report.entity.Report;
import com.nokcha.efbe.domain.report.entity.ReportStatus;
import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import com.nokcha.efbe.domain.report.repository.ReportRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


// 처리 정책 — "자동 첫 신고 대표":
//   - PROCESSED: 관리자가 클릭한 reportId 와 무관하게 같은 target 의 가장 오래된 PENDING 이 자동 대표.
//                나머지 PENDING 은 cascade — suspensionId 미연결, parent 만 채움.
//   - DISMISSED: 단건만 처리 (일괄 X).
//
// 목록
//   - getReportsGrouped(): (target_type, target_id) 단위 그룹 목록 + enrich (신고자/대상자 닉네임, 콘텐츠 미리보기)
@Service
@RequiredArgsConstructor
public class AdminReportService {

    private static final int PREVIEW_MAX_LENGTH = 120;

    private final ReportRepository reportRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final UserRepository userRepository;
    private final PostItRepository postItRepository;
    private final BalGameCommentRepository balGameCommentRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public Page<AdminReportSummaryRspDto> getReports(ReportStatus statusFilter, Pageable pageable) {
        Page<Report> page = (statusFilter == null)
                ? reportRepository.findAll(pageable)
                : reportRepository.findAllByStatus(statusFilter, pageable);
        return page.map(AdminReportSummaryRspDto::from);
    }

    // 그룹화 목록 — (target_type, target_id) 단위. 첫 신고 오래된 순 정렬 + enrich.
    @Transactional(readOnly = true)
    public Page<AdminReportGroupRspDto> getReportsGrouped(ReportStatus statusFilter, Pageable pageable) {
        Page<AdminReportGroupKey> keyPage = reportRepository.findGroupKeys(statusFilter, pageable);

        // 1) 페이지 내 모든 그룹의 신고 preload (페이지 size 만큼 쿼리).
        Map<String, List<Report>> reportsByGroup = new LinkedHashMap<>();
        for (AdminReportGroupKey key : keyPage.getContent()) {
            reportsByGroup.put(
                    groupKeyOf(key.targetType(), key.targetId()),
                    reportRepository.findAllByTargetTypeAndTargetIdOrderByCreateTimeAsc(
                            key.targetType(), key.targetId()));
        }

        // 2) enrich context 빌드 — target_type 별 batch fetch (페이지 size 무관, 최대 3쿼리).
        List<Report> allReports = reportsByGroup.values().stream()
                .flatMap(List::stream)
                .toList();
        ReportEnrichContext ctx = buildEnrichContext(allReports);

        // 3) 그룹 별 매핑.
        return keyPage.map(key -> {
            List<Report> reports = reportsByGroup.get(groupKeyOf(key.targetType(), key.targetId()));
            List<AdminReportSummaryRspDto> dtos = reports.stream()
                    .map(r -> enrichSummary(r, ctx))
                    .toList();
            return AdminReportGroupRspDto.of(key, dtos);
        });
    }

    @Transactional(readOnly = true)
    public AdminReportDetailRspDto getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REPORT));
        return toDetailWithFallback(report);
    }

    // 처리 — 자동 첫 신고 대표 정책
    @Transactional
    public AdminReportDetailRspDto processReport(Long reportId, AdminReportProcessReqDto reqDto) {
        Report clicked = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REPORT));

        if (clicked.getStatus() != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }

        AdminAccount admin = loadCurrentAdmin();

        List<Report> pendings = reportRepository.findAllByTargetTypeAndTargetIdAndStatusOrderByCreateTimeAsc(
                clicked.getTargetType(), clicked.getTargetId(), ReportStatus.PENDING);

        Report representative = pendings.get(0);
        representative.process(admin, reqDto.getSuspensionId());

        pendings.stream()
                .skip(1)
                .forEach(r -> r.processAsCascade(admin, representative));

        return AdminReportDetailRspDto.from(representative);
    }

    @Transactional
    public AdminReportDetailRspDto dismissReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REPORT));

        AdminAccount admin = loadCurrentAdmin();
        report.dismiss(admin);
        return AdminReportDetailRspDto.from(report);
    }

    private AdminAccount loadCurrentAdmin() {
        // 통합 인증 패턴 — admin 도 JWT subject 가 admin.id 라 SecurityUtil.getCurrentUserId() 로 가져옴.
        Long adminId = securityUtil.getCurrentUserId();
        return adminAccountRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
    }

    private AdminReportDetailRspDto toDetailWithFallback(Report report) {
        Report parent = report.getParent();
        if (parent == null) {
            return AdminReportDetailRspDto.from(report);
        }
        return AdminReportDetailRspDto.from(report, parent.getSuspensionId(), parent.getId());
    }

    // ─────────────────────────── enrich ───────────────────────────

    // 그룹 내 신고들의 target_type 별 id 를 모아 batch fetch.
    // CHAT / CHAT_IMAGE 는 현 단계 미지원 — enrich 시 빈 처리.
    private ReportEnrichContext buildEnrichContext(List<Report> reports) {
        if (reports.isEmpty()) return ReportEnrichContext.empty();

        Set<Long> postItIds = collectTargetIds(reports, ReportTargetType.POST_IT);
        Set<Long> balCommentIds = collectTargetIds(reports, ReportTargetType.BAL_COMMENT);
        Set<Long> profileUserIds = collectTargetIds(reports, ReportTargetType.PROFILE);

        Map<Long, PostIt> postItMap = batchFetch(postItIds, postItRepository::findAllById, PostIt::getId);
        Map<Long, BalGameComment> balCommentMap = batchFetch(balCommentIds, balGameCommentRepository::findAllById, BalGameComment::getId);

        // 통합 user id 수집: reporter + POST_IT 작성자 + BAL_COMMENT 작성자 + PROFILE 본인.
        Set<Long> userIds = new HashSet<>();
        reports.forEach(r -> {
            if (r.getReporter() != null && r.getReporter().getId() != null) {
                userIds.add(r.getReporter().getId());
            }
        });
        postItMap.values().forEach(p -> userIds.add(p.getUser().getId()));
        balCommentMap.values().forEach(c -> userIds.add(c.getUser().getId()));
        userIds.addAll(profileUserIds);

        Map<Long, User> userMap = batchFetch(userIds, userRepository::findAllById, User::getId);

        return new ReportEnrichContext(userMap, postItMap, balCommentMap);
    }

    private Set<Long> collectTargetIds(List<Report> reports, ReportTargetType type) {
        return reports.stream()
                .filter(r -> r.getTargetType() == type)
                .map(Report::getTargetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private <T> Map<Long, T> batchFetch(Set<Long> ids,
                                         Function<Iterable<Long>, Iterable<T>> fetcher,
                                         Function<T, Long> idExtractor) {
        if (ids.isEmpty()) return Map.of();
        return StreamSupport.stream(fetcher.apply(ids).spliterator(), false)
                .collect(Collectors.toMap(idExtractor, Function.identity()));
    }

    // 신고 한 건을 enrich 된 SummaryDto 로 변환.
    private AdminReportSummaryRspDto enrichSummary(Report report, ReportEnrichContext ctx) {
        Long reporterId = report.getReporter() != null ? report.getReporter().getId() : null;
        String reporterNickname = lookupNickname(ctx, reporterId);

        Long targetUserId = null;
        Long balGameId = null;
        String targetPreview = null;

        switch (report.getTargetType()) {
            case POST_IT -> {
                PostIt p = ctx.postIts().get(report.getTargetId());
                if (p != null) {
                    targetUserId = p.getUser().getId();
                    targetPreview = truncate(p.resolveDisplayContent());
                }
            }
            case BAL_COMMENT -> {
                BalGameComment c = ctx.balComments().get(report.getTargetId());
                if (c != null) {
                    targetUserId = c.getUser().getId();
                    balGameId = c.getGame().getId();
                    targetPreview = truncate(c.resolveDisplayContent());
                }
            }
            case PROFILE -> targetUserId = report.getTargetId();
            case CHAT, CHAT_IMAGE -> {
                // 현 단계 미지원
            }
        }

        String targetUserNickname = lookupNickname(ctx, targetUserId);

        return AdminReportSummaryRspDto.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .status(report.getStatus())
                .reporterId(reporterId)
                .createTime(report.getCreateTime())
                .reporterNickname(reporterNickname)
                .targetUserId(targetUserId)
                .targetUserNickname(targetUserNickname)
                .balGameId(balGameId)
                .targetPreview(targetPreview)
                .build();
    }

    private String lookupNickname(ReportEnrichContext ctx, Long userId) {
        if (userId == null) return null;
        return Optional.ofNullable(ctx.users().get(userId)).map(User::getNickname).orElse(null);
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() <= PREVIEW_MAX_LENGTH ? s : s.substring(0, PREVIEW_MAX_LENGTH);
    }

    private String groupKeyOf(ReportTargetType targetType, Long targetId) {
        return targetType.name() + "-" + targetId;
    }
}
