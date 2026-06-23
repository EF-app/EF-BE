package com.nokcha.efbe.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 통합 테스트 메타 어노테이션 — 실제 MariaDB (ef_db_test) 사용.
 *  - @SpringBootTest 로 전체 ApplicationContext 부팅 (Spring Boot 4 에서는 @DataJpaTest 미가용)
 *  - webEnvironment=MOCK — Servlet 환경 mocking (SecurityConfig 의 HttpSecurity 빈 위해 필요)
 *  - 메서드 단위 @Transactional 롤백 — 테스트 종료 시 자동 정리
 *
 *  사용:
 *    @IntegrationTest
 *    class MyServiceTest {
 *        @Autowired EntityManager em;
 *        @Autowired UserManagementImpl userMgmt;
 *        ...
 *    }
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
public @interface IntegrationTest {}
