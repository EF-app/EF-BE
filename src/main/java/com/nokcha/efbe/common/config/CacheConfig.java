package com.nokcha.efbe.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 인메모리 캐시 매니저 (단일 인스턴스 환경 기준)
// Redis 전환 시: spring-boot-starter-data-redis 의존성 추가 + application.yml 에 spring.cache.type=redis 로 교체
@Configuration
public class CacheConfig {

    // @Cacheable 로 사용하는 캐시 이름 목록 — 존재하지 않는 이름으로 접근 시 dynamic 생성되지만,
    // 명시적으로 나열해 오타/누락을 조기에 발견할 수 있도록 고정
    private static final String[] CACHE_NAMES = {
            "categoryByCode",
            "itemByCode",
            "itemEntityByCode",
            "itemList",
            "subscriptionPlans"
    };

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CACHE_NAMES);
    }
}
