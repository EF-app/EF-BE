package com.nokcha.efbe.domain.admin.errorLog.dto.response;

import com.nokcha.efbe.domain.errorLog.entity.ErrorSeverity;
import com.nokcha.efbe.domain.errorLog.entity.ErrorSource;
import com.nokcha.efbe.domain.errorLog.entity.SystemErrorLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "관리자 에러 로그 단건 상세 (stacktrace·metadata 포함)")
public class AdminErrorLogDetailRspDto {

    @Schema(description = "에러 로그 PK", example = "1024")
    private Long id;

    @Schema(description = "에러 출처", example = "API")
    private ErrorSource errorSource;

    @Schema(description = "클래스·메서드명 또는 API 경로", example = "POST /v1/match/feed")
    private String errorType;

    @Schema(description = "심각도", example = "ERROR")
    private ErrorSeverity severity;

    @Schema(description = "HTTP 상태코드 (API 에러 시)", example = "500")
    private Integer httpStatus;

    @Schema(description = "요청 URL", example = "/v1/match/feed")
    private String requestUrl;

    @Schema(description = "영향받은 일반 사용자 ID", example = "55")
    private Long userId;

    @Schema(description = "영향받은 관리자 ID", example = "3")
    private Long adminId;

    @Schema(description = "Exception 클래스명", example = "java.lang.NullPointerException")
    private String errorClass;

    @Schema(description = "예외 메시지")
    private String errorMessage;

    @Schema(description = "전체 stacktrace")
    private String stacktrace;

    @Schema(description = "추가 컨텍스트 (JSON 문자열)")
    private String metadata;

    @Schema(description = "발생 시각", example = "2026-06-27T12:01:29")
    private LocalDateTime occurredAt;

    @Schema(description = "복구 시각 (미해결이면 null)", example = "2026-06-27T15:20:00")
    private LocalDateTime resolvedAt;

    public static AdminErrorLogDetailRspDto from(SystemErrorLog e) {
        return AdminErrorLogDetailRspDto.builder()
                .id(e.getId())
                .errorSource(e.getErrorSource())
                .errorType(e.getErrorType())
                .severity(e.getSeverity())
                .httpStatus(e.getHttpStatus())
                .requestUrl(e.getRequestUrl())
                .userId(e.getUserId())
                .adminId(e.getAdminId())
                .errorClass(e.getErrorClass())
                .errorMessage(e.getErrorMessage())
                .stacktrace(e.getStacktrace())
                .metadata(e.getMetadata())
                .occurredAt(e.getOccurredAt())
                .resolvedAt(e.getResolvedAt())
                .build();
    }
}
