package com.nokcha.efbe.domain.admin.suspension.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.admin.suspension.dto.request.AdminSuspensionCreateReqDto;
import com.nokcha.efbe.domain.admin.suspension.dto.request.AdminSuspensionLiftReqDto;
import com.nokcha.efbe.domain.admin.suspension.dto.response.AdminSuspensionRspDto;
import com.nokcha.efbe.domain.admin.suspension.service.AdminSuspensionService;
import com.nokcha.efbe.domain.suspension.entity.UserSuspension;
import com.nokcha.efbe.domain.suspension.entity.SuspensionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "Admin Suspension", description = "관리자 제재 부과/해제/조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin")
public class AdminSuspensionController {

    private final AdminSuspensionService adminSuspensionService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "유저 제재 부과",
            description = "body.targetUserId 로 대상 지정. type=WARNING/TEMPORARY/PERMANENT. " +
                    "TEMPORARY 면 durationDays 필수. " +
                    "sourceTargetType/Id 는 신고 그룹 기반 제재 시 채움, 임의 제재 시 null. " +
                    "WARNING 부과 시 최근 30일 5회 누적되면 자동 일시정지 에스컬레이션이 일어남")
    @PostMapping("/suspensions")
    public RspTemplate<AdminSuspensionRspDto> createSuspension(
            @Valid @RequestBody AdminSuspensionCreateReqDto req) {
        UserSuspension suspension = adminSuspensionService.applySuspension(req.getTargetUserId(), req.getType(), req.getReason(), req.getDurationDays(), req.getSourceTargetType(), req.getSourceTargetId());
        return new RspTemplate<>(HttpStatus.CREATED, "제재가 부과되었습니다.", adminSuspensionService.toDto(suspension));
    }

    @Operation(summary = "제재 수동 해제",
            description = "활성 제재를 만료 전에 일찍 해제. is_lifted=true + 해제 정보 기록")
    @PatchMapping("/suspensions/{suspensionId}/lift")
    public RspTemplate<AdminSuspensionRspDto> liftSuspension(
            @PathVariable Long suspensionId,
            @Valid @RequestBody AdminSuspensionLiftReqDto req) {
        Long adminId = securityUtil.getCurrentUserId();
        UserSuspension suspension = adminSuspensionService.liftSuspension(suspensionId, adminId, req.getLiftedReason());
        return new RspTemplate<>(HttpStatus.OK, "제재가 해제되었습니다.", adminSuspensionService.toDto(suspension));
    }

    @Operation(summary = "특정 유저의 활성 차단 제재 일괄 해제",
            description = "유저의 활성 TEMPORARY/PERMANENT 제재를 한 번에 모두 해제. WARNING(옐로카드 누적)은 대상 제외")
    @PatchMapping("/users/{userId}/suspensions/lift-all")
    public RspTemplate<java.util.List<AdminSuspensionRspDto>> liftAllForUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminSuspensionLiftReqDto req) {
        Long adminId = securityUtil.getCurrentUserId();
        java.util.List<UserSuspension> lifted = adminSuspensionService.liftAllBlockingByUserId(
                userId, adminId, req.getLiftedReason());
        java.util.List<AdminSuspensionRspDto> dtos = adminSuspensionService.toDtoList(lifted);
        return new RspTemplate<>(HttpStatus.OK,
                String.format("활성 제재 %d건이 일괄 해제되었습니다.", lifted.size()), dtos);
    }

    @Operation(summary = "전체 제재 목록",
            description = "특정 유저의 제재 이력은 userId 쿼리 파라미터로 조회.")
    @GetMapping("/suspensions")
    public RspTemplate<Page<AdminSuspensionRspDto>> getSuspensions(
            @Parameter(description = "유저 PK 필터") @RequestParam(required = false) Long userId,
            @Parameter(description = "유저 닉네임/UUID LIKE 키워드 필터")
            @RequestParam(required = false) String userKeyword,
            @Parameter(description = "제재 유형 필터") @RequestParam(required = false) SuspensionType type,
            @Parameter(description = "수동 해제 여부 필터") @RequestParam(required = false) Boolean isLifted,
            @Parameter(description = "부과 시각 시작 (ISO LocalDateTime)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "부과 시각 종료 (ISO LocalDateTime)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AdminSuspensionRspDto> data = adminSuspensionService.toDtoPage(
                adminSuspensionService.searchForAdmin(userId, userKeyword, type, isLifted, from, to, pageable));
        return new RspTemplate<>(HttpStatus.OK, "제재 목록을 조회했습니다.", data);
    }

    @Operation(summary = "제재 단건 상세",
            description = "제재 row PK 로 단건 조회. 활성/자동만료/수동해제 등 모든 라이프사이클 정보 포함.")
    @GetMapping("/suspensions/{suspensionId}")
    public RspTemplate<AdminSuspensionRspDto> getSuspension(@PathVariable Long suspensionId) {
        UserSuspension suspension = adminSuspensionService.getSuspension(suspensionId);
        return new RspTemplate<>(HttpStatus.OK, "제재 정보를 조회했습니다.", adminSuspensionService.toDto(suspension));
    }
}
