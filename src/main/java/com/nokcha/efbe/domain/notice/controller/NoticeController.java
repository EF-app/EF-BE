package com.nokcha.efbe.domain.notice.controller;

import com.nokcha.efbe.domain.notice.dto.request.NoticeReqDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticeDetailRspDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticePageRspDto;
import com.nokcha.efbe.domain.notice.service.NoticeService;
import com.nokcha.efbe.common.response.RspTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notice", description = "공지사항 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notices")
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 작성
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "공지사항 작성", description = "관리자만 공지사항을 작성할 수 있습니다.")
    @PostMapping
    public RspTemplate<NoticeDetailRspDto> createNotice(
            Authentication authentication,
            @Valid @RequestBody NoticeReqDto reqDto
    ) {
        return new RspTemplate<>(
                HttpStatus.CREATED,
                "공지사항 작성이 완료되었습니다.",
                noticeService.createNotice(authentication.getName(), reqDto)
        );
    }

    // 공지사항 수정
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "공지사항 수정", description = "관리자만 공지사항을 수정할 수 있습니다.")
    @PatchMapping("/{noticeId}")
    public RspTemplate<NoticeDetailRspDto> updateNotice(
            Authentication authentication,
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeReqDto reqDto
    ) {
        return new RspTemplate<>(
                HttpStatus.OK,
                "공지사항 수정이 완료되었습니다.",
                noticeService.updateNotice(authentication.getName(), noticeId, reqDto)
        );
    }

    // 공지사항 삭제
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "공지사항 삭제", description = "관리자만 공지사항을 삭제할 수 있습니다.")
    @DeleteMapping("/{noticeId}")
    public RspTemplate<Void> deleteNotice(Authentication authentication, @PathVariable Long noticeId) {
        noticeService.deleteNotice(authentication.getName(), noticeId);
        return new RspTemplate<>(HttpStatus.OK, "공지사항 삭제가 완료되었습니다.");
    }

    // 공지사항 목록을 조회한다.
    @Operation(summary = "공지사항 목록 조회", description = "일반 유저와 관리자가 공지사항 목록을 조회할 수 있습니다.")
    @GetMapping
    public RspTemplate<NoticePageRspDto> getNotices(@RequestParam(defaultValue = "0") int page) {
        return new RspTemplate<>(HttpStatus.OK, "공지사항 목록 조회가 완료되었습니다.", noticeService.getNotices(page));
    }

    // 공지사항 상세를 조회한다.
    @Operation(summary = "공지사항 상세 조회", description = "일반 유저와 관리자가 공지사항 상세를 조회할 수 있습니다.")
    @GetMapping("/{noticeId}")
    public RspTemplate<NoticeDetailRspDto> getOneNotice(@PathVariable Long noticeId) {
        return new RspTemplate<>(HttpStatus.OK, "공지사항 상세 조회가 완료되었습니다.", noticeService.getOneNotice(noticeId));
    }
}
