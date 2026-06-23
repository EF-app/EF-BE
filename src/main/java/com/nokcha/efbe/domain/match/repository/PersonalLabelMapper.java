package com.nokcha.efbe.domain.match.repository;

import com.nokcha.efbe.domain.match.model.BodyType;
import com.nokcha.efbe.domain.match.model.Drinking;
import com.nokcha.efbe.domain.match.model.Fashion;
import com.nokcha.efbe.domain.match.model.Grooming;
import com.nokcha.efbe.domain.match.model.HairLength;
import com.nokcha.efbe.domain.match.model.HeightBand;
import com.nokcha.efbe.domain.match.model.Smoking;
import com.nokcha.efbe.domain.match.model.Tendency;
import com.nokcha.efbe.domain.profile.entity.CodePersonal;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * code_personal (bigCategory, smallCategory) → 매칭 enum 변환.
 *  실제 운영 시드 ({@code CodePersonalDataInitializer}) 의 한국어 라벨 기준.
 *
 *  ┌─ bigCategory ─┬─ smallCategory (한국어) → enum ──────────────────────────┐
 *  │ 머리          │ 숏컷, 단발~중단발, 긴머리 → HairLength                    │
 *  │ 체형          │ 슬림, 보통, 통통, 통통 이상 → BodyType                    │
 *  │ 키            │ 150이하, 151~155, 156~160, 160~165, 166~170, 171이상 → HeightBand │
 *  │ 성향          │ 온깁, 깁선호, 텍선호, 온텍, 플라토닉 → Tendency           │
 *  │ 음주          │ 아예 안 마심, 가끔, 꽤, 자주, 금주 중 → Drinking          │
 *  │ 흡연          │ 비흡연자, 아주 가끔, 때때로, 흡연자, 금연 중 → Smoking    │
 *  │ 패션 스타일   │ 캐주얼, 스트릿, 미니멀, 댄디, 스포티, 빈티지, 기타 → Fashion │
 *  │ 꾸미는 스타일 │ 꾸미는 걸 좋아해요, 자연스러운 꾸안꾸, … → Grooming       │
 *  └───────────────┴────────────────────────────────────────────────────────────┘
 *
 *  "선택 안함" 은 모든 카테고리에서 null (DontCare) — ScoreCalculator.ideal 이 평가 스킵.
 *
 *  주의: 시드 라벨 변경 시 이 파일과 함께 갱신 필요.
 *   (Stage 4 약속: smallCategory=enum name 은 운영 시드와 어긋남 → 한국어 매핑으로 통일)
 */
@Slf4j
public final class PersonalLabelMapper {

    /* ── bigCategory 한국어 키 (CodePersonalDataInitializer 와 일치) ── */
    public static final String BIG_HAIR     = "머리";
    public static final String BIG_BODY     = "체형";
    public static final String BIG_HEIGHT   = "키";
    public static final String BIG_TENDENCY = "성향";
    public static final String BIG_DRINKING = "음주";
    public static final String BIG_SMOKING  = "흡연";
    public static final String BIG_FASHION  = "패션 스타일";
    public static final String BIG_GROOMING = "꾸미는 스타일";

    private static final String NO_CHOICE = "선택 안함";

    /* ── smallCategory → enum 매핑 테이블 ── */

    private static final Map<String, HairLength> HAIR = Map.of(
            "숏컷",       HairLength.SHORT,
            "단발~중단발", HairLength.MEDIUM,
            "긴머리",     HairLength.LONG
    );

    private static final Map<String, BodyType> BODY = Map.of(
            "슬림",       BodyType.SLIM,
            "보통",       BodyType.NORMAL,
            "통통",       BodyType.CHUBBY,
            "통통 이상",  BodyType.MORE
    );

