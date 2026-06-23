package com.nokcha.efbe.domain.match.service;

import com.nokcha.efbe.common.util.LocationUtil;
import com.nokcha.efbe.domain.match.config.MatchingConfigLoader;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.dto.response.FeedCardRspDto;
import com.nokcha.efbe.domain.match.feed.ColdStartFeed;
import com.nokcha.efbe.domain.match.feed.FeedRecomputeThrottle;
import com.nokcha.efbe.domain.match.repository.MatchDailyFeedQueryRepository;
import com.nokcha.efbe.domain.match.repository.UserManagement;
import com.nokcha.efbe.domain.match.repository.projection.FeedView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 현재 피드 조회
 *  본인 status 단락 (TEMPORARY/PERMANENT/WITHDRAWING/WITHDRAWN) 은 SuspensionGuardFilter 가 진입 차단.

 *    응답이 빈데 daily_feed 의 raw row 도 0 이면 (배치 실패 / 휴면 복귀 등) → ColdStartFeed 동기 호출 후 재조회.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchFeedService {

    private final MatchDailyFeedQueryRepository dailyFeedQuery;
    private final UserManagement userMgmt;
    private final ColdStartFeed coldStartFeed;
    private final MatchingConfigLoader configLoader;
    private final FeedRecomputeThrottle throttle;

    public List<FeedCardRspDto> getCurrentFeed(long viewerId) {
        List<FeedView> rows = dailyFeedQuery.findCurrentFeed(viewerId);

        // 빈 응답 + raw row 자체도 0 이면 lazy ColdStartFeed 시도
        if (rows.isEmpty()) {
            int rawCount = dailyFeedQuery.countByViewerId(viewerId);
            if (rawCount == 0 && throttle.tryAcquire(viewerId)) {
                rows = tryLazyColdStart(viewerId);
            }
        }

        return rows.stream()
                .map(v -> new FeedCardRspDto(
                        v.matchRank(),
                        v.targetId(),
                        v.slotType(),
                        v.tagsJson(),
                        v.nickname(),
                        v.age(),
                        LocationUtil.composeLocation(v.country(), v.city()),
                        v.mbti(),
                        v.job(),
                        v.bioMessage(),
                        v.mainPhotoUrl(),
                        v.distanceKm()))
                .toList();
    }

    private List<FeedView> tryLazyColdStart(long viewerId) {
        try {
            UserContext me = userMgmt.loadContext(viewerId);
            if (me == null) {
                log.warn("[MatchFeedService] lazy ColdStartFeed — UserContext null, viewerId={}", viewerId);
                return List.of();
            }
            coldStartFeed.build(me, configLoader.load());
            log.info("[MatchFeedService] lazy ColdStartFeed 실행 — viewerId={}", viewerId);
            return dailyFeedQuery.findCurrentFeed(viewerId);
        } catch (Exception e) {
            log.warn("[MatchFeedService] lazy ColdStartFeed 실패 — viewerId={}, err={}", viewerId, e.getMessage(), e);
            return List.of();
        }
    }
}
