package com.nokcha.efbe.domain.notice.dto.response;

import com.nokcha.efbe.domain.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "공지사항 페이지 응답")
public class NoticePageRspDto {

    @Schema(description = "공지사항 목록")
    private List<NoticeSummaryRspDto> notices;

    @Schema(description = "현재 페이지 번호", example = "0")
    private int page;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @Schema(description = "전체 페이지 수", example = "3")
    private int totalPages;

    @Schema(description = "전체 공지사항 수", example = "25")
    private long totalElements;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean last;
}
