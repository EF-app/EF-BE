package com.nokcha.efbe.domain.faq.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nokcha.efbe.domain.faq.entity.CodeFaq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

// FAQ 단건 응답 — FE FaqItem { id, category, question, answer } 형태와 호환
@Getter
@Builder
@Schema(description = "FAQ 항목")
public class FaqRspDto {

    @Schema(description = "FAQ PK (code_faq.id)", example = "1")
    private Long id;

    @Schema(description = "카테고리 (소문자 키) — account/matching/message/payment/report/etc", example = "account")
    private String category;

    @Schema(description = "질문", example = "비밀번호를 잊어버렸어요. 어떻게 하나요?")
    private String question;

    @Schema(description = "답변")
    private String answer;

    // Lombok boolean 직렬화 (is 접두 제거 회피) — 명시적 JSON 키 지정
    @Schema(description = "인기 질문 여부", example = "false")
    @JsonProperty("isPopular")
    private boolean isPopular;

    @Schema(description = "카테고리 내부 노출 순서", example = "1")
    private Integer displayOrder;

    public static FaqRspDto from(CodeFaq f) {
        return FaqRspDto.builder()
                .id(f.getId())
                .category(f.getCategory().apiKey())
                .question(f.getQuestion())
                .answer(f.getAnswer())
                .isPopular(Boolean.TRUE.equals(f.getIsPopular()))
                .displayOrder(f.getDisplayOrder())
                .build();
    }
}