    private static final Map<String, HeightBand> HEIGHT = Map.of(
            "150이하",  HeightBand.UNDER_150,
            "151~155", HeightBand.H_151_155,
            "156~160", HeightBand.H_156_160,
            "160~165", HeightBand.H_161_165,   // 시드 표기는 160~165, enum 은 H_161_165
            "166~170", HeightBand.H_166_170,
            "171이상",  HeightBand.H_171_PLUS
    );

    private static final Map<String, Tendency> TENDENCY = Map.of(
            "온깁",      Tendency.ON_GIP,
            "깁선호",    Tendency.GIP_PREF,
            "텍선호",    Tendency.TXT_PREF,
            "온텍",      Tendency.ON_TXT,
            "플라토닉",  Tendency.PLATONIC
    );

    private static final Map<String, Drinking> DRINKING = Map.of(
            "아예 안 마심", Drinking.NEVER,
            "금주 중",      Drinking.QUIT,
            "가끔 마심",    Drinking.RARE,
            "꽤 마심",      Drinking.MODERATE,
            "자주 마심",    Drinking.OFTEN
    );

    private static final Map<String, Smoking> SMOKING = Map.of(
            "비흡연자",       Smoking.NEVER,
            "금연 중",        Smoking.QUIT,
            "아주 가끔 피움", Smoking.RARE,
            "때때로 피움",    Smoking.SOMETIMES,
            "흡연자",         Smoking.REGULAR
    );

    private static final Map<String, Fashion> FASHION = Map.of(
            "캐주얼", Fashion.CASUAL,
            "스트릿", Fashion.STREET,
            "미니멀", Fashion.MINIMAL,
            "댄디",   Fashion.DANDY,
            "스포티", Fashion.SPORTY,
            "빈티지", Fashion.VINTAGE,
            "기타",   Fashion.ETC
    );

    private static final Map<String, Grooming> GROOMING = Map.of(
            "꾸미는 걸 좋아해요",   Grooming.LIKE_GROOMING,
            "자연스러운 꾸안꾸",   Grooming.NATURAL,
            "깔끔하게 신경 써요",  Grooming.CLEAN,
            "편한 게 좋아요",      Grooming.COMFORTABLE,
            "상황에 따라 달라요",  Grooming.SITUATIONAL,
            "기타",                Grooming.ETC
    );

    private PersonalLabelMapper() {}

    public static HairLength toHair(CodePersonal cp)     { return lookup(cp, BIG_HAIR,     HAIR); }
    public static BodyType toBody(CodePersonal cp)       { return lookup(cp, BIG_BODY,     BODY); }
    public static HeightBand toHeight(CodePersonal cp)   { return lookup(cp, BIG_HEIGHT,   HEIGHT); }
    public static Tendency toTendency(CodePersonal cp)   { return lookup(cp, BIG_TENDENCY, TENDENCY); }
    public static Drinking toDrinking(CodePersonal cp)   { return lookup(cp, BIG_DRINKING, DRINKING); }
    public static Smoking toSmoking(CodePersonal cp)     { return lookup(cp, BIG_SMOKING,  SMOKING); }
    public static Fashion toFashion(CodePersonal cp)     { return lookup(cp, BIG_FASHION,  FASHION); }
    public static Grooming toGrooming(CodePersonal cp)   { return lookup(cp, BIG_GROOMING, GROOMING); }

    private static <E extends Enum<E>> E lookup(CodePersonal cp, String expectBig,
                                                Map<String, E> table) {
        if (cp == null) return null;
        if (!expectBig.equals(cp.getBigCategory())) {
            return null;  // 다른 카테고리 row — 호출자 분기에서 정상.
        }
        String small = cp.getSmallCategory();
        if (NO_CHOICE.equals(small)) return null;  // 선택 안함 → DontCare
        E val = table.get(small);
        if (val == null) {
            log.warn("[PersonalLabelMapper] 알 수 없는 라벨 — big={}, small={}, id={}",
                    cp.getBigCategory(), small, cp.getId());
        }
        return val;
    }
}
