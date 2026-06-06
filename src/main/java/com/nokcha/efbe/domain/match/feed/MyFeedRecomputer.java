package com.nokcha.efbe.domain.match.feed;

import com.nokcha.efbe.domain.match.calculator.MatchCalculator;
import com.nokcha.efbe.domain.match.calculator.SortKeyCalculator;
import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.config.MatchingConfigLoader;
import com.nokcha.efbe.domain.match.model.DailyFeedRow;
import com.nokcha.efbe.domain.match.model.PairScore;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.pool.CandidateSelector;
import com.nokcha.efbe.domain.match.repository.DailyFeedRepository;
import com.nokcha.efbe.domain.match.repository.UserManagement;
import com.nokcha.efbe.domain.match.tag.TagDisplayFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 단건 (뷰어 1명) 피드 재계산.
 *  - NightlyMatchBatch 의 뷰어 1명 처리 흐름과 동일 (후보 풀 → 점수 → 슬롯 → 백필 → 교체 저장)
 *  - 풀 0명 fallback (§10.19): {@link ColdStartFeed#build} 흐름으로 대체
 *  - emptyRanks 백필 (§10.21): picked 가 dailyShow 미만이면 ColdStartFeed 풀에서 dedup 후 빈 자리 채움
 *  - 호출처:
 *      · {@code NightlyMatchBatch.run} (04:00 전체 배치, 각 viewer 순회)
 *      · {@code NightlyMatchBatch.retryFailedViewers} (05:00 보정 배치)
 *      · {@code ProfileChangeListener.onProfileUpdated} (지역 / 이상형 중요포인트 변경)
 *
 *  ※ MatchingConfig 는 호출자가 한 번 로드해 넘기는 게 효율적 (배치 케이스). 단건 호출은
 *     {@link #recompute(long)} 가 내부에서 로드.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MyFeedRecomputer {

    private final MatchingConfigLoader configLoader;
    private final UserManagement userMgmt;
    private final CandidateSelector candidateSelector;
    private final MatchCalculator calculator;
    private final FeedSelector feedSelector;
    private final DailyFeedRepository dailyFeedRepo;
    private final ColdStartFeed coldStartFeed;
    private final SortKeyCalculator sortKeyCalc;
    private final TagDisplayFormatter tagFormatter;

    /** 단건 호출 (편집 후 트리거) — cfg 로드 포함. */
    @Transactional
    public void recompute(long meUserId) {
        UserContext me = userMgmt.loadContext(meUserId);
        if (me == null) {
            log.warn("[MyFeedRecomputer] UserContext 로드 실패 — userId={}", meUserId);
            return;
        }
        process(me, configLoader.load(), LocalDate.now());
    }

    /**
     * 배치 호출 — 미리 로드된 cfg / today 재사용. NightlyMatchBatch 가 뷰어마다 호출.
     *
     *  풀 0명 fallback: `CandidateSelector.buildPool` 결과가 비면 매칭 가능한 후보가 없는 상황
     *  (예: 매우 좁은 인구 통계, 초기 운영 시점). ColdStartFeed 흐름으로 자동 대체.
     *
     *  emptyRanks 백필: 자격 풀은 있지만 dailyShow 미만으로 채워진 경우, ColdStartFeed 풀
     *  (topLikedYesterday + recentlyActive) 에서 dedup 후 빈 자리 채움. reserved rank 자리는
     *  RecentNewbieBatch 가 채울 자리라 백필에서도 skip.
     */
    public void process(UserContext me, MatchingConfig cfg, LocalDate today) {
        List<UserContext> pool = candidateSelector.buildPool(me, cfg);
        if (pool.isEmpty()) {
            log.info("[MyFeedRecomputer] 풀 0명 — ColdStartFeed fallback. userId={}", me.id());
            coldStartFeed.build(me, cfg);
            return;
        }

        // score_cache 야간 적재 제거 (§10.20) — 페어 점수는 메모리 안에서만 계산
        List<PairScore> scored = new ArrayList<>(pool.size());
        for (UserContext other : pool) {
            scored.add(calculator.score(me, other, cfg));
        }
        List<DailyFeedRow> rows = feedSelector.select(me, scored, cfg);

        // §10.21 백필 — 분기 없이 항상 호출. emptyRanks 가 0 이면 자연 종료.
        rows = backfillEmptyRanks(me, cfg, rows);

        dailyFeedRepo.replaceDailyFeed(me.id(), today, rows);
    }

    /**
     * 정상 picked 카드는 그대로 두고, 비어있는 rank 자리에만 ColdStartFeed 풀에서 채움.
     *  reserved rank 자리 {10,20,30,40,50} 는 RecentNewbieBatch 가 채울 자리라 skip.
     *  slot_type 은 SCORE 재활용 (별도 enum 추가 안 함).
     */
    private List<DailyFeedRow> backfillEmptyRanks(UserContext me, MatchingConfig cfg,
                                                  List<DailyFeedRow> existingRows) {
        Set<Integer> reserved = FeedSelector.computeReservedRanks(cfg);
        Set<Integer> usedRanks = existingRows.stream()
                .map(DailyFeedRow::rank).collect(Collectors.toSet());
        List<Integer> emptyRanks = IntStream.rangeClosed(1, cfg.getDailyShow())
                .filter(r -> !usedRanks.contains(r) && !reserved.contains(r))
                .boxed().toList();
        if (emptyRanks.isEmpty()) return existingRows;

        Set<Long> excludeIds = existingRows.stream()
                .map(DailyFeedRow::targetId).collect(Collectors.toSet());
        excludeIds.add(me.id());

        // ColdStartFeed 와 동일 풀 — 인기 + 최근 활동
        List<UserContext> popular = userMgmt.topLikedYesterday(me, cfg);
        List<UserContext> recent  = userMgmt.recentlyActive(me, cfg);
        LinkedHashSet<UserContext> backfillPool = new LinkedHashSet<>();
        Iterator<UserContext> pi = popular.iterator();
        Iterator<UserContext> ri = recent.iterator();
        int target = emptyRanks.size();
        while (backfillPool.size() < target && (pi.hasNext() || ri.hasNext())) {
            if (pi.hasNext()) { UserContext u = pi.next(); if (!excludeIds.contains(u.id())) backfillPool.add(u); }
            if (ri.hasNext()) { UserContext u = ri.next(); if (!excludeIds.contains(u.id())) backfillPool.add(u); }
        }
        if (backfillPool.isEmpty()) return existingRows;

        // 백필 풀 점수 계산 (정상 매칭과 동일 — 태그 8종 다 발동)
        List<PairScore> scored = backfillPool.stream()
                .map(o -> calculator.score(me, o, cfg)).toList();

        // sortKey 산출 + 내림차순 정렬
        Map<PairScore, Double> sortKeys = new HashMap<>(scored.size() * 2);
        for (PairScore p : scored) {
            sortKeys.put(p, sortKeyCalc.calc(me, p.keyword(), p.idealBidir(),
                    p.lifestyle(), p.location(), cfg));
        }
        List<PairScore> sorted = scored.stream()
                .sorted(Comparator.comparingDouble((PairScore p) -> sortKeys.get(p)).reversed())
                .toList();

        // emptyRanks 자리에 sortKey 순으로 매핑. 사용된 targetId 는 dedup (백필 풀 안 중복 방지)
        Set<Long> usedTargets = new HashSet<>(excludeIds);
        List<DailyFeedRow> result = new ArrayList<>(existingRows);
        int i = 0;
        for (PairScore p : sorted) {
            if (i >= emptyRanks.size()) break;
            if (!usedTargets.add(p.otherId())) continue;
            result.add(new DailyFeedRow(emptyRanks.get(i++), p.otherId(),
                    sortKeys.get(p), "SCORE", tagFormatter.renderJson(me, p)));
        }
        log.debug("[MyFeedRecomputer] 백필 — userId={}, emptyRanks={}, filled={}",
                me.id(), emptyRanks.size(), i);
        return result;
    }
}
