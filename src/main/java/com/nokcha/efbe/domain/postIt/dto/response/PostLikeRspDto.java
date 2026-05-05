package com.nokcha.efbe.domain.postIt.dto.response;

import lombok.Builder;
import lombok.Getter;

// 포스트잇 좋아요 토글 응답
// 토글(추가/취소) 직후 갱신된 누적 좋아요 수와 viewer 의 좋아요 여부를 반환.
// FE 가 응답값을 그대로 반영하면 ±1 클라이언트 계산으로 인한 NaN/desync 방지.
@Getter
@Builder
public class PostLikeRspDto {
    private Long postId;
    private long likeCount;
    private boolean likedByMe;
}
