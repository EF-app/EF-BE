package com.nokcha.efbe.domain.match.calculator;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.profile.entity.Purpose;
import com.nokcha.efbe.domain.match.model.MatchUtil;
import com.nokcha.efbe.domain.match.model.PairScore;
import com.nokcha.efbe.domain.match.model.StyleScore;
import com.nokcha.efbe.domain.match.model.Tag;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.service.KeywordFreqService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 한 페어 (me ↔ other) → PairScore.
 *  4 영역 점수 + 태그 8 종 + sortKey 조립.
 *
 *  카테고리 라벨:
 *    OUTDOOR  → #여가메이트
 *    SELF_DEV → #자기계발러
 *    SPORTS   → #운동메이트
 */
@Service
@RequiredArgsConstructor
public class MatchCalculator {

    private final ScoreCalculator scoreCalc;
    private final KeywordFreqService keywordFreq;

    public PairScore score(UserContext me, UserContext other, MatchingConfig cfg) {
        double keyword   = scoreCalc.keyword(me, other, cfg);
        StyleScore ideal = scoreCalc.ideal(me, other, cfg);
        double lifestyle = scoreCalc.lifestyle(me, other);
        double location  = scoreCalc.location(me, other, cfg);

        // FRIEND 가드 — 이상형 계열 태그 전부 제외
        boolean skipIdeal = me.purpose() == Purpose.FRIEND
                         || other.purpose() == Purpose.FRIEND;

        Set<String> commonKeywords = MatchUtil.intersect(me.keywords(), other.keywords());
        Set<String> commonCustom   = MatchUtil.intersect(me.customKeywords(), other.customKeywords());

        List<Tag> tags = new ArrayList<>();
        List<String> categoryCodes = new ArrayList<>();

        // 1. #키워드
        if (keyword >= cfg.getKeywordTagThreshold()) {
            tags.add(Tag.keyword(MatchUtil.pct(keyword), chips(commonKeywords, cfg)));
        }

        // 2. 이상형 계열 — 가드: 해당 쪽이 실제 입력했을 때만
        if (!skipIdeal) {
            if (ideal.aHasIdeal() && ideal.bHasIdeal()
                    && ideal.aToB() >= cfg.getIdealBothMin()
                    && ideal.bToA() >= cfg.getIdealBothMin()) {
                tags.add(Tag.ideal(MatchUtil.pct(ideal.bidir())));
            }
            if (ideal.aHasIdeal() && ideal.aToB() >= cfg.getILikeThreshold()) {
                tags.add(Tag.iLike(MatchUtil.pct(ideal.aToB())));
            }
            if (ideal.bHasIdeal() && ideal.bToA() >= cfg.getLikesMeThreshold()) {
                tags.add(Tag.likesMe(MatchUtil.pct(ideal.bToA())));
            }
        }

        // 3. #라이프
        if (lifestyle >= cfg.getLifestyleTagThreshold()) {
            tags.add(Tag.lifestyle(MatchUtil.pct(lifestyle)));
        }

        // 4. #가까운지역 — % 표시 없음, 라벨만
        if (location >= cfg.getLocationTagThreshold()) {
            tags.add(Tag.nearby());
        }

        // 5. #같은카테고리 (OUTDOOR/SELF_DEV/SPORTS)
        for (String cat : cfg.getCategoryMateCats()) {
            Set<String> commonInCat = MatchUtil.intersect(
                    me.keywordsByCategory().getOrDefault(cat, Set.of()),
                    other.keywordsByCategory().getOrDefault(cat, Set.of())
            );
            if (commonInCat.size() >= cfg.getCategoryMateMin()) {
                tags.add(Tag.categoryMate(categoryLabel(cat), chips(commonInCat, cfg)));
                categoryCodes.add(cat);
            }
        }

        // 6. ✨#개인키워드
        boolean hasCustom = commonCustom.size() >= cfg.getCustomKwMin();
        if (hasCustom) {
            tags.add(Tag.customKw(chips(commonCustom, cfg)));
        }

        // 7. #정반대의매력 — 위 1~7 모두 미발동 + 키워드∩=0 + 커스텀∩=0
        boolean anyCoreTag = tags.stream().anyMatch(t -> switch (t.type()) {
            case KEYWORD, CATEGORY_MATE, CUSTOM_KW, IDEAL, I_LIKE, LIKES_ME, LIFESTYLE, NEARBY -> true;
            default -> false;
        });
        boolean totalOpposite = !anyCoreTag
                && commonKeywords.isEmpty()
                && commonCustom.isEmpty();
        if (totalOpposite) {
            tags.add(Tag.totalOpposite());
        }

        boolean newbie = isNewbie(other, cfg);

        return new PairScore(
                other.id(),
                keyword, ideal.bidir(), lifestyle, location,
                ideal.aToB(), ideal.bToA(),
                categoryCodes,
                hasCustom,
                totalOpposite,
                newbie,
                tags
        );
    }

    /** 공통 키워드 칩: 빈도 낮은(희귀한) 것부터 N개 */
    private List<String> chips(Set<String> common, MatchingConfig cfg) {
        return common.stream()
                .sorted(Comparator.comparingInt(keywordFreq::countOf))
                .limit(cfg.getKeywordChipCount())
                .toList();
    }

    private boolean isNewbie(UserContext u, MatchingConfig cfg) {
        return ChronoUnit.DAYS.between(u.signupAt(), LocalDate.now()) < cfg.getNewbieWindowDays();
    }

    /** 카테고리 코드 → 표시 라벨 */
    private String categoryLabel(String categoryCode) {
        return switch (categoryCode) {
            case "OUTDOOR"  -> "여가메이트";
            case "SELF_DEV" -> "자기계발러";
            case "SPORTS"   -> "운동메이트";
            default          -> categoryCode;  // 시드 외 값 들어오면 코드 그대로
        };
    }
}
