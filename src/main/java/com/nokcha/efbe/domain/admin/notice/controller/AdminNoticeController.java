package com.nokcha.efbe.domain.admin.notice.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.notice.dto.request.NoticeReqDto;
import com.nokcha.efbe.domain.admin.notice.service.AdminNoticeService;
import com.nokcha.efbe.domain.notice.dto.response.NoticeDetailRspDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 관리자 공지사항 관리 API.
@Tag(name = "Admin Notice", description = "관리자 공지사항 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/notices")
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    @Operation(summary = "공지사항 작성", description = "관리자만 공지사항을 작성할 수 있습니다.")
    @PostMapping
    public RspTemplate<NoticeDetailRspDto> createNotice(@Valid @RequestBody NoticeReqDto reqDto) {
        return new RspTemplate<>(
                HttpStatus.CREATED,
                "공지사항 작성이 완료되었습니다.",
                adminNoticeService.createNotice(reqDto)
        );
    }

    @Operation(summary = "공지사항 수정", description = "관리자만 공지사항을 수정할 수 있습니다.")
    @PatchMapping("/{noticeId}")
    public RspTemplate<NoticeDetailRspDto> updateNotice(@PathVariable Long noticeId,
                                                       @Valid @RequestBody NoticeReqDto reqDto) {
        return new RspTemplate<>(
                HttpStatus.OK,
                "공지사항 수정이 완료되었습니다.",
                adminNoticeService.updateNotice(noticeId, reqDto)
        );
    }

    @Operation(summary = "공지사항 삭제", description = "관리자만 공지사항을 삭제할 수 있습니다.")
    @DeleteMapping("/{noticeId}")
    public RspTemplate<Void> deleteNotice(@PathVariable Long noticeId) {
        adminNoticeService.deleteNotice(noticeId);
        return new RspTemplate<>(HttpStatus.OK, "공지사항 삭제가 완료되었습니다.");
    }
}
