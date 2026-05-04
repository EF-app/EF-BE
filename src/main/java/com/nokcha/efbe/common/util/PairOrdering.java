package com.nokcha.efbe.common.util;

// 매칭 페어 정렬 / 해시 유틸 (user_a_id < user_b_id 규약 강제)
// match_results.UNIQUE(user_a_id, user_b_id) 가 정렬 저장을 요구하므로 INSERT 경로 전체에서 이 유틸을 사용한다.
public final class PairOrdering {

    private PairOrdering() {}

    // 두 user id 를 작은→큰 순으로 정렬 반환 — 호출 측은 [0]=user_a, [1]=user_b 로 사용
    public static Long[] ordered(Long a, Long b) {
        if (a == null || b == null) throw new IllegalArgumentException("null user id");
        return a <= b ? new Long[]{a, b} : new Long[]{b, a};
    }

    // 페어 중복 체크용 64bit 해시 — small << 32 | large. 동일 페어면 동일 값 반환
    public static long hash(Long a, Long b) {
        Long[] p = ordered(a, b);
        return (p[0] << 32) | (p[1] & 0xFFFFFFFFL);
    }

    // chat_room.pair_key 포맷 — "{small}-{large}"
    public static String pairKey(Long a, Long b) {
        Long[] p = ordered(a, b);
        return p[0] + "-" + p[1];
    }
}
