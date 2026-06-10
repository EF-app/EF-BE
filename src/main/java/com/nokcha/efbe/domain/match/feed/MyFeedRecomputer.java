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
import com.nokcha.efbe.infra.scheduler.match.BatchPhaseMetrics;
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

// MyFeedRecomputer = viewer 1명 단위 재계산 엔진.
// 단건 트리거(관리자 강제, 프로필 변경, 복귀 3종) 와
// 배치(04:00 정상 / 05:00 보정 / 부팅 catch-up / 관리자 full·recover)에서 모두 호출되는 매칭 도메인의 공용 진입점.
// - 운영 관찰 후 필요하면 viewer 별 ShedLock 또는 row-level mutex 검토

/**
 * 단건 (뷰어 1명) 피드 재계산.
 *  - NightlyMatchBatch 의 뷰어 1명 처리 흐름과 동일 (후보 풀 → 점수 → 슬롯 → 백필 → 교체 저장)
 *  - 풀 0명 fallback : {@link ColdStartFeed#build} 흐름으로 대체
 *  - emptyRanks 백필 : picked 가 dailyShow 미만이면 ColdStartFeed 풀에서 dedup 후 빈 자리 채움
 *  - 호출처:
 *      · {@code NightlyMatchBatch.run} (04:00 전체 배치, 각 viewer 순회)
 *      · {@code NightlyMatchBatch.recoverFailedViewers} (05:00 보정 배치)
 *      · {@code ProfileChangeListener.onProfileUpdated} (지역 변경)
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

    /** 단건 호출 (편집 후 트리거) — cfg 로드 포함. 캐시 없음. */
    @Transactional
    public void recompute(long meUserId) {
        UserContext me = userMgmt.loadContext(meUserId);
        if (me == null) {
            log.warn("[MyFeedRecomputer] UserContext 로드 실패 — userId={}", meUserId);
            return;
        }
        process(me, configLoader.load(), LocalDate.now(), null, null);
    }

    /** 캐시 없이 호출 — 기존 시그니처 호환 (CandidateSelector 가 null fallback). */
    public void process(UserContext me, MatchingConfig cfg, LocalDate today) {
        process(me, cfg, today, null, null);
    }

    /** activeCache 만 — metrics 없는 호출. */
    public void process(UserContext me, MatchingConfig cfg, LocalDate today,
                        Map<Long, UserContext> activeCache) {
        process(me, cfg, today, activeCache, null);
    }

    /**
     * 배치 호출 — 미리 로드된 cfg / today / activeCache / metrics 재사용.
     *  activeCache: 활성 viewer 전체 Map<id, UserContext> — viewer 1명당 loadContexts 의 N² 부하 회피.
     *               null 이면 단건 호출 흐름 (CandidateSelector 가 findEligible 로 fallback).
     *  metrics:     phase 별 ns 누계. null 이면 측정 skip.
     *
     *  풀 0명 fallback: ColdStartFeed.
     *  emptyRanks 백필: — reserved rank 자리 skip 하고 빈 자리 채움.
     *  Step 5 skip: 어제 top-N target set 과 같으면 replaceDailyFeed 자체 skip.
     */
    public void process(UserContext me, MatchingConfig cfg, LocalDate today,
                        Map<Long, UserContext> activeCache, BatchPhaseMetrics metrics) {
        long t0 = System.nanoTime();
        List<UserContext> pool = candidateSelector.buildPool(me, cfg, activeCache);
        if (metrics != null) metrics.buildPoolNs.add(System.nanoTime() - t0);
        if (pool.isEmpty()) {
            log.info("[MyFeedRecomputer] 풀 0명 — ColdStartFeed fallback. userId={}", me.id());
            coldStartFeed.build(me, cfg);
            return;
        }

        long t1 = System.nanoTime();
        List<PairScore> scored = new ArrayList<>(pool.size());
        for (UserContext other : pool) {
            scored.add(calculator.score(me, other, cfg));
        }
        if (metrics != null) metrics.scoreNs.add(System.nanoTime() - t1);

        long t2 = System.nanoTime();
        List<DailyFeedRow> rows = feedSelector.select(me, scored, cfg);
        if (metrics != null) metrics.selectNs.add(System.nanoTime() - t2);

        // 백필 — 분기 없이 항상 호출. emptyRanks 가 0 이면 자연 종료.
        long t3 = System.nanoTime();
        rows = backfillEmptyRanks(me, cfg, rows);
        if (metrics != null) metrics.backfillNs.add(System.nanoTime() - t3);

        // 어제 target set 과 같으면 DB write skip
        long t4 = System.nanoTime();
        if (isFeedUnchanged(me.id(), rows)) {
            if (metrics != null) metrics.feedSkipped.increment();
            log.debug("[MyFeedRecomputer] feed 변화 없음 — DB write skip. userId={}", me.id());
        } else {
            dailyFeedRepo.replaceDailyFeed(me.id(), today, rows);
        }
        if (metrics != null) metrics.replaceNs.add(System.nanoTime() - t4);
    }

    /**
     *  어제 daily_feed 의 target_id set 과 오늘 계산 결과 set 이 같으면 skip.
     *  주의: tags_json / sort_key / rank 가 같이 바뀔 수 있어 (KeywordFreqService 빈도 영향)
     *       엄밀히는 row 전체 hash 비교가 옳음. 보수적 단순화 — set 만 비교. -- 아직 보류
     *       hit ratio 측정 후 실제 카드 표시에 차이가 있는지 운영 검증 필요.
     */
    private boolean isFeedUnchanged(long viewerId, List<DailyFeedRow> newRows) {
        Set<Long> newSet = newRows.stream().map(DailyFeedRow::targetId).collect(Collectors.toSet());
        Set<Long> oldSet = dailyFeedRepo.findTargetIdsByViewerId(viewerId);
        return !oldSet.isEmpty() && oldSet.equals(newSet);
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
