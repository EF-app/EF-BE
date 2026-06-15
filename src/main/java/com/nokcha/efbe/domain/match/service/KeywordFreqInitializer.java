package com.nokcha.efbe.domain.match.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 부팅 직후 1회 자동 캐시 갱신 — {@link KeywordFreqService#refresh()} 호출.
 *
 *  ※ {@link KeywordFreqServiceImpl} 자신의 @PostConstruct 에서 호출하지 않는 이유:
 *    - @PostConstruct 안 self-invocation 은 Spring AOP proxy 우회 → @Transactional 적용 안 됨
 *    - strict 트랜잭션 모드 (hibernate.connection.autocommit=false) 에서 SELECT 실패 가능성
 *
 *  이 별도 빈에서 인터페이스 타입으로 호출 → proxy 통과 → @Transactional 정상.
 *
 *  실패해도 부팅 차단 안 함 — warn 로그 + 빈 캐시로 시작, 다음 04:00 배치가 채움.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordFreqInitializer {

    private final KeywordFreqService keywordFreqService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            keywordFreqService.refresh();
        } catch (Exception e) {
            log.warn("[KeywordFreqInitializer] 부팅 시 캐시 초기화 실패 — 04:00 배치까지 빈 상태. err={}",
                    e.getMessage(), e);
        }
    }
}
