package com.nokcha.efbe.domain.faq.dto.response;

import com.nokcha.efbe.domain.faq.entity.CodeFaq;
import com.nokcha.efbe.domain.faq.entity.FaqCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "FAQ 항목")
public class FaqRspDto {

    @Schema(description = "FAQ PK (code_faq.id)", example = "1")
    private Long id;

    @Schema(description = "카테고리 (소문자 키) — account/matching/message/payment/report/etc", example = "account")
    private FaqCategory category;

    @Schema(description = "질문", example = "비밀번호를 잊어버렸어요. 어떻게 하나요?")
    private String question;

    @Schema(description = "답변")
    private String answer;

    @Schema(description = "인기 질문 여부", example = "false")
    private Boolean isPopular;

    @Schema(description = "카테고리 내부 노출 순서", example = "1")
    private Integer displayOrder;

    public static FaqRspDto from(CodeFaq f) {
        return FaqRspDto.builder()
                .id(f.getId())
                .category(f.getCategory())
                .question(f.getQuestion())
                .answer(f.getAnswer())
                .isPopular(Boolean.TRUE.equals(f.getIsPopular()))
                .displayOrder(f.getDisplayOrder())
                .build();
    }
}
