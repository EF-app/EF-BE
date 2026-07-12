package com.nokcha.efbe.infra.scheduler;

import com.nokcha.efbe.domain.errorLog.entity.ErrorSeverity;
import com.nokcha.efbe.domain.errorLog.service.SystemErrorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 스케줄러 공통 실행 가드 — 배치 본문을 감싸 미처리 예외를 통일된 정책으로 처리한다.
 *
 * 정책: 배치 전체 중단 예외 → system_error_log(BATCH/ERROR) 적재 후 rethrow.
 *   - 적재는 SystemErrorLogService.logStore* (REQUIRES_NEW) 라 원 트랜잭션 롤백과 무관하게 남는다.
 *   - rethrow 로 Spring 스케줄러의 실패 로깅 + ShedLock 의 실패 인지를 그대로 유지.
 *
 * 개별 항목(loop) 부분 실패(WARN)는 각 스케줄러가 루프 안에서 직접 logStoreBatch(WARN) 로 처리한다
 * (전체 중단 ERROR 와 성격이 달라 가드로 묶지 않음).
 */
@Component
@RequiredArgsConstructor
public class SchedulerGuard {

    private final SystemErrorLogService systemErrorLogService;

    /**
     * @param batchName system_error_log.error_type 에 기록될 배치 식별자 (예: "PostItScheduler.expirePins")
     * @param body      배치 본문
     */
    public void runGuarded(String batchName, Runnable body) {
        try {
            body.run();
        } catch (Exception e) {
            systemErrorLogService.logStoreBatch(ErrorSeverity.ERROR, batchName, null, e);
            throw e;
        }
    }
}
