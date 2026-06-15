package com.nokcha.efbe.infra.scheduler.match;

import java.util.concurrent.atomic.LongAdder;

/**
 * 04:00 정상 / 05:00 보정 배치의 phase 별 누계 측정 (배치 성능).
 *  Step 3 병렬화 대비 thread-safe (LongAdder). null 전달이면 측정 skip 패턴.
 *
 *  phase 정의:
 *    - buildPool: 후보 풀 + bbox + 셔플
 *    - score:     pool 전체 페어 점수 계산
 *    - select:    FeedSelector 슬롯 선정 + sortKey + tags_json 렌더
 *    - backfill:  emptyRanks 백필
 *    - replace:   DELETE + INSERT (replaceDailyFeed)
 *
 *  소요 ms 합 = wall-clock 아님 (병렬 처리 시 thread 누계). thread 수로 나눠 평균 활용.
 */
public final class BatchPhaseMetrics {

    public final LongAdder buildPoolNs = new LongAdder();
    public final LongAdder scoreNs     = new LongAdder();
    public final LongAdder selectNs    = new LongAdder();
    public final LongAdder backfillNs  = new LongAdder();
    public final LongAdder replaceNs   = new LongAdder();

    public final LongAdder viewersDone = new LongAdder();

    public long buildPoolMs() { return buildPoolNs.sum() / 1_000_000; }
    public long scoreMs()     { return scoreNs.sum() / 1_000_000; }
    public long selectMs()    { return selectNs.sum() / 1_000_000; }
    public long backfillMs()  { return backfillNs.sum() / 1_000_000; }
    public long replaceMs()   { return replaceNs.sum() / 1_000_000; }

    public String summary() {
        return String.format(
                "buildPool=%dms score=%dms select=%dms backfill=%dms replace=%dms viewers=%d",
                buildPoolMs(), scoreMs(), selectMs(), backfillMs(), replaceMs(),
                viewersDone.sum());
    }
}
