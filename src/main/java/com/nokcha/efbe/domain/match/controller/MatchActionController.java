package com.nokcha.efbe.domain.match.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.match.dto.request.MatchActionReqDto;
import com.nokcha.efbe.domain.match.service.MatchActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 매칭 단방향 액션 등록 (LIKE / PASS / SUPER_LIKE / POWER_MESSAGE).
 *  한 페어당 활성 액션 1 개 — 기존 액션은 자동으로 새 액션으로 대체.
 *  PASS 만 30 일 쿨다운, 그 외는 영구 제외.
 */
@Tag(name = "Match Action", description = "매칭 액션 API (좋아요/패스/슈퍼좋아요/파워메시지)")
@RestController
@RequestMapping("/v1/matches/{targetId}/actions")
@RequiredArgsConstructor
public class MatchActionController {

    private final MatchActionService matchActionService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "매칭 액션 등록",
            description = "LIKE/PASS/SUPER_LIKE/POWER_MESSAGE 중 하나를 등록합니다. " +
                    "같은 페어의 이전 액션이 있으면 자동으로 대체됩니다.")
    @PostMapping
    public RspTemplate<Void> createAction(@PathVariable Long targetId,
                                          @Valid @RequestBody MatchActionReqDto req) {
        Long actorId = securityUtil.getCurrentUserId();
        matchActionService.recordAction(actorId, targetId, req.type());
        return new RspTemplate<>(HttpStatus.CREATED, "매칭 액션 등록 성공");
    }
}
