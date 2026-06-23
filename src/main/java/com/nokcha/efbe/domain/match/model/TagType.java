package com.nokcha.efbe.domain.match.model;

/**
 * 8 종 태그 + 그룹핑 보조 enum.
 *
 *  우선순위 (표시 순서):
 *    P1: KEYWORD, CUSTOM_KW, IDEAL, I_LIKE/LIKES_ME(≥70%)
 *    P2: CATEGORY_MATE, I_LIKE/LIKES_ME(<70%)
 *    P3: LIFESTYLE, NEARBY
 *    단독: TOTAL_OPPOSITE
 */
public enum TagType {
    KEYWORD,         // #키워드 (구 #관심사)
    CATEGORY_MATE,   // #여가메이트/#자기계발러/#운동메이트
    CUSTOM_KW,       // ✨#개인키워드
    IDEAL,           // #이상형
    I_LIKE,          // #내가좋아하는 (관점 반전)
    LIKES_ME,        // #나를좋아하는 (관점 반전)
    LIFESTYLE,       // #라이프
    NEARBY,          // #가까운지역
    TOTAL_OPPOSITE   // #정반대의매력
}
