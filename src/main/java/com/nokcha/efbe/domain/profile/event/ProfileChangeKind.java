package com.nokcha.efbe.domain.profile.event;

/**
 * 프로필 변경 영향 분류.
 *  매칭 본인 피드 재계산 트리거 여부 결정:
 *    - AREA : 후보 풀 / 거리 / 국내·해외 그룹 자체가 바뀜 → 즉시 재계산
 *    - 그 외 변경 (점수만 영향 — keywords/personals/lifestyle/ideal 6필드) → 다음 04:00 배치 (eventual)
 *    - 표시 항목 (nickname/photo/bio/mbti) → 카드 렌더링이 실시간 join → 즉시 보임
 *
 *  ※ IDEAL_POINTS 는 즉시 재계산 대상에서 제외 (어뷰즈 통로 차단).
 *     다음 04:00 배치 때 sortKey 가중치 반영.
 */
public enum ProfileChangeKind {
    AREA;

    public boolean triggersRecompute() {
        return this == AREA;
    }
}
