package com.nokcha.efbe.infra.scheduler.match;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** 04:00 정상 / 05:00 보정 배치의 viewer 처리 병렬화 Executor --yml 파일로 변경
 *
 *  설계:
 *    - viewer 1명 처리는 100% 독립 (다른 viewer 의 daily_feed row 와 disjoint) → 충돌 없이 병렬 가능
 *    - core/max = CPU 코어 - 2 (운영 환경 변수로 조정). queue 는 충분히 크게 (5만 viewer 까지).
 *    - 큐 가득 차도 CallerRunsPolicy 로 main thread 가 직접 실행 (배치 정지 방지)
 *
 *  Hikari pool 크기와 함께 조정:
 *    - threads ≤ DB_POOL_MAX 여야 의미. 8 thread → pool 16
 *    - thread 가 conn 보다 많으면 conn 대기로 병렬 효과 무의미.
 *    - yml 에서 spring.datasource.hikari.maximum-pool-size 를 16 으로 올려서 thread 8개 + HTTP 여유 8개 확보한 게 이 이유.
 */
@Configuration
public class MatchBatchExecutorConfig {

    @Bean(name = "matchBatchExecutor", destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor matchBatchExecutor(
            @Value("${match-batch.threads:8}") int threads, // yml에 match-batch.threads: 8 입력 권고 // thread수 = CPU 코어 - 2
            @Value("${match-batch.queue-capacity:60000}") int queueCapacity) {  // 10만 viewer 까지 큐에 쌓을 수 있음
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(threads);
        ex.setMaxPoolSize(threads); // core = max → 고정 thread 풀 (conn 풀 사이즈 산정하기 쉽도록)
        ex.setQueueCapacity(queueCapacity);
        ex.setThreadNamePrefix("match-batch-");
        // thread 다 일하는 중 + 큐도 가득일 때
        // 백압(back-pressure) — queue 50,000 마저 가득 차면 submit() 호출한 thread (= main 배치 루프 thread) 가 직접 실행. 작업 거부/누락 X
        // CallerRunsPolicy 가 매칭 배치에 맞는 이유:
        //   -무손실 자가 회복 — 자동 백압
        //   -최악의 경우에도 안전 — main 이 일꾼이 되면 submit 루프가 느려질 뿐, 시스템은 살아있음.
        ex.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        ex.setWaitForTasksToCompleteOnShutdown(true);   // shutdown 시그널 받아도 큐에 남은 작업 끝까지 처리. (false 면 즉시 중단)
        ex.setAwaitTerminationSeconds(300); // 종료 대기 max 5분. 그 안에 남은 작업 끝나면 정상 종료, 안 끝나면 강제 종료.
        ex.initialize();
        return ex;
    }
}
