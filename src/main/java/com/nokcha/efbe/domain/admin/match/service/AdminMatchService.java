package com.nokcha.efbe.domain.admin.match.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminMatchRecomputeRspDto;
import com.nokcha.efbe.domain.match.feed.MyFeedRecomputer;
import com.nokcha.efbe.domain.match.repository.DailyFeedRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import com.nokcha.efbe.domain.user.repository.UserRepository;
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
    private final DailyFeedRepository dailyFeedRepository;

    /**
     * 특정 유저의 daily_feed 강제 재계산.
     *  내부 동작 = {@link MyFeedRecomputer#recompute} 와 동일
     */
    @Transactional
    public AdminMatchRecomputeRspDto forceRecompute(long userId) {
        long start = System.currentTimeMillis();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.MATCH_RECOMPUTE_INACTIVE_USER);
        }

        myFeedRecomputer.recompute(userId);

        int cardCount = dailyFeedRepository.countByViewerId(userId);
        long durationMs = System.currentTimeMillis() - start;

        log.info("[AdminMatch] 강제 재계산 — userId={}, cardCount={}, durationMs={}",
                userId, cardCount, durationMs);

        return new AdminMatchRecomputeRspDto(userId, cardCount, durationMs);
    }
}
