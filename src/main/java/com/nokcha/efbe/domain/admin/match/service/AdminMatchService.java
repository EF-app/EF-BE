package com.nokcha.efbe.domain.admin.match.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminMatchFullBatchRspDto;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminMatchRecoverBatchRspDto;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminMatchUserBatchRspDto;
import com.nokcha.efbe.domain.match.feed.MyFeedRecomputer;
import com.nokcha.efbe.domain.match.repository.MatchDailyFeedQueryRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import com.nokcha.efbe.infra.scheduler.match.NightlyMatchBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 매칭 운영 도구 — 관리자 전용
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMatchService {

    private final UserRepository userRepository;
    private final MyFeedRecomputer myFeedRecomputer;
    private final MatchDailyFeedQueryRepository dailyFeedQuery;
    private final NightlyMatchBatch nightlyMatchBatch;

    /**
     * 특정 유저의 daily_feed 재계산.
     *  내부 동작 = {@link MyFeedRecomputer#recompute} (재계산로직 : 유저 프로필 변경/휴면복귀/제재해제/탈퇴취소/회원가입)
     */
    @Transactional
    public AdminMatchUserBatchRspDto runUserBatch(long userId) {
        long start = System.currentTimeMillis();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.MATCH_RECOMPUTE_INACTIVE_USER);
        }

        myFeedRecomputer.recompute(userId);

        int cardCount = dailyFeedQuery.countByViewerId(userId);
        long durationMs = System.currentTimeMillis() - start;

        log.info("[AdminMatch] user batch — userId={}, cardCount={}, durationMs={}",
                userId, cardCount, durationMs);

        return new AdminMatchUserBatchRspDto(userId, cardCount, durationMs);
    }

    /**
     * 보정 배치 실행 — "04:00 정상 배치 누락 의심" 시 관리자가 일괄 복구.
     *  내부 동작 = {@link NightlyMatchBatch#runRecoverNow()} 와 동일 (ShedLock 우회).
     *  idempotent: 오늘 daily_feed row 없는 viewer 만 처리하므로 중복 호출되어도 안전.
     */
    public AdminMatchRecoverBatchRspDto runRecoverBatch() {
        NightlyMatchBatch.RecoverStats s = nightlyMatchBatch.runRecoverNow();
        log.info("[AdminMatch] recover batch — target={}, recover={}, coldStart={}, fail={}, ms={}",
                s.targetCount(), s.recoverCount(), s.coldStartCount(), s.failCount(), s.durationMs());
        return new AdminMatchRecoverBatchRspDto(
                s.targetCount(), s.recoverCount(), s.coldStartCount(), s.failCount(), s.durationMs());
    }

    /**
     * 전체 정상 배치 강제 실행 — 04:00 cron 과 동일 흐름.
     */
    public AdminMatchFullBatchRspDto runFullBatch() {
        NightlyMatchBatch.FullStats s = nightlyMatchBatch.runFullNow();
        log.info("[AdminMatch] full batch — total={}, success={}, fail={}, ms={}",
                s.totalViewers(), s.successCount(), s.failCount(), s.durationMs());
        return new AdminMatchFullBatchRspDto(
                s.totalViewers(), s.successCount(), s.failCount(), s.durationMs());
    }
}
