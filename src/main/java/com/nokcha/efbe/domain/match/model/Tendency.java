package com.nokcha.efbe.domain.match.model;

/**
 * 성향 — code_personal "성향" 카테고리 매핑.
 *  스펙트럼 4 (ordinal 0~3) + 특수 1 (PLATONIC).
 *  의사코드/이전 명세서의 GIP_TXT 는 우리 시드에 없음 → enum 에서 제외 (분기는 발동 안 함).
 *
 *  명세서 부록 A 매트릭스:
 *   - PLATONIC ↔ PLATONIC = 1.0,    그 외 = 0.1
 *   - 스펙트럼 vs 스펙트럼:
 *       d=0: 끝단(ON_GIP/ON_TXT)이면 0.0(충돌), 중간(GIP_PREF/TXT_PREF)이면 0.3(온건)
 *       d=1: 0.2,  d=2: 0.9,  d=3: 1.0
 */
public enum Tendency {
    ON_GIP,    // 온깁     — 스펙트럼 0
    GIP_PREF,  // 깁선호   — 스펙트럼 1
    TXT_PREF,  // 텍선호   — 스펙트럼 2
    ON_TXT,    // 온텍     — 스펙트럼 3
    PLATONIC;  // 플라토닉 — 특수

    public boolean isSpectrum() {
        return this != PLATONIC;
    }
}
