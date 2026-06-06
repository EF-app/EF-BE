package com.nokcha.efbe.domain.match.tag;

import com.nokcha.efbe.domain.match.model.ImportantPoint;
import com.nokcha.efbe.domain.match.model.MatchUtil;
import com.nokcha.efbe.domain.match.model.PairScore;
import com.nokcha.efbe.domain.match.model.Tag;
import com.nokcha.efbe.domain.match.model.TagType;
import com.nokcha.efbe.domain.match.model.UserContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PairScore.tags → 카드 표시용 tags_json 그룹핑 (명세서 §3.6 + 부록 B).
 *
 *  ── 그룹 규칙 ──────────────────────────────────────────────
 *    (1) 키워드 묶음  : KEYWORD + CATEGORY_MATE (라벨만 categories[]로 부착)
 *    (2) 이상형 묶음  : IDEAL / I_LIKE / LIKES_ME → 1 개 선택
 *    (3) 개인키워드   : CUSTOM_KW
 *    (4) 정반대       : TOTAL_OPPOSITE (단독)
 *    (5) 라이프·지역  : LIFESTYLE / NEARBY (P3, 뒤로)
 *
 *  ⭐ 중요 포인트 일치 시 star=true (§3.6).
 *  데일리 피드는 이미 me 관점 → 추가 반전 불필요.
 */
@Component
public class TagDisplayFormatter {

    public String renderJson(UserContext me, PairScore ps) {
        Set<ImportantPoint> ip = me.importantPoints();
        Map<TagType, Tag> idx = indexFirstByType(ps.tags());
        List<Map<String, Object>> out = new ArrayList<>();

        /* (1) 키워드 묶음 */
        Tag keyword = idx.get(TagType.KEYWORD);
        List<Tag> cats = ps.tags().stream()
                .filter(t -> t.type() == TagType.CATEGORY_MATE)
                .toList();
        if (keyword != null) {
            Map<String, Object> block = baseTag(TagType.KEYWORD, keyword, ip);
            if (!cats.isEmpty()) {
                block.put("categories", cats.stream().map(Tag::label).toList());
            }
            out.add(block);
        } else if (!cats.isEmpty()) {
            out.add(baseTag(TagType.CATEGORY_MATE, cats.get(0), ip));
        }

        /* (2) 이상형 묶음 — 1 개 선택 */
        Tag ideal = pickIdealTag(idx);
        if (ideal != null) out.add(baseTag(ideal.type(), ideal, ip));

        /* (3) 개인키워드 */
        if (idx.containsKey(TagType.CUSTOM_KW)) {
            out.add(baseTag(TagType.CUSTOM_KW, idx.get(TagType.CUSTOM_KW), ip));
        }

        /* (4) 정반대 단독 */
        if (idx.containsKey(TagType.TOTAL_OPPOSITE)) {
            out.add(Map.of("type", TagType.TOTAL_OPPOSITE.name()));
        }

        /* (5) 라이프 / 지역 — P3 (뒤) */
        if (idx.containsKey(TagType.LIFESTYLE)) {
            out.add(baseTag(TagType.LIFESTYLE, idx.get(TagType.LIFESTYLE), ip));
        }
        if (idx.containsKey(TagType.NEARBY)) {
            out.add(baseTag(TagType.NEARBY, idx.get(TagType.NEARBY), ip));
        }

        return MatchUtil.toJson(out);
    }

    /**
     * 이상형 3 태그 (IDEAL / I_LIKE / LIKES_ME) → 1 개.
     *  양쪽 다 강하면 IDEAL, 한쪽만이면 그 방향 — TagDeterminer 가 가드 통과한 태그만 넘긴다는 전제.
     */
    private Tag pickIdealTag(Map<TagType, Tag> idx) {
        boolean iLike   = idx.containsKey(TagType.I_LIKE);
        boolean likesMe = idx.containsKey(TagType.LIKES_ME);
        if (iLike && likesMe) {
            return idx.getOrDefault(TagType.IDEAL, idx.get(TagType.I_LIKE));
        }
        if (iLike)   return idx.get(TagType.I_LIKE);
        if (likesMe) return idx.get(TagType.LIKES_ME);
        return idx.get(TagType.IDEAL);
    }

    private Map<String, Object> baseTag(TagType type, Tag tag, Set<ImportantPoint> ip) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type.name());
        if (tag.hasPercent()) m.put("percent", tag.percent());
        if (tag.label() != null) m.put("label", tag.label());
        if (tag.chips() != null && !tag.chips().isEmpty()) m.put("chips", tag.chips());
        if (isStarred(type, ip)) m.put("star", true);
        return m;
    }

    private Map<TagType, Tag> indexFirstByType(List<Tag> tags) {
        Map<TagType, Tag> map = new EnumMap<>(TagType.class);
        for (Tag t : tags) map.putIfAbsent(t.type(), t);
        return map;
    }

    /** 중요 포인트 별 ⭐ 표시 (§3.6). */
    private boolean isStarred(TagType type, Set<ImportantPoint> ip) {
        if (ip == null || ip.isEmpty()) return false;
        return switch (type) {
            case KEYWORD, CATEGORY_MATE, CUSTOM_KW -> ip.contains(ImportantPoint.KEYWORD);
            case IDEAL, I_LIKE, LIKES_ME           -> ip.contains(ImportantPoint.IDEAL);
            case LIFESTYLE                         -> ip.contains(ImportantPoint.LIFESTYLE);
            case NEARBY                            -> ip.contains(ImportantPoint.LOCATION);
            default                                -> false;
        };
    }
}
