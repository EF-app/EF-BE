package com.nokcha.efbe.common.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Executor;

/**
 * @EnableAsync 기본 task executor 의 thread 가 호출자 SecurityContext 를 상속하도록 TaskDecorator 등록.
 *  → @Async 메서드 안에서 SecurityContextHolder 가 호출자 Authentication 보유
 *  → AuditorAware<Long> 가 호출자 사용자 id 로 audit 컬럼 (create_user/update_user) 을 정확히 채움.
 *
 *  MatchFeedRecomputeListener 의 @Async 3종 (onUserCreated/onProfileUpdated/onUserReactivated) 이
 *  ProfileEditService 같은 인증된 호출자의 context 를 잃지 않게 함.
 */
@Configuration
public class AsyncSecurityConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-sec-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);   // graceful shutdown — in-flight @Async task 완료 대기. setWaitForTasksToCompleteOnShutdown 만으론 0초라 무효.
        executor.setTaskDecorator(runnable -> {
            SecurityContext snapshot = SecurityContextHolder.getContext();
            return () -> {
                try {
                    SecurityContextHolder.setContext(snapshot);
                    runnable.run();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            };
        });
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
