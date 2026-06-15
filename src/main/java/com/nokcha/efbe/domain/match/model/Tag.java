package com.nokcha.efbe.domain.match.model;

import java.util.List;

/**
 *  - hasPercent=true 면 percent 노출 (#키워드 86% 등)
 *  - chips 는 KEYWORD / CATEGORY_MATE / CUSTOM_KW 의 키워드 칩 (빈도 낮은 것부터 N개)
 *  - label 은 CATEGORY_MATE 의 라벨 (#여가메이트 등) — 그 외 null
 */
public record Tag(
        TagType type,
        boolean hasPercent,
        int percent,
        String label,
        List<String> chips
) {
    public static Tag keyword(int p, List<String> chips)         { return new Tag(TagType.KEYWORD,       true,  p, null, chips); }
    public static Tag ideal(int p)                                { return new Tag(TagType.IDEAL,         true,  p, null, List.of()); }
    public static Tag iLike(int p)                                { return new Tag(TagType.I_LIKE,        true,  p, null, List.of()); }
    public static Tag likesMe(int p)                              { return new Tag(TagType.LIKES_ME,      true,  p, null, List.of()); }
    public static Tag lifestyle(int p)                            { return new Tag(TagType.LIFESTYLE,     true,  p, null, List.of()); }
    public static Tag nearby()                                    { return new Tag(TagType.NEARBY,        false, 0, null, List.of()); }
    public static Tag categoryMate(String label, List<String> ch) { return new Tag(TagType.CATEGORY_MATE, false, 0, label, ch); }
    public static Tag customKw(List<String> kws)                  { return new Tag(TagType.CUSTOM_KW,     false, 0, null, kws); }
    public static Tag totalOpposite()                             { return new Tag(TagType.TOTAL_OPPOSITE,false, 0, null, List.of()); }
}
