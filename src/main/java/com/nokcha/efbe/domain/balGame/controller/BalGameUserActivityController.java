package com.nokcha.efbe.domain.balGame.controller;

import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.balGame.dto.response.BalGameUserActivityEntryRspDto;
import com.nokcha.efbe.domain.balGame.dto.response.BalVoteRspDto;
import com.nokcha.efbe.domain.balGame.service.BalGameUserActivityService;
import com.nokcha.efbe.domain.balGame.service.BalVoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "BalGame UserActivity", description = "밸런스 게임 — 내 활동 (마이페이지 — 내가 투표한 게임 목록)")
@RestController
@RequestMapping("/v1/users/me/bal-games")
@RequiredArgsConstructor
public class BalGameUserActivityController {

    private final BalGameUserActivityService balGameUserActivityService;
    private final BalVoteService balVoteService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "내가 투표한 밸런스게임 목록", description = "현재 로그인 유저가 투표한 밸런스게임 목록을 커서 페이지네이션으로 반환합니다.")
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

    // 내 투표 결과 조회 (% 포함)
    @Operation(summary = "내 투표 결과 조회", description = "% 포함")
    @GetMapping("/{gameId}/votes")
    public RspTemplate<BalVoteRspDto> getMyVote(@PathVariable Long gameId) {
        Long userId = securityUtil.getCurrentUserId();
        BalVoteRspDto data = balVoteService.getMyVote(gameId, userId);
        return new RspTemplate<>(HttpStatus.OK, "내 투표 조회 성공", data);
    }
}
