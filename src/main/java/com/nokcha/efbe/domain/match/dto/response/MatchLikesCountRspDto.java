package com.nokcha.efbe.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * my 화면의 좋아요 배너 — 3개 숫자.
 *  - sent     : 내가 누른 좋아요 (actor=me, LIKE+SUPER_LIKE, 7일 cutoff)
 *  - received : 받은 좋아요    (target=me, LIKE+SUPER_LIKE, 7일 cutoff)
 *  - mutual   : 서로 좋아요    (양방향 LIKE+SUPER_LIKE 페어, 양쪽 모두 7일 cutoff)
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "내 좋아요 카운트 (sent/received/mutual)")
public class MatchLikesCountRspDto {

    @Schema(description = "내가 누른 좋아요 수", example = "12")
    private int sent;

    @Schema(description = "받은 좋아요 수", example = "8")
    private int received;

    @Schema(description = "서로 좋아요 수", example = "3")
    private int mutual;
}
