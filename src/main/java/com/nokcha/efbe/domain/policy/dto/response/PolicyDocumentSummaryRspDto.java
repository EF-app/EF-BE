package com.nokcha.efbe.domain.policy.dto.response;

import com.nokcha.efbe.domain.policy.entity.CodePolicyDocument;
import com.nokcha.efbe.domain.policy.entity.PolicyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "약관 목록 응답 (본문 미포함)")
public class PolicyDocumentSummaryRspDto {

    @Schema(description = "약관 타입", example = "TERMS_AGREE")
    private PolicyType policyType;

    @Schema(description = "버전", example = "v1.1")
    private String version;

    @Schema(description = "제목", example = "EF 서비스 이용약관")
    private String title;

    @Schema(description = "한 줄 요약")
    private String summary;

    @Schema(description = "필수 동의 여부", example = "true")
    private boolean isRequired;

    @Schema(description = "시행일")
    private LocalDateTime effectiveDate;

    public static PolicyDocumentSummaryRspDto from(CodePolicyDocument doc) {
        return PolicyDocumentSummaryRspDto.builder()
                .policyType(doc.getPolicyType())
                .version(doc.getVersion())
                .title(doc.getTitle())
                .summary(doc.getSummary())
                .isRequired(doc.isRequired())
                .effectiveDate(doc.getEffectiveDate())
                .build();
    }
}
