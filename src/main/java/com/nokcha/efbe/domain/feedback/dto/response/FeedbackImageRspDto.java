package com.nokcha.efbe.domain.feedback.dto.response;

import com.nokcha.efbe.domain.feedback.entity.FeedbackImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

// 피드백 첨부 이미지 응답
@Getter
@Builder
@Schema(description = "피드백 첨부 이미지")
public class FeedbackImageRspDto {

    @Schema(description = "이미지 PK", example = "1")
    private Long id;

    @Schema(description = "이미지 URL")
    private String url;

    @Schema(description = "정렬 순서 (0부터)", example = "0")
    private Integer sortOrder;

    @Schema(description = "원본 파일명")
    private String originalName;

    public static FeedbackImageRspDto from(FeedbackImage img) {
        return FeedbackImageRspDto.builder()
                .id(img.getId())
                .url(img.getUrl())
                .sortOrder(img.getSortOrder())
                .originalName(img.getOriginalName())
                .build();
    }
}
