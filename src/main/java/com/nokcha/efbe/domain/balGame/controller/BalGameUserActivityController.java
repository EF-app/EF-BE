package com.nokcha.efbe.domain.balGame.controller;

import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.balGame.dto.response.BalGameUserActivityEntryRspDto;
import com.nokcha.efbe.domain.balGame.service.BalGameUserActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 밸런스 게임 — 내 활동 RESTful 컨트롤러 (마이페이지)
@Tag(name = "BalGame UserActivity", description = "밸런스 게임 — 내 활동 (마이페이지 — 내가 투표한 게임 목록)")
@RestController
@RequestMapping("/v1/bal-game/my")
@RequiredArgsConstructor
public class BalGameUserActivityController {

    private final BalGameUserActivityService balGameUserActivityService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "내가 투표한 밸런스게임 목록",
            description = "현재 로그인 유저가 투표한 밸런스게임 목록을 커서 페이지네이션으로 반환합니다. " +
                    "정렬: 내가 투표한 시각 DESC, voteId DESC (안정 정렬). " +
                    "게임 상태가 PUBLISHED 또는 ARCHIVED 인 항목만 노출 (DRAFT/HIDDEN 제외). " +
                    "각 카드에는 게임 본문(option A/B + 이모지), 내 투표 결과(myChoice), 양 옵션 투표수(aCount/bCount/totalCount=aCount+bCount), 댓글수가 포함됩니다. " +
                    "카드 클릭 시 기존 댓글창 API (/v1/bal-game/{gameId}/comments) 로 진입합니다.")
    @GetMapping
    public ResponseEntity<RspTemplate<CursorPageResponse<BalGameUserActivityEntryRspDto>>> getMyVotedGames(
            @Parameter(description = "이전 응답의 nextCursor — 첫 페이지는 비워서 호출")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "한 번에 가져올 카드 수 (기본 20, 최대 50)")
            @RequestParam(required = false) Integer size) {
        Long userId = securityUtil.getCurrentUserId();
        CursorPageResponse<BalGameUserActivityEntryRspDto> data =
                balGameUserActivityService.getMyVotedGames(userId, cursor, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "내가 투표한 밸런스게임 목록 조회 성공", data));
    }
}
