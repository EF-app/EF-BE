package com.nokcha.efbe.common.exception.handler;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.exception.dto.ErrorRspDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // BingException 발생 시 (유효성 검사)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorRspDto<Map<String, String>>> handleBindException(BindException e, HttpServletRequest request) {
        printLog(e, request);

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();   // 오류 목록 가져오기

        StringBuilder sb = new StringBuilder();
        Map<String, String> errorInfoMap = new HashMap<>();

        // 오류를 추출해서 메시지 담기
        for (FieldError fieldError: fieldErrors) {
            String errorMsg = sb
                    .append(fieldError.getDefaultMessage())
                    .append(" 요청받은 값: ")
                    .append(fieldError.getRejectedValue())
                    .toString();

            errorInfoMap.put(fieldError.getField(), errorMsg);

            sb.setLength(0);
        }

        // 에러 전송 (400 에러)
        return createErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, errorInfoMap);
    }

    // @RequestParam 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorRspDto<String>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        printLog(e, request);
        String message = "파라미터 '" + e.getParameterName() + "'이(가) 누락되었습니다.";
        return createErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, message);
    }

    // 일반적인 런타임 예외 처리
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, NoSuchElementException.class})
    public ResponseEntity<ErrorRspDto<String>> handleBusinessException(RuntimeException e, HttpServletRequest request){
        printLog(e, request);
        return createErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // BusinessException을 상속한 다른 CustomException에도 적용
    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<ErrorRspDto<String>> handleBusinessException(BusinessException e, HttpServletRequest request){
        printLog(e, request);
        return createErrorResponse(e.getCode(), e.getHttpStatus(), e.getMessage());
    }

    // 정적 리소스/매핑 미존재 — 404 로 깔끔히 응답 (500 + traceId 마스킹 전에 분기).
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRspDto<String>> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        log.warn("404 NotFound: method={} url={}", request.getMethod(), request.getRequestURI());
        return createErrorResponse(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다.");
    }

    // 예상하지 못한 예외 발생 시 500 — 응답에는 trace ID 만, 상세 스택트레이스는 서버 로그로만 남김.
    // 운영 환경에서 내부 경로/테이블명/쿼리 등이 응답으로 노출되는 것을 차단.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRspDto<String>> handleException(Exception e, HttpServletRequest request){
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String traceId = java.util.UUID.randomUUID().toString();
        // 서버 로그에는 traceId 와 전체 스택트레이스 함께 기록
        log.error("[traceId={}] 예외 처리 범위 외의 오류 발생: {}", traceId, e.getMessage(), e);
        printLog(e, request);
        return createErrorResponse(httpStatus.value(), httpStatus,
                "internal server error (traceId=" + traceId + ")");
    }

    // 응답 생성 메소드
    private <T> ResponseEntity<ErrorRspDto<T>> createErrorResponse(int statusCode, HttpStatus httpStatus, T errorMessage) {
        ErrorRspDto<T> errDto = new ErrorRspDto<>(statusCode, httpStatus, errorMessage);
        return ResponseEntity.status(httpStatus).body(errDto);
    }

    // ErrorCode를 받아서 상태 코드와 메시지를 사용해 응답을 생성
    private ResponseEntity<ErrorRspDto<String>> createErrorResponse(ErrorCode errorCode) {
        int statusCode = errorCode.getCode();
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);

        ErrorRspDto<String> errDto = new ErrorRspDto<>(
                statusCode, httpStatus, errorCode.getMessage());
        return ResponseEntity.status(httpStatus).body(errDto);
    }

    // 예외 출력
    private void printLog(Exception e, HttpServletRequest request) {
        log.error("발생 예외: {}, 에러 메시지: {}, 요청 Method: {}, 요청 url: {}",
                e.getClass().getSimpleName(), e.getMessage(), request.getMethod(), request.getRequestURI(), e);
    }
}
