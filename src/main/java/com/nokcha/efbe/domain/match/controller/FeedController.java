package com.nokcha.efbe.domain.match.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.match.dto.response.FeedCardRspDto;
import com.nokcha.efbe.domain.match.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Match Feed", description = "현재 피드 조회 API")
@RestController
@RequestMapping("/v1/matches/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "현재 피드 조회",
            description = "현재 노출용 50명 (매일 04:00 배치로 교체). 정지/탈퇴/미승인/차단 대상은 read-time 자동 제외.")
    @GetMapping
    public RspTemplate<List<FeedCardRspDto>> getCurrentFeed() {
        Long viewerId = securityUtil.getCurrentUserId();
        List<FeedCardRspDto> data = feedService.getCurrentFeed(viewerId);
        return new RspTemplate<>(HttpStatus.OK, "피드 조회 성공", data);
    }
}
