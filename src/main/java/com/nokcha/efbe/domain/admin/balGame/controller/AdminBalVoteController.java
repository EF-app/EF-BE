package com.nokcha.efbe.domain.admin.balGame.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.balGame.dto.response.AdminBalVoteRspDto;
import com.nokcha.efbe.domain.admin.balGame.service.AdminBalVoteService;
import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 어드민 측 BalVote (개별 투표자) 조회 API.
@Tag(name = "Admin BalVote", description = "관리자 밸런스 게임 투표자 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/bal-game")
public class AdminBalVoteController {

    private final AdminBalVoteService adminBalVoteService;

    @Operation(summary = "투표자 목록 조회",
            description = "한 게임의 개별 투표자 목록 (페이지네이션). choice 옵션 필터. " +
                    "정렬은 createTime DESC, id DESC 고정 (Repository Querydsl).")
    @GetMapping("/{gameUuid}/votes")
    public RspTemplate<Page<AdminBalVoteRspDto>> getVotes(
            @PathVariable String gameUuid,
            @RequestParam(required = false) BalVoteChoice choice,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        return new RspTemplate<>(HttpStatus.OK, "투표자 목록을 조회했습니다.",
                adminBalVoteService.getVotes(gameUuid, choice, pageable));
    }
}
