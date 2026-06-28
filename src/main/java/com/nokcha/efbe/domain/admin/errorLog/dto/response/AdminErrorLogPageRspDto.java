package com.nokcha.efbe.domain.admin.errorLog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "에러 로그 페이지 (숫자 페이지네이션 — totalPages 포함)")
public class AdminErrorLogPageRspDto {

    @Schema(description = "현재 페이지 row 목록")
    private List<AdminErrorLogItemRspDto> content;

    @Schema(description = "현재 페이지 번호 (0-base)", example = "0")
    private int page;

    @Schema(description = "페이지 크기", example = "20")
    private int size;

    @Schema(description = "전체 row 수", example = "137")
    private long totalElements;

    @Schema(description = "전체 페이지 수", example = "7")
    private int totalPages;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;
}
