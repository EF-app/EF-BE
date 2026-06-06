package com.nokcha.efbe.domain.match.service;

import com.nokcha.efbe.common.util.LocationUtil;
import com.nokcha.efbe.domain.match.config.MatchingConfigLoader;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.dto.response.FeedCardRspDto;
import com.nokcha.efbe.domain.match.feed.ColdStartFeed;
import com.nokcha.efbe.domain.match.feed.FeedRecomputeThrottle;
import com.nokcha.efbe.domain.match.repository.DailyFeedRepository;
import com.nokcha.efbe.domain.match.repository.UserManagement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 현재 피드 조회 — viewer 당 row 1 세트만 유지하므로 날짜 필터 없음.
 *  본인 status 단락 (TEMPORARY/PERMANENT/WITHDRAWING/WITHDRAWN) 은 SuspensionGuardFilter 가 진입 차단.
 *
 *  ── lazy ColdStartFeed (§10.20) ──
 *    응답이 빈데 daily_feed 의 raw row 도 0 이면 (배치 실패 / 휴면 복귀 등) → ColdStartFeed 동기 호출 후 재조회.
 *    raw row 가 있는데 read-time 오버레이로 0 = 본인이 다 액션한 정상 동작이라 fallback 안 함 (명세서 §4.4).
 *    throttle 30 초로 중복 호출 차단.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private final DailyFeedRepository feedRepo;
    private final UserManagement userMgmt;
    private final ColdStartFeed coldStartFeed;
    private final MatchingConfigLoader configLoader;
    private final FeedRecomputeThrottle throttle;

    public List<FeedCardRspDto> getCurrentFeed(long viewerId) {
        List<DailyFeedRepository.FeedView> rows = feedRepo.findCurrentFeed(viewerId);

        // 빈 응답 + raw row 자체도 0 이면 lazy ColdStartFeed 시도
        if (rows.isEmpty()) {
            int rawCount = feedRepo.countByViewerId(viewerId);
            if (rawCount == 0 && throttle.tryAcquire(viewerId)) {
                rows = tryLazyColdStart(viewerId);
            }
        }

        return rows.stream()
                .map(v -> new FeedCardRspDto(
                        v.rank(),
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

    private List<DailyFeedRepository.FeedView> tryLazyColdStart(long viewerId) {
        try {
            UserContext me = userMgmt.loadContext(viewerId);
            if (me == null) {
                log.warn("[FeedService] lazy ColdStartFeed — UserContext null, viewerId={}", viewerId);
                return List.of();
            }
            coldStartFeed.build(me, configLoader.load());
            log.info("[FeedService] lazy ColdStartFeed 실행 — viewerId={}", viewerId);
            return feedRepo.findCurrentFeed(viewerId);
        } catch (Exception e) {
            log.warn("[FeedService] lazy ColdStartFeed 실패 — viewerId={}, err={}", viewerId, e.getMessage(), e);
            return List.of();
        }
    }
}
