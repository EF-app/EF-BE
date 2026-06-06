package com.nokcha.efbe.domain.match.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 매칭 설정값 POJO — 배치 시작 시 {@link MatchingConfigLoader} 가 code_match_config 로 덮어쓴다.
 *  필드 기본값 = 코드 폴백 (DB 키 누락 시 사용).
 *
 *  ※ 임계값·가중치 하드코딩 금지 원칙: 모든 매칭 로직은 이 객체만 본다.
 *  ※ 명세서: EF_매칭로직_통합 명세서_v1_0.md §6.2 참고.
 *
 *  ── 명명 통일 (Phase 1) ──
 *    5 영역 = Keyword / Ideal / Lifestyle / Location.
 *    "interest" / "style" / "life" 명명 폐기.
 */
@Getter
@Setter
public class MatchingConfig {

    /* ─── 1. 후보 필터 ─── */
    private int ageMaxDiff       = 8;   // 나이차 상한
    private int lastActiveDays   = 31;  // 활성 viewer 임계값 (휴면 기준)
    private int passCooldownDays = 30;  // PASS 쿨다운

    /* ─── 2. 풀 ─── */
    private int poolSize         = 500; // 후보 풀
    private double newbieRatio   = 0.40;    // 뉴비 양동이 비율
    private int newbieWindowDays = 3;   // 가입 후 N일 = 뉴비
    /** 반경 확장 step (km). -1 = 전국. */
    private int[] radiusStepsKm  = {20, 50, 100, -1};   // 반경 확장 단계

    /* ─── 3. 키워드 ─── */
    private double keywordBase         = 0.40;
    private double keywordCoef         = 0.60;
    private double keywordTagThreshold = 0.50;

    /* ─── 4. 이상형 ─── */
    private double idealBothMin     = 0.45;
    private double iLikeThreshold   = 0.65;
    private double likesMeThreshold = 0.65;
    private int    idealMinFields   = 3;
    private double idealFewPenalty  = 0.80;

    /* ─── 5. 라이프 ─── */
    private double lifestyleTagThreshold = 0.60;

    /* ─── 6. 지역 ─── */
    /** km 구간 → 점수. 명세서 §2.4 의 5단계. */
    private double[][] regionTiers       = {{5, 1.0}, {20, 0.8}, {50, 0.6}, {100, 0.4}, {99999, 0.2}};
    private double locationTagThreshold  = 0.60;

    /* ─── 7. sortKey 가중치 ─── */
    private double weightKeyword   = 0.40;
    private double weightIdeal     = 0.35;
    private double weightLifestyle = 0.10;
    private double weightLocation  = 0.15;

    /**
     * 중요포인트 차등 가산 (명세서 §2.5).
     *  영역별 평균 도달치가 다르므로 동일 가산은 강조 의도를 묽힘.
     *  잘 안 오르는 영역(키워드·이상형)에 더 큰 가산.
     */
    private double bumpKeyword   = 0.15;   // 키워드 평균 ~0.50, 능동 선택
    private double bumpIdeal     = 0.20;   // 이상형 평균 ~0.48, 가장 도달 어려움
    private double bumpLifestyle = 0.05;   // 라이프 평균 ~0.78, 이미 잘 나옴
    private double bumpLocation  = 0.05;   // 지역 평균 ~0.70+, 풀 단계 반경 필터로 이미 가까움

    /* ─── 8. 같은카테고리 ─── */
    private List<String> categoryMateCats = List.of("OUTDOOR", "SELF_DEV", "SPORTS");   // 같은카테고리 타입
    private int categoryMateMin           = 2;

    /* ─── 9. 개인키워드 ─── */
    private int customKwMin = 1;

    /* ─── 10. 슬롯 ─── */
    private int dailyShow   = 50;   // 노출 cap
    private int newbieFloor = 10;   // 뉴비 하한
    private int randomSlots = 5;    // 랜덤 자리

    /* ─── 11. 표시 ─── */
    private int keywordChipCount = 3;   // 키워드 몇 개 나올지

    /* ─── 12. ProfileChangeListener 어뷰즈 가드 (§10.22) ─── */
    /** 본인이 오늘 한 액션 수가 이 값 이상이면 프로필 변경 시 재계산 차단. */
    private int recomputeActionThreshold = 5;
    /** 일일 프로필 변경 재계산 최대 횟수. 0 = 즉시 재계산 비활성. */
    private int recomputeMaxPerDay = 1;

    /* ─── 13. 신규자 fan-out (매시간 미니 배치) ─── */
    /** 가입 후 N 시간 이내면 "신규자" 로 간주 — 04:00 정상 배치 갭(최대 28h) 완화용. */
    private int freshNewbieWindowHours = 24;
    /** 신규자 1 명이 등장할 viewer 최대 수. 시간당 INSERT 부하 cap. */
    private int freshNewbieFanOut       = 200;
    /** 50 cap 안에서 신규자에게 예약된 슬롯 수. 04:00 배치가 비워두고 미니 배치가 채움. */
    private int freshNewbieReservedSlots = 5;   // 신규자 예약 자리 (RecentNewbieBatch 가 채움)
    /**
     * 신규자 예약 자리의 rank 간격 (step).
     *  reservedRanks = {step×1, step×2, ..., step×reservedSlots}
     *  기본 5 → {5, 10, 15, 20, 25} (앞쪽 절반에 분산, 노출 우선).
     *  step×reservedSlots > dailyShow 면 dailyShow 초과 자리는 자연 무시.
     */
    private int freshNewbieReservedStep = 5;
}
