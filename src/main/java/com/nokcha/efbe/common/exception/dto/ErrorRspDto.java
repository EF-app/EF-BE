package com.nokcha.efbe.common.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorRspDto<T> {

    @Schema(description = "HTTP 상태 코드", example = "400")
    private int code;

    @Schema(description = "HTTP 상태 phrase", example = "Bad Request")
    private String httpStatus;

    @Schema(description = "비즈니스 에러 식별자 (ErrorCode enum 이름). 클라이언트 분기용. 일반 예외는 null.",
            example = "NOT_VOTED_FOR_COMMENT", nullable = true)
    private String errorCode;

    @Schema(description = "에러 메시지 또는 필드별 검증 오류")
    private T errorMessage;

    // 기존 생성자 유지 (errorCode = null 로 위임) — 일반 예외 핸들러용
    public ErrorRspDto(int code, HttpStatus httpStatus, T errorMessage) {
        this(code, httpStatus, null, errorMessage);
    }

    // BusinessException 핸들러에서 ErrorCode 이름까지 전달할 때 사용
    public ErrorRspDto(int code, HttpStatus httpStatus, String errorCode, T errorMessage) {
        this.code = code;
        this.httpStatus = httpStatus.getReasonPhrase();
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
