package com.nokcha.efbe.domain.admin.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 일일 피드 페이지 응답
 *  대량 row 환경에서 COUNT(*) 가 풀 스캔이라 안하고 admin 조회 화면은 [이전]/[다음] UX 로 단순화.
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "일일 피드 페이지 (총 row 수 없음)")
public class AdminDailyFeedPageRspDto {

    @Schema(description = "현재 페이지 row 목록")
    private List<AdminDailyFeedItemRspDto> content;

    @Schema(description = "현재 페이지 번호 (0-base)")
    private int page;

    @Schema(description = "페이지 크기")
    private int size;

    @Schema(description = "다음 페이지 존재 여부")
    private boolean hasNext;
}
