package com.nokcha.efbe.domain.admin.match.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.admin.match.dto.request.AdminMatchConfigUpdateReqDto;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminDailyFeedPageRspDto;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminMatchFullBatchRspDto;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminMatchRecoverBatchRspDto;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminMatchConfigItemRspDto;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminMatchUserBatchRspDto;
import com.nokcha.efbe.domain.admin.match.service.AdminDailyFeedService;
import com.nokcha.efbe.domain.admin.match.service.AdminMatchConfigService;
import com.nokcha.efbe.domain.admin.match.service.AdminMatchService;
import com.nokcha.efbe.domain.match.entity.MatchDailyFeed.SlotType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 매칭 운영 도구 — 관리자 전용.
 */
@Tag(name = "Admin Match", description = "관리자 매칭 운영 도구 (강제 재계산 등)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/matches")
public class AdminMatchController {

    private final AdminMatchService adminMatchService;
    private final AdminMatchConfigService adminMatchConfigService;
    private final AdminDailyFeedService adminDailyFeedService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "특정 유저 피드 재계산",
            description = "어뷰즈 가드 우회")
    @PostMapping("/batch/user/{userId}")
    public RspTemplate<AdminMatchUserBatchRspDto> runUserBatch(@PathVariable Long userId) {
        AdminMatchUserBatchRspDto result = adminMatchService.runUserBatch(userId);
        return new RspTemplate<>(HttpStatus.OK, "피드 재계산 완료", result);
    }

    @Operation(summary = "보정 배치 강제 실행 — 04:00 정상 배치 누락 의심",
            description = "오늘 daily_feed row 가 없는 활성+승인 viewer 만 일괄 복구. " +
                    "05:00 cron 보정과 동일 (ShedLock 우회)")
    @PostMapping("/batch/recover")
    public RspTemplate<AdminMatchRecoverBatchRspDto> runRecoverBatch() {
        AdminMatchRecoverBatchRspDto result = adminMatchService.runRecoverBatch();
        return new RspTemplate<>(HttpStatus.OK, "보정 배치 실행 완료", result);
    }

    @Operation(summary = "전체 정상 배치 강제 실행 — 04:00 cron 과 동일",
            description = "활성 viewer 전체 대상으로 daily_feed 를 계산" +
                    "전체 — 매칭 로직 변경 후 즉시 반영하고 싶을 때 사용")
    @PostMapping("/batch/full")
    public RspTemplate<AdminMatchFullBatchRspDto> runFullBatch() {
        AdminMatchFullBatchRspDto result = adminMatchService.runFullBatch();
        return new RspTemplate<>(HttpStatus.OK, "전체 정상 배치 실행 완료", result);
    }

    @Operation(summary = "매칭 설정값 전체 조회",
            description = "code_match_config 38행 + 각 row 메타 (valueType / description / updatedAt / updatedBy). " +
                    "configKey 알파벳 정렬.")
    @GetMapping("/config")
    public RspTemplate<List<AdminMatchConfigItemRspDto>> getConfig() {
        return new RspTemplate<>(HttpStatus.OK, "매칭 설정값을 조회했습니다.", adminMatchConfigService.getAll());
    }

    @Operation(summary = "매칭 설정값 부분 갱신",
            description = "변경된 entries 만 PATCH. 검증: (1) key 존재, (2) valueType 별 파싱, " +
                    "(3) sortKey 가중치 4개 합 = 1.0 (±0.01). MatchingConfigLoader 가 매 호출 findAll() 이라 즉시 반영.")
    @PatchMapping("/config")
    public RspTemplate<List<AdminMatchConfigItemRspDto>> updateConfig(
            @Valid @RequestBody AdminMatchConfigUpdateReqDto req) {
        String adminIdentifier = "admin:" + securityUtil.getCurrentUserId();
        return new RspTemplate<>(HttpStatus.OK, "매칭 설정값을 갱신했습니다.",
                adminMatchConfigService.update(req, adminIdentifier));
    }

    @Operation(summary = "일일 피드 조회",
            description = "match_daily_feed + users JOIN")
    @GetMapping("/daily-feed")
    public RspTemplate<AdminDailyFeedPageRspDto> getDailyFeed(
            @RequestParam(required = false) Long viewerIdFrom,
            @RequestParam(required = false) Long viewerIdTo,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) LocalDate feedDate,
            @RequestParam(required = false) SlotType slotType,
            @RequestParam(required = false) Short rank,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return new RspTemplate<>(HttpStatus.OK, "일일 피드를 조회했습니다.",
                adminDailyFeedService.search(viewerIdFrom, viewerIdTo, targetId, feedDate, slotType, rank, page, size));
    }
}
