package com.nokcha.efbe.domain.match.feed;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.model.DailyFeedRow;
import com.nokcha.efbe.domain.match.model.PairScore;
import com.nokcha.efbe.domain.match.model.Tag;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.fixture.UserContextBuilder;
import com.nokcha.efbe.domain.match.tag.TagDisplayFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 체크리스트 (슬롯):
 *  - 개인키워드 보장 (CUSTOM_KW)
 *  - 점수 상위 (SCORE) — dailyShow - randomSlots
 *  - 뉴비 하한 (NEWBIE) — newbieFloor 까지 채움
 *  - 랜덤 발견 (RANDOM) — randomSlots
 *  - 총 dailyShow 개 ↑로 자르기
 *  - rank 부여 1..N
 */
class FeedSelectorTest {

    /** 실제 formatter + SortKeyCalculator 사용 — sortKey 계산이 FeedSelector 안으로 이동. */
    private final FeedSelector selector = new FeedSelector(
            new TagDisplayFormatter(),
            new com.nokcha.efbe.domain.match.calculator.SortKeyCalculator());

    private MatchingConfig cfgWith(int daily, int newbieFloor, int randomSlots) {
        MatchingConfig cfg = new MatchingConfig();
        cfg.setDailyShow(daily);
        cfg.setNewbieFloor(newbieFloor);
        cfg.setRandomSlots(randomSlots);
        cfg.setFreshNewbieReservedSlots(0);  // FeedSelector 슬롯 테스트는 신규자 예약 영향 0 로 격리
        return cfg;
    }

    /**
     * 영역 점수 4개를 sortKey 값과 동일하게 세팅 → SortKeyCalculator 가
     * 가중치 합 1 로 정규화하므로 결과 sortKey = 그 값. 테스트의 의도된 순서 보존.
     */
    private static PairScore pair(long otherId, double sortKey, boolean newbie, boolean hasCustom) {
        return new PairScore(
                otherId,
                sortKey, sortKey, sortKey, sortKey,
                0.5, 0.5,
                List.of(),
                hasCustom,
                false,
                newbie,
                List.of()
        );
    }

    @Test
    @DisplayName("총 dailyShow 개 + rank 1..N — 우선순위는 sortKey 내림차순")
    void totalCountAndRank() {
        MatchingConfig cfg = cfgWith(10, 0, 0);
        UserContext me = UserContextBuilder.builder().id(1).build();
        List<PairScore> scored = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            scored.add(pair(100 + i, i * 0.01, false, false));  // sortKey 0.00 ~ 0.19
        }

        List<DailyFeedRow> rows = selector.select(me, scored, cfg);

        assertEquals(10, rows.size());
        for (int i = 0; i < rows.size(); i++) assertEquals(i + 1, rows.get(i).matchRank());
        // sortKey 내림차순 → 상위 10명 (sortKey 0.19 ~ 0.10)
        assertTrue(rows.get(0).sortKey() > rows.get(rows.size() - 1).sortKey());
    }

    @Test
    @DisplayName("개인키워드 보장 — hasCustomKeyword 가 sortKey 낮아도 무조건 포함")
    void customKeywordGuaranteed() {
        MatchingConfig cfg = cfgWith(3, 0, 0);  // 3 자리 (core=3)
        UserContext me = UserContextBuilder.builder().id(1).build();
        List<PairScore> scored = new ArrayList<>();
        scored.add(pair(100, 0.9, false, false));
        scored.add(pair(101, 0.8, false, false));
        scored.add(pair(102, 0.7, false, false));
        scored.add(pair(103, 0.05, false, true));  // 개인키워드 — sortKey 최하

        List<DailyFeedRow> rows = selector.select(me, scored, cfg);

        assertEquals(3, rows.size());
        assertTrue(rows.stream().anyMatch(r -> r.targetId() == 103L),
                "개인키워드 보유자(103)는 sortKey 낮아도 포함되어야");
    }

    @Test
    @DisplayName("뉴비 하한 — newbieFloor=3 보장, 점수 낮아도 뉴비 채움")
    void newbieFloor() {
        MatchingConfig cfg = cfgWith(5, 3, 0);  // 5 자리, 뉴비 하한 3
        UserContext me = UserContextBuilder.builder().id(1).build();
        List<PairScore> scored = new ArrayList<>();
        // 베테랑 5명 점수 높음
        for (int i = 0; i < 5; i++) scored.add(pair(100 + i, 0.9 - i*0.01, false, false));
        // 뉴비 3명 점수 낮음
        for (int i = 5; i < 8; i++) scored.add(pair(100 + i, 0.05 + i*0.001, true, false));

        List<DailyFeedRow> rows = selector.select(me, scored, cfg);

        assertEquals(5, rows.size());
        long newbieRows = rows.stream()
                .filter(r -> "NEWBIE".equals(r.slotType()) || isNewbieTarget(r, scored))
                .count();
        // 뉴비 슬롯이 최소 (5-2)=3, 또는 SCORE 슬롯에 뉴비가 들어와도 3 이상
        long newbieByTarget = rows.stream()
                .filter(r -> scored.stream().anyMatch(p -> p.otherId() == r.targetId() && p.newbie()))
                .count();
        assertTrue(newbieByTarget >= 3, "뉴비 하한 3 보장, 실제 뉴비 수=" + newbieByTarget);
    }

    @Test
    @DisplayName("랜덤 슬롯 — randomSlots=2 만큼 RANDOM slot_type 부여")
    void randomSlotsAllocation() {
        MatchingConfig cfg = cfgWith(5, 0, 2);
        UserContext me = UserContextBuilder.builder().id(1).build();
        List<PairScore> scored = new ArrayList<>();
        for (int i = 0; i < 10; i++) scored.add(pair(100 + i, 0.5 - i*0.01, false, false));

        List<DailyFeedRow> rows = selector.select(me, scored, cfg);

        long randomCount = rows.stream().filter(r -> "RANDOM".equals(r.slotType())).count();
        assertEquals(2, randomCount, "RANDOM 슬롯 정확히 2");
    }

    private boolean isNewbieTarget(DailyFeedRow row, List<PairScore> scored) {
        return scored.stream().anyMatch(p -> p.otherId() == row.targetId() && p.newbie());
    }
}
