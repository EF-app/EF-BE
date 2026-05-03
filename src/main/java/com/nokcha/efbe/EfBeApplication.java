package com.nokcha.efbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// JPA Auditing 활성화는 common/config/JpaAuditingConfig 가 담당 (createUser/updateUser 자동 주입).
// 여기 @EnableJpaAuditing 두면 jpaAuditingHandler bean 중복 등록으로 부팅 실패.
@SpringBootApplication
public class EfBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(EfBeApplication.class, args);
    }

}
