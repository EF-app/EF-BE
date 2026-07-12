package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.user.entity.UserDestructionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// user_destruction_log 표준 JPA Repository (파기 이력 적재 + 재시도 대상 조회)
public interface UserDestructionLogRepository extends JpaRepository<UserDestructionLog, Long> {

    // 외부(R2 등) 파기 미완료 건 — 재시도 배치 대상
    List<UserDestructionLog> findByExternalPurgeStatusIn(List<String> statuses);

    // 관리자 유저 상세 — user_withdrawal 이 없는 파기 유저(휴면 파기 등)의 파기 시각 폴백용
    Optional<UserDestructionLog> findTopByUserIdOrderByDestroyedAtDesc(Long userId);
}
