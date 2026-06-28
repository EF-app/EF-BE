package com.nokcha.efbe.domain.errorLog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokcha.efbe.domain.errorLog.entity.ErrorSeverity;
import com.nokcha.efbe.domain.errorLog.entity.ErrorSource;
import com.nokcha.efbe.domain.errorLog.entity.SystemErrorLog;
import com.nokcha.efbe.domain.errorLog.repository.SystemErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Map;

// 전 기능 공통 에러 적재 서비스

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemErrorLogService {

    // TEXT/VARCHAR 컬럼 길이 방어
    private static final int MAX_ERROR_TYPE = 150;
    private static final int MAX_ERROR_CLASS = 200;
    private static final int MAX_REQUEST_URL = 500;
    private static final int MAX_MESSAGE = 2000;
    private static final int MAX_STACKTRACE = 10_000;

    private final SystemErrorLogRepository systemErrorLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * 가장 일반적인 적재 진입점. 모든 필드를 직접 제어한다
     *
     * @param source      에러 출처
     * @param severity    심각도 (null 이면 ERROR)
     * @param errorType   클래스명·메서드명 또는 API 경로
     * @param httpStatus  API 에러 시 HTTP 상태코드
     * @param requestUrl  API 호출 URL
     * @param userId      영향받은 일반 사용자
     * @param adminId     영향받은 관리자
     * @param e           발생 예외 (error_class·error_message·stacktrace 자동 추출)
     * @param metadata    추가 컨텍스트 (params 등). JSON 으로 직렬화되어 저장. 없으면 null
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logStore(ErrorSource source, ErrorSeverity severity, String errorType,
                         Integer httpStatus, String requestUrl, Long userId, Long adminId,
                         Throwable e, Map<String, Object> metadata) {
        doLogStore(source, severity, errorType, httpStatus, requestUrl, userId, adminId, e, metadata);
    }

    /**
     * 배치/스케줄러 실패 적재:
     *  - WARN  : 개별 항목 실패 (다음 배치/요청에서 자동 복구). userId 지정
     *  - ERROR : 배치 전체 중단 (설정/캐시 로드 실패 등)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logStoreBatch(ErrorSeverity severity, String batchName, Long userId, Throwable e) {
        doLogStore(ErrorSource.BATCH, severity, batchName, null, null, userId, null, e, null);
    }

    /** @Async·이벤트 리스너 비동기 처리 실패. severity 로 구분 (보통 WARN). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logStoreEvent(ErrorSeverity severity, String listenerName, Long userId, Throwable e) {
        doLogStore(ErrorSource.EVENT, severity, listenerName, null, null, userId, null, e, null);
    }

    /** 외부 의존성(R2·Firestore·FirebaseAuth·향후 PG) 호출 실패. severity 로 구분 (보통 ERROR). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logStoreExternal(ErrorSeverity severity, String component, Long userId, Long adminId, Throwable e) {
        doLogStore(ErrorSource.EXTERNAL, severity, component, null, null, userId, adminId, e, null);
    }

    /**
     * 실제 적재 단일 구현. 모든 예외를 삼켜 호출자(REQUIRES_NEW 진입점)로 전파하지 않는다.
     */
    private void doLogStore(ErrorSource source, ErrorSeverity severity, String errorType,
                          Integer httpStatus, String requestUrl, Long userId, Long adminId,
                          Throwable e, Map<String, Object> metadata) {
        try {
            systemErrorLogRepository.save(SystemErrorLog.builder()
                    .errorSource(source)
                    .severity(severity != null ? severity : ErrorSeverity.ERROR)
                    .errorType(truncate(errorType, MAX_ERROR_TYPE))
                    .httpStatus(httpStatus)
                    .requestUrl(truncate(requestUrl, MAX_REQUEST_URL))
                    .userId(userId)
                    .adminId(adminId)
                    .errorClass(truncate(errorClassOf(e), MAX_ERROR_CLASS))
                    .errorMessage(truncate(messageOf(e), MAX_MESSAGE))
                    .stacktrace(truncate(stackTraceOf(e), MAX_STACKTRACE))
                    .metadata(toJson(metadata))
                    .occurredAt(LocalDateTime.now())
                    .build());
        } catch (Exception ex) {
            // 로깅 자신의 INSERT 실패 — 삼키고 파일 로그만 남긴다 (2차 예외/무한루프 방지).
            log.error("system_error_log INSERT 실패. source={}, errorType={}", source, errorType, ex);
        }
    }

    private String errorClassOf(Throwable e) {
        return e != null ? e.getClass().getName() : "UnknownError";
    }

    private String messageOf(Throwable e) {
        return e != null ? e.getMessage() : null;
    }

    private String stackTraceOf(Throwable e) {
        if (e == null) return null;
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private String toJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception ex) {
            log.warn("system_error_log metadata 직렬화 실패 — metadata 생략", ex);
            return null;
        }
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
