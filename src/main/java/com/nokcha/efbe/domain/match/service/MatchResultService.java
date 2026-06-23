package com.nokcha.efbe.domain.match.service;

import com.nokcha.efbe.domain.match.entity.MatchResult;
import com.nokcha.efbe.domain.match.model.MatchTriggerType;
import com.nokcha.efbe.domain.match.repository.MatchResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * match_results 도메인 서비스.
 *  - mutual 성사 → recordMutual (idempotent — UNIQUE 페어로 중복 INSERT 방지)
 *  - 페어 (a, b) 정규화: LEAST → userA, GREATEST → userB
 *  - cancel 시엔 row 유지 (mutual cancel 흐름은 match_actions UPDATE 만)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchResultService {

    private final MatchResultRepository matchResultRepo;

    /**
     * mutual 성사 시 호출 — 같은 페어가 이미 있으면 idempotent (LIKE/SUPER_LIKE 토글 흐름 흡수).
     *
     *  @param actorId   액션 발생 actor (LIKE 누른 사람)
     *  @param targetId  상대
     *  @param isSuper   양쪽 중 SUPER_LIKE 하나라도 (호출처에서 확인)
     *  @param trigger   매칭 트리거 (대부분 MUTUAL_LIKE)
     */
    @Transactional
    public void recordMutual(long actorId, long targetId, boolean isSuper, MatchTriggerType trigger) {
        long userA = Math.min(actorId, targetId);
        long userB = Math.max(actorId, targetId);
        if (matchResultRepo.existsByUserAIdAndUserBId(userA, userB)) {
            // 이미 매칭 — cancel/restore 토글 흐름. is_super 갱신만 검토 (단순화 — 첫 매칭 시점 그대로)
            log.debug("[MatchResult] mutual 이미 존재 — userA={}, userB={}", userA, userB);
            return;
        }
        matchResultRepo.save(MatchResult.builder()
                .userAId(userA)
                .userBId(userB)
                .triggerType(trigger)
                .isSuper(isSuper)
                .build());
        log.debug("[MatchResult] mutual 신규 — userA={}, userB={}, isSuper={}, trigger={}",
                userA, userB, isSuper, trigger);
    }
}
