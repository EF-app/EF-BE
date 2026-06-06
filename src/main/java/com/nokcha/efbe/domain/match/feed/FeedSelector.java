package com.nokcha.efbe.domain.match.feed;

import com.nokcha.efbe.domain.match.calculator.SortKeyCalculator;
import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.model.DailyFeedRow;
import com.nokcha.efbe.domain.match.model.PairScore;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.tag.TagDisplayFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 3 단계 — sortKey 정렬 → 슬롯 50 명 선정 (명세서 §6.8).
 *
 *  순서 (관리자 설정값 기준):
 *    1) 개인키워드 보장  (CUSTOM_KW)
 *    2) 뉴비 하한        (NEWBIE)           — newbieFloor 까지 sortKey 순으로
 *    3) 점수 상위        (SCORE)            — core = dailyShow - randomSlots - freshNewbieReservedSlots
 *    4) 랜덤 발견        (RANDOM)           — randomSlots 만큼 동결
 *
 *  ※ sortKey 는 이 클래스 안에서 계산 — me 의 importantPoints 와 PairScore 의 영역 점수 4 가 입력.
 *     (cache 에 sortKey 가 저장되지 않으므로 정렬 시점에 한 번만 계산.)
 *  ※ freshNewbieReservedSlots 자리는 비워둠 — 앞쪽에서 step 간격 분산.
 *     (reservedSlots=5, step=5 → rank 5, 10, 15, 20, 25 자리 skip)
 *     매시간 미니 배치(RecentNewbieBatch)가 그 자리 채움.
 *  슬롯 종류별 dedup 은 LinkedHashMap (PairScore → slotType) — 등록 순서 보존.
 */
@Component
@RequiredArgsConstructor
public class FeedSelector {

    private final TagDisplayFormatter formatter;
    private final SortKeyCalculator sortKeyCalc;

    public List<DailyFeedRow> select(UserContext me, List<PairScore> scored, MatchingConfig cfg) {
        /* 페어별 sortKey 1 회 계산 (캐싱) — 이후 정렬·rank 부여에 재사용. */
        Map<PairScore, Double> sortKeys = new HashMap<>(scored.size() * 2);
        for (PairScore p : scored) {
            sortKeys.put(p, sortKeyCalc.calc(me, p.keyword(), p.idealBidir(), p.lifestyle(), p.location(), cfg));
        }
        scored.sort(Comparator.comparingDouble((PairScore p) -> sortKeys.get(p)).reversed());

        LinkedHashMap<PairScore, String> picked = new LinkedHashMap<>();

        /* 1. 개인키워드 보장 */
        for (PairScore p : scored) {
            if (p.hasCustomKeyword()) picked.putIfAbsent(p, "CUSTOM_KW");
        }

        /* 2. 뉴비 하한 */
        long newbieCount = picked.keySet().stream().filter(PairScore::newbie).count();
        for (PairScore p : scored) {
            if (newbieCount >= cfg.getNewbieFloor()) break;
            if (p.newbie() && !picked.containsKey(p)) {
                picked.put(p, "NEWBIE");
                newbieCount++;
            }
        }

        /* 3. 점수 상위 — core 자리 (랜덤·신규자 예약 제외) */
        int core = cfg.getDailyShow() - cfg.getRandomSlots() - cfg.getFreshNewbieReservedSlots();
        for (PairScore p : scored) {
            if (picked.size() >= core) break;
            picked.putIfAbsent(p, "SCORE");
        }

        /* 4. 랜덤 발견 */
        List<PairScore> rest = new ArrayList<>(scored);
        rest.removeAll(picked.keySet());
        Collections.shuffle(rest);
        for (PairScore p : rest.stream().limit(cfg.getRandomSlots()).toList()) {
            picked.put(p, "RANDOM");
        }

        /* 5. rank 부여 — 신규자 예약 자리는 skip */
        Set<Integer> reservedRanks = computeReservedRanks(cfg);
        List<DailyFeedRow> rows = new ArrayList<>(Math.min(picked.size(), cfg.getDailyShow()));
        int rank = 1;
        for (Map.Entry<PairScore, String> e : picked.entrySet()) {
            while (reservedRanks.contains(rank)) rank++;
            if (rank > cfg.getDailyShow()) break;
            PairScore p = e.getKey();
            rows.add(new DailyFeedRow(rank++, p.otherId(), sortKeys.get(p), e.getValue(),
                    formatter.renderJson(me, p)));
        }
        return rows;
    }

    /**
     * 신규자 예약 rank — 앞쪽에서 step 간격으로 reservedSlots 자리 선점.
     *  freshNewbieReservedSlots=5, freshNewbieReservedStep=5 → {5, 10, 15, 20, 25}
     *  step×i 가 dailyShow 초과면 그 자리는 제외 (자연 무시).
     *  freshNewbieReservedSlots=0 또는 step=0 이면 빈 Set.
     */
    public static Set<Integer> computeReservedRanks(MatchingConfig cfg) {
        int n = cfg.getFreshNewbieReservedSlots();
        int step = cfg.getFreshNewbieReservedStep();
        int total = cfg.getDailyShow();
        if (n <= 0 || step <= 0 || total <= 0) return Set.of();
        Set<Integer> result = new HashSet<>(n);
        for (int i = 1; i <= n; i++) {
            int rank = i * step;
            if (rank > total) break;
            result.add(rank);
        }
        return result;
    }
}
