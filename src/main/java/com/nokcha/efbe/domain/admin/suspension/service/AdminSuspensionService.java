package com.nokcha.efbe.domain.admin.suspension.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import com.nokcha.efbe.domain.admin.suspension.dto.response.AdminSuspensionRspDto;
import com.nokcha.efbe.domain.admin.suspension.repository.AdminSuspensionRepository;
import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import com.nokcha.efbe.domain.suspension.entity.SuspensionType;
import com.nokcha.efbe.domain.suspension.entity.UserSuspension;
import com.nokcha.efbe.domain.suspension.repository.UserSuspensionRepository;
import com.nokcha.efbe.domain.suspension.service.SuspensionService;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 관리자 제재 행위 + 표현.
 *  - 부과/해제/검색/조회 — 관리자 컨트롤러가 호출
 *  - 자동 에스컬레이션 — applySuspension 내부에서 자기 호출
 *  - DTO 변환 + admin_account 이름 enrich
 *
 *  도메인 불변식(users.status 동기화)은 SuspensionService.evaluateAndUpdateStatus 로 위임.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminSuspensionService {

    // 경고 N회 -> 일시정지
    public static final int WARNING_THRESHOLD = 5;
    // 최근 N일 내 WARNING 카운트
    public static final int WARNING_WINDOW_DAYS = 30;
    // WARNING 1건의 유효 기간 , 자동만료되기까지의 일수
    public static final int WARNING_LIFETIME_DAYS = 30;

    // 자동 에스컬레이션 등급 (TEMPORARY 일수)
    public static final int FIRST_TEMP_DAYS = 7;
    public static final int SECOND_TEMP_DAYS = 30;

    private static final String SYSTEM_ADMIN_NAME = "시스템";

    private final UserSuspensionRepository userSuspensionRepository;
    private final AdminSuspensionRepository adminSuspensionRepository;
    private final UserRepository userRepository;
    private final SuspensionService suspensionService;
    private final AdminAccountRepository adminAccountRepository;

    /* ─────────── 부과 / 해제 / 조회 ─────────── */

    public UserSuspension applySuspension(
            Long userId,
            SuspensionType type,
            String reason,
            Integer durationDays,
            ReportTargetType sourceTargetType,
            Long sourceTargetId,
            Long adminId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        validateApplyInput(user, type, reason, durationDays);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endsAt = switch (type) {
            case WARNING -> now.plusDays(WARNING_LIFETIME_DAYS);
            case TEMPORARY -> now.plusDays(durationDays);
            case PERMANENT -> null;
        };

        UserSuspension suspension = UserSuspension.builder()
                .user(user)
                .suspensionType(type)
                .reason(reason)
                .startsAt(now)
                .endsAt(endsAt)
                .sourceTargetType(sourceTargetType)
                .sourceTargetId(sourceTargetId)
                .build();
        userSuspensionRepository.save(suspension);
        suspensionService.evaluateAndUpdateStatus(user);

        // WARNING 부과 직후만 에스컬레이션 검사. TEMPORARY/PERMANENT 부과 시 검사 안 함 (무한 루프 방지).
        if (type == SuspensionType.WARNING) {
            escalateAfterWarningIfNeeded(user, reason);
        }

        return suspension;
    }

    // 단건 수동 해제 — 해당 row UPDATE + users.status update
    public UserSuspension liftSuspension(Long suspensionId, Long adminId, String liftedReason) {
        UserSuspension suspension = userSuspensionRepository.findById(suspensionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SUSPENSION));

        if (Boolean.TRUE.equals(suspension.getIsLifted())) {
            throw new BusinessException(ErrorCode.ALREADY_LIFTED_SUSPENSION);
        }

        suspension.liftManually(adminId, liftedReason);
        suspensionService.evaluateAndUpdateStatus(suspension.getUser());
        return suspension;
    }

    // 특정 유저의 활성 차단 제재 (TEMPORARY/PERMANENT) 일괄 수동 해제.
    public List<UserSuspension> liftAllBlockingByUserId(Long userId, Long adminId, String liftedReason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        List<UserSuspension> active = userSuspensionRepository
                .findActiveByUserId(userId, LocalDateTime.now())
                .stream()
                .filter(s -> s.getSuspensionType() != SuspensionType.WARNING)
                .toList();

        if (active.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_SUSPENSION);
        }

        for (UserSuspension s : active) {
            s.liftManually(adminId, liftedReason);
        }
        suspensionService.evaluateAndUpdateStatus(user);
        return active;
    }

    // 관리자: 단건 상세
    @Transactional(readOnly = true)
    public UserSuspension getSuspension(Long suspensionId) {
        return userSuspensionRepository.findById(suspensionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SUSPENSION));
    }

    // 관리자 전체 제재 목록 — 동적 필터 + 페이지네이션
    @Transactional(readOnly = true)
    public Page<UserSuspension> searchForAdmin(Long userId,
                                               String userKeyword,
                                               SuspensionType type,
                                               Boolean isLifted,
                                               LocalDateTime from,
                                               LocalDateTime to,
                                               Pageable pageable) {
        String keyword = (userKeyword == null || userKeyword.isBlank()) ? null : userKeyword;
        return adminSuspensionRepository.searchForAdmin(userId, keyword, type, isLifted, from, to, pageable);
    }

    // 단건 변환 — 부과/해제/상세
    @Transactional(readOnly = true)
    public AdminSuspensionRspDto toDto(UserSuspension suspension) {
        Map<Long, String> nameMap = loadAdminNameMap(collectAdminIds(List.of(suspension)));
        return build(suspension, nameMap);
    }

    // 페이지 변환 — 목록 페이지네이션 응답
    @Transactional(readOnly = true)
    public Page<AdminSuspensionRspDto> toDtoPage(Page<UserSuspension> page) {
        Map<Long, String> nameMap = loadAdminNameMap(collectAdminIds(page.getContent()));
        return page.map(s -> build(s, nameMap));
    }

    // 리스트 변환 — 유저 상세 인라인 + 일괄 해제 응답
    @Transactional(readOnly = true)
    public List<AdminSuspensionRspDto> toDtoList(List<UserSuspension> suspensions) {
        Map<Long, String> nameMap = loadAdminNameMap(collectAdminIds(suspensions));
        return suspensions.stream().map(s -> build(s, nameMap)).toList();
    }

    private void validateApplyInput(User user, SuspensionType type, String reason, Integer durationDays) {
        if (type == null || reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_SUSPENSION_REQUEST);
        }
        if (type == SuspensionType.TEMPORARY && (durationDays == null || durationDays <= 0)) {
            throw new BusinessException(ErrorCode.INVALID_SUSPENSION_REQUEST);
        }
        if (user.getStatus() == UserStatus.PERMANENT) {
            throw new BusinessException(ErrorCode.INVALID_SUSPENSION_REQUEST);
        }
        if (user.getStatus() == UserStatus.WITHDRAWING || user.getStatus() == UserStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.INVALID_SUSPENSION_REQUEST);
        }
    }

    // WARNING 부과 직후 호출.
    // 최근 30일 내 WARNING 카운트가 임계치(5) 이상이면 자동 TEMPORARY 부과.
    // 직전 TEMPORARY 등급에 따라 7/30/PERMANENT 결정
    private void escalateAfterWarningIfNeeded(User user, String triggerReason) {
        LocalDateTime since = LocalDateTime.now().minusDays(WARNING_WINDOW_DAYS);
        long warnings = adminSuspensionRepository.countRecentWarnings(user.getId(), since);
        if (warnings < WARNING_THRESHOLD) return;

        List<UserSuspension> latestTemp = adminSuspensionRepository
                .findLatestTemporaryByUserId(user.getId(), PageRequest.of(0, 1));

        if (latestTemp.isEmpty()) {
            applyAutoTemporary(user, FIRST_TEMP_DAYS, triggerReason, warnings);
            return;
        }

        UserSuspension last = latestTemp.get(0);
        long lastDurationDays = (last.getEndsAt() == null)
                ? 0L
                : ChronoUnit.DAYS.between(last.getStartsAt(), last.getEndsAt());

        if (lastDurationDays <= FIRST_TEMP_DAYS) {
            applyAutoTemporary(user, SECOND_TEMP_DAYS, triggerReason, warnings);
        } else {
            applyAutoPermanent(user, triggerReason, warnings);
        }
    }

    private void applyAutoTemporary(User user, int days, String triggerReason, long warnings) {
        String reason = String.format(
                "최근 %d일 경고 %d회 누적으로 자동 %d일 일시정지 (직전 사유: %s)",
                WARNING_WINDOW_DAYS, warnings, days, triggerReason);
        applySuspension(user.getId(), SuspensionType.TEMPORARY, reason, days, null, null, null);
        log.info("자동 에스컬레이션: userId={} → TEMPORARY {}일", user.getId(), days);
    }

    private void applyAutoPermanent(User user, String triggerReason, long warnings) {
        String reason = String.format(
                "최근 %d일 경고 %d회 누적 + 직전 30일 정지 이력으로 자동 영구정지 (직전 사유: %s)",
                WARNING_WINDOW_DAYS, warnings, triggerReason);
        applySuspension(user.getId(), SuspensionType.PERMANENT, reason, null, null, null, null);
        log.info("자동 에스컬레이션: userId={} → PERMANENT", user.getId());
    }

    private AdminSuspensionRspDto build(UserSuspension s, Map<Long, String> nameMap) {
        return AdminSuspensionRspDto.from(s,
                resolveAdminName(s.getCreateUser(), nameMap),
                resolveAdminName(s.getLiftedByAdminId(), nameMap));
    }

    private Set<Long> collectAdminIds(List<UserSuspension> suspensions) {
        Set<Long> ids = new HashSet<>();
        for (UserSuspension s : suspensions) {
            if (s.getCreateUser() != null && s.getCreateUser() > 0) ids.add(s.getCreateUser());
            if (s.getLiftedByAdminId() != null) ids.add(s.getLiftedByAdminId());
        }
        return ids;
    }

    private Map<Long, String> loadAdminNameMap(Set<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return adminAccountRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(AdminAccount::getId, AdminAccount::getName, (a, b) -> a));
    }

    private String resolveAdminName(Long adminId, Map<Long, String> nameMap) {
        if (adminId == null) return null;
        if (adminId == 0L) return SYSTEM_ADMIN_NAME;
        return nameMap.get(adminId);
    }
}
