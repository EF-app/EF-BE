package com.nokcha.efbe.common.exception.handler;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.exception.dto.ErrorRspDto;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.errorLog.entity.ErrorSeverity;
import com.nokcha.efbe.domain.errorLog.entity.ErrorSource;
import com.nokcha.efbe.domain.errorLog.service.SystemErrorLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String ADMIN_PATH_PREFIX = "/v1/admin";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final SystemErrorLogService systemErrorLogService;
    private final SecurityUtil securityUtil;
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

    // BusinessException을 상속한 다른 CustomException에도 적용 — errorCode (enum 이름) 함께 전달
    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<ErrorRspDto<String>> handleBusinessException(BusinessException e, HttpServletRequest request){
        printLog(e, request);
        // cause 를 품은 BusinessException = 외부연동(R2·Firestore·FirebaseAuth 등) 실패
        if (e.getCause() != null) {
            logStoreExternal(e, request);
        }
        ErrorRspDto<String> body = new ErrorRspDto<>(e.getCode(), e.getHttpStatus(), e.getErrorCode(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(body);
    }

    // 정적 리소스/매핑 미존재
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRspDto<String>> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        log.warn("404 NotFound: method={} url={}", request.getMethod(), request.getRequestURI());
        return createErrorResponse(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다.");
    }

    // 예상하지 못한 예외 발생 시 500 에러와 함께 기본 에러 메시지 넘기기
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRspDto<String>> handleException(Exception e, HttpServletRequest request){
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("예외 처리 범위 외의 오류 발생");
        printLog(e, request);
        // 상세 stacktrace 는 파일 로그(printLog) + system_error_log(logStoreApi)에만 저장.
        // 클라이언트 응답엔 일반 메시지만 — 내부 구조/SQL/경로 노출 방지.
        logStoreApi(e, request, httpStatus.value());
        //String fullStackTrace = LoggingUtil.stackTraceToString(e);

        return createErrorResponse(httpStatus.value(), httpStatus, "서버 오류가 발생했습니다.");
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

    // 미처리 예외(500) → API / ADMIN_API 적재
    private void logStoreApi(Exception e, HttpServletRequest request, int httpStatus) {
        boolean admin = isAdminRequest(request);
        Long principalId = securityUtil.getCurrentUserIdOrNull();
        systemErrorLogService.logStore(
                admin ? ErrorSource.ADMIN_API : ErrorSource.API,
                ErrorSeverity.ERROR,
                request.getMethod() + " " + request.getRequestURI(),
                httpStatus,
                request.getRequestURI(),
                admin ? null : principalId,
                admin ? principalId : null,
                e,
                null);
    }

    // cause 를 품은 BusinessException → EXTERNAL 적재. error_class·stacktrace 는 실제 원인(cause) 기준.
    private void logStoreExternal(BusinessException e, HttpServletRequest request) {
        boolean admin = isAdminRequest(request);
        Long principalId = securityUtil.getCurrentUserIdOrNull();
        String errorType = e.getErrorCode() != null ? e.getErrorCode() : request.getRequestURI();
        systemErrorLogService.logStore(
                ErrorSource.EXTERNAL,
                ErrorSeverity.ERROR,
                errorType,
                e.getHttpStatus().value(),
                request.getRequestURI(),
                admin ? null : principalId,
                admin ? principalId : null,
                e.getCause(),
                null);
    }

    // 관리자 요청 판별 — URL prefix 또는 ROLE_ADMIN 권한.
    private boolean isAdminRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith(ADMIN_PATH_PREFIX)) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
    }
}
