package com.nokcha.efbe.domain.postIt.dto.request;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 포스트잇 작성 요청 DTO
@Getter
@NoArgsConstructor
@Schema(description = "포스트잇 작성 요청")
public class PostCreateReqDto {

    @NotBlank
    @Size(max = 2000)
    @Schema(description = "포스트잇 본문 (최대 2000자)", example = "오늘 점심 같이 드실 분 구해요!")
    private String content;

    @NotNull
    @Schema(description = "7종 카테고리 코드", example = "FREE")
    private PostCategory categoryCode;

    @Schema(description = "익명 여부 (번개 카테고리 LIGHTN은 서버에서 false 강제)", example = "false")
    private Boolean isAnonymous;

    @Schema(description = "프리미엄 유저의 3일 만료 옵션. null/false 면 1일(24h), true 면 3일(72h)", example = "false")
    private Boolean premiumDuration;
}
