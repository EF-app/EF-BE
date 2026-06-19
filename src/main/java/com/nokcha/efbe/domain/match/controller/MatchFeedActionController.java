package com.nokcha.efbe.domain.match.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.match.dto.request.MatchActionReqDto;
import com.nokcha.efbe.domain.match.dto.response.MatchFeedActionResultRspDto;
import com.nokcha.efbe.domain.match.service.MatchFeedActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 매칭피드 (HI 탭) 카드 액션 — LIKE / PASS / SUPER_LIKE / POWER_MESSAGE + 되돌리기.
 *  한 페어당 활성 액션 1 개. PASS 만 30 일 쿨다운, 그 외는 영구 제외.
 */
@Tag(name = "Match Feed Action", description = "매칭피드 카드 액션 (좋아요/패스/슈퍼좋아요/파워메시지)")
@RestController
@RequestMapping("/v1/matches/{targetId}/actions")
@RequiredArgsConstructor
public class MatchFeedActionController {

    private final MatchFeedActionService matchFeedActionService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "매칭피드 카드 액션 등록",
            description = "LIKE/PASS/SUPER_LIKE/POWER_MESSAGE 중 하나를 등록합니다. " +
                    "같은 페어의 이전 액션이 있으면 자동으로 대체됩니다. " +
                    "응답의 isMatched=true 이면 양방향 매칭 성사 (클라이언트가 popup 표시 + 서로 좋아요 화면 라우팅).")
    @PostMapping
    public RspTemplate<MatchFeedActionResultRspDto> createAction(@PathVariable Long targetId,
                                                              @Valid @RequestBody MatchActionReqDto req) {
        Long actorId = securityUtil.getCurrentUserId();
        MatchFeedActionResultRspDto result = matchFeedActionService.recordAction(actorId, targetId, req.getType());
        return new RspTemplate<>(HttpStatus.CREATED, "매칭 액션 등록 성공", result);
    }

    @Operation(summary = "매칭피드 액션 되돌리기",
            description = "actor → target 페어의 액션 row 삭제. 현 정책: PASS 만 허용 " +
                    "(LIKE/SUPER_LIKE/POWER_MESSAGE 는 매칭 popup·별 차감·채팅방 등 부수 효과로 단순 DELETE 불가).")
    @DeleteMapping
    public RspTemplate<Void> undoAction(@PathVariable Long targetId) {
        Long actorId = securityUtil.getCurrentUserId();
        matchFeedActionService.undoAction(actorId, targetId);
        return new RspTemplate<>(HttpStatus.OK, "매칭 액션 되돌리기 성공");
    }
}
