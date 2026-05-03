package com.nokcha.efbe.domain.balGame.controller;

import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.security.SecurityUtil;
import com.nokcha.efbe.domain.balGame.dto.response.BalGameDetailRspDto;
import com.nokcha.efbe.domain.balGame.dto.response.BalGameSummaryRspDto;
import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.service.BalGameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 밸런스 게임 본문 RESTful 컨트롤러 (사용자 측 — 목록·상세 조회)
@RestController
@RequestMapping("/v1/bal-game")
@RequiredArgsConstructor
public class BalGameController {

    private final BalGameService balGameService;

    // 공개된 밸런스 게임 목록 (커서 기반, 카테고리 옵션)
    @Operation(summary = "밸런스 게임 목록 (커서 페이지네이션)")
    @GetMapping
    public ResponseEntity<RspTemplate<CursorPageResponse<BalGameSummaryRspDto>>> getBalanceGames(
            @RequestParam(required = false) BalCategoryCode categoryCode,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size) {
        CursorPageResponse<BalGameSummaryRspDto> data = balGameService.getBalanceGames(categoryCode, cursor, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "밸런스 게임 목록 조회 성공", data));
    }

    // 홈 화면 — 오늘의 밸런스게임 가로 스와이프용 배치.
    // PUBLISHED 상태, update_time DESC, 각 카드에 댓글 3개 + 내 투표 포함.
    @Operation(summary = "홈 - 오늘의 밸런스게임 (배치)",
            description = "PUBLISHED 게임을 update_time DESC 로 size 개 묶어서 반환. " +
                    "각 카드는 BalGameDetailRspDto 와 동일 모양 (이모지·내 투표·최신 댓글 3개 포함). 비로그인 시 myChoice=null.")
    @GetMapping("/home")
    public ResponseEntity<RspTemplate<List<BalGameDetailRspDto>>> getHomeFeed(
            @Parameter(description = "한 번에 가져올 카드 수 (기본 5, 최대 20)")
            @RequestParam(required = false) Integer size) {
        Long viewerId = SecurityUtil.getCurrentUserIdOrSystem();
        List<BalGameDetailRspDto> data = balGameService.getHomeFeed(size, viewerId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "홈 밸런스 게임 조회 성공", data));
    }

    // 단건 상세 조회
    @Operation(summary = "밸런스 게임 단건 상세")
    @GetMapping("/{gameId}")
    public ResponseEntity<RspTemplate<BalGameDetailRspDto>> getOneBalanceGame(@PathVariable Long gameId) {
        Long viewerId = SecurityUtil.getCurrentUserIdOrSystem();
        BalGameDetailRspDto data = balGameService.getOneBalanceGame(gameId, viewerId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "밸런스 게임 상세 조회 성공", data));
    }
}
