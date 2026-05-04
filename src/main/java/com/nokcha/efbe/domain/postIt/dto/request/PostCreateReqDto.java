package com.nokcha.efbe.domain.postIt.dto.request;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 포스트잇 작성 요청 DTO
@Getter
@NoArgsConstructor
public class PostCreateReqDto {

    @NotBlank
    @Size(max = 2000)
    private String content;

    // 7종 카테고리 코드 (LIGHTN=번개)
    @NotNull
    private PostCategory categoryCode;

    // 익명 여부 (번개 카테고리는 서버에서 false 강제)
    private Boolean isAnonymous;

    // 프리미엄 유저의 3일 만료 옵션 (null/false = 1일)
    private Boolean premiumDuration;
}
