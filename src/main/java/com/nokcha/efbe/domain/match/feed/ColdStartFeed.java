package com.nokcha.efbe.domain.match.feed;

import com.nokcha.efbe.domain.match.calculator.MatchCalculator;
import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.model.DailyFeedRow;
import com.nokcha.efbe.domain.match.model.PairScore;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.repository.DailyFeedRepository;
import com.nokcha.efbe.domain.match.repository.UserManagement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 임시 피드 — 명세서 §6.9 / §10.7 / §10.19.
 *  전날 인기(좋아요 多) + 최근 접속 두 풀을 교차로 섞어 dailyShow 까지 채움.
 *  점수 계산 + 슬롯 선정은 정상 흐름과 동일.
 *
 *  호출처:
 *    - {@link ColdStartFeedListener} — 가입 완료 직후 (UserCreatedEvent AFTER_COMMIT)
 *    - {@link MyFeedRecomputer#process} — 정상 흐름의 풀 0명 fallback
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ColdStartFeed {

    private final UserManagement userMgmt;
    private final MatchCalculator calculator;
    private final FeedSelector feedSelector;
    private final DailyFeedRepository dailyFeedRepo;

    @Transactional
    public void build(UserContext me, MatchingConfig cfg) {
        List<UserContext> popular = userMgmt.topLikedYesterday(me, cfg);
        List<UserContext> recent  = userMgmt.recentlyActive(me, cfg);

        LinkedHashSet<UserContext> mixed = new LinkedHashSet<>();
        Iterator<UserContext> pi = popular.iterator();
        Iterator<UserContext> ri = recent.iterator();
        while (mixed.size() < cfg.getDailyShow() && (pi.hasNext() || ri.hasNext())) {
            if (pi.hasNext()) mixed.add(pi.next());
            if (ri.hasNext()) mixed.add(ri.next());
        }

        List<PairScore> scored = new ArrayList<>(mixed.size());
        for (UserContext other : mixed) scored.add(calculator.score(me, other, cfg));

        List<DailyFeedRow> rows = feedSelector.select(me, scored, cfg);
        dailyFeedRepo.replaceDailyFeed(me.id(), LocalDate.now(), rows);

        log.info("[ColdStartFeed] 임시 피드 생성 — userId={}, rows={}", me.id(), rows.size());
    }
}
