package com.nokcha.efbe.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 비즈니스 로직 상 예외
@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final HttpStatus httpStatus;
    // ErrorCode enum 이름 (예: "NOT_VOTED_FOR_COMMENT"). 문자열 메시지로 던질 땐 null.
    private final String errorCode;

    // 기본 에러 메시지
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // throwable 의 detailMessage 에 들어가며, throwable.getMessage()로 부를 수 있음
        this.code = errorCode.getCode();
        this.httpStatus = HttpStatus.valueOf(errorCode.getCode());
        this.errorCode = errorCode.name();
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.httpStatus = HttpStatus.valueOf(errorCode.getCode());
        this.errorCode = errorCode.name();
    }

    // 같은 ErrorCode 안에서 상세 메시지만 다른 케이스 — 예: "weight_keyword: DOUBLE 파싱 실패"
    public BusinessException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.code = errorCode.getCode();
        this.httpStatus = HttpStatus.valueOf(errorCode.getCode());
        this.errorCode = errorCode.name();
    }

    public BusinessException(String message, HttpStatus httpStatus) {
        super(message);
        this.code = httpStatus.value();
        this.httpStatus = httpStatus;
        this.errorCode = null;
    }

    public BusinessException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.code = httpStatus.value();
        this.httpStatus = httpStatus;
        this.errorCode = null;
    }
}
