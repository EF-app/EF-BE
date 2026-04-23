package com.nokcha.efbe.domain.notice.dto.response;

import com.nokcha.efbe.domain.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "공지사항 목록 응답")
public class NoticeSummaryRspDto {

    @Schema(description = "공지사항 ID", example = "1")
    private Long id;

    @Schema(description = "공지사항 제목", example = "서비스 점검 안내")
    private String title;

    @Schema(description = "공지사항 작성자 닉네임", example = "관리자")
    private String author;

    @Schema(description = "공지사항 작성일시", example = "2026-04-23T12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "공지사항 조회수", example = "10")
    private Long viewCount;

    public static NoticeSummaryRspDto from(Notice notice, String authorNickname) {
        return NoticeSummaryRspDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .author(authorNickname)
                .createTime(notice.getCreateTime())
                .viewCount(notice.getViewCount())
                .build();
    }
}
