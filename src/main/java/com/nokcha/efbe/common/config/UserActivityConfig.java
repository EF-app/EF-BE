package com.nokcha.efbe.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 유저활동 추적 인프라 — last_active_at 갱신용
 *
 *  Bean 3종:
 *    - {@link #userActivityThrottleCache()}     5분 TTL Caffeine — 동일 user 가 5분 안 중복 UPDATE 방지
 *    - {@link #userActivityExecutor()}          비동기 UPDATE 전용 ThreadPool (매칭 batch 와 격리)
 *    - {@code @EnableAsync}                     UserActivityRecorder 의 @Async 활성화
 */
@Configuration
@EnableAsync
public class UserActivityConfig {

    @Bean
    public Cache<Long, Boolean> userActivityThrottleCache() {
        return Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats()
                .build();
    }

    @Bean(name = "userActivityExecutor")
    public ThreadPoolTaskExecutor userActivityExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(16);
        ex.setQueueCapacity(10_000);
        ex.setThreadNamePrefix("user-activity-");
        // DiscardOldestPolicy 는 가장 오래된 task 를 폐기 — 갱신 누락은 다음 request / heartbeat 가 흡수.
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(10);
        ex.initialize();
        return ex;
    }
}
