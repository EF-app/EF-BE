package com.nokcha.efbe.domain.notice.controller;

import com.nokcha.efbe.domain.notice.dto.response.NoticeDetailRspDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticePageRspDto;
import com.nokcha.efbe.domain.notice.entity.NoticeCategory;
import com.nokcha.efbe.domain.notice.service.NoticeService;
import com.nokcha.efbe.common.response.RspTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notice", description = "공지사항 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notices")
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 목록 조회
    @Operation(summary = "공지사항 목록 조회", description = "일반 유저와 관리자가 공지사항 목록을 조회할 수 있습니다.")
    @GetMapping
    public RspTemplate<NoticePageRspDto> getNotices(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(required = false) NoticeCategory category) {
        return new RspTemplate<>(HttpStatus.OK, "공지사항 목록 조회가 완료되었습니다.", noticeService.getNotices(page, category));
    }

    // 공지사항 상세 조회
    @Operation(summary = "공지사항 상세 조회", description = "일반 유저와 관리자가 공지사항 상세를 조회할 수 있습니다.")
    @GetMapping("/{noticeId}")
    public RspTemplate<NoticeDetailRspDto> getOneNotice(@PathVariable Long noticeId) {
        return new RspTemplate<>(HttpStatus.OK, "공지사항 상세 조회가 완료되었습니다.", noticeService.getOneNotice(noticeId));
    }
}
