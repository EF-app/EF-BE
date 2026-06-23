package com.nokcha.efbe.domain.match.fixture;

import com.nokcha.efbe.domain.match.model.BodyType;
import com.nokcha.efbe.domain.match.model.Drinking;
import com.nokcha.efbe.domain.match.model.Fashion;
import com.nokcha.efbe.domain.match.model.Grooming;
import com.nokcha.efbe.domain.match.model.HairLength;
import com.nokcha.efbe.domain.match.model.HeightBand;
import com.nokcha.efbe.domain.match.model.Ideal;
import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import com.nokcha.efbe.domain.profile.entity.Purpose;
import com.nokcha.efbe.domain.match.model.Self;
import com.nokcha.efbe.domain.match.model.Smoking;
import com.nokcha.efbe.domain.match.model.Tendency;
import com.nokcha.efbe.domain.match.model.UserContext;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * 체크리스트 테스트용 UserContext 빌더.
 *  record 라 자체 builder 가 없어 mutable 빌더를 fixture 로 둠.
 *  의도적으로 production 코드에 두지 않음 — 테스트 한정.
 */
public final class UserContextBuilder {

    private long id = 1L;
    private int age = 27;
    private LocalDate signupAt = LocalDate.now().minusDays(30);
    private String regionCountry = "한국";
    private double lat = 37.5;
    private double lon = 127.0;
    private Purpose purpose = Purpose.MIXED;
    private Set<String> keywords = Set.of();
    private Set<String> customKeywords = Set.of();
    private Map<String, Set<String>> keywordsByCategory = Map.of();
    private Ideal ideal = noIdeal();
    private Self self = blankSelf();
    private Drinking drinking = Drinking.NEVER;
    private Smoking smoking = Smoking.NEVER;
    private Set<IdealPointType> importantPoints = Set.of();

    public static UserContextBuilder builder() { return new UserContextBuilder(); }

    public UserContextBuilder id(long v)                                  { this.id = v; return this; }
    public UserContextBuilder age(int v)                                  { this.age = v; return this; }
    public UserContextBuilder signupAt(LocalDate v)                       { this.signupAt = v; return this; }
    public UserContextBuilder regionCountry(String v)                     { this.regionCountry = v; return this; }
    public UserContextBuilder coord(double la, double lo)                 { this.lat = la; this.lon = lo; return this; }
    public UserContextBuilder purpose(Purpose v)                          { this.purpose = v; return this; }
    public UserContextBuilder keywords(Set<String> v)                     { this.keywords = v; return this; }
    public UserContextBuilder customKeywords(Set<String> v)               { this.customKeywords = v; return this; }
    public UserContextBuilder keywordsByCategory(Map<String, Set<String>> v) { this.keywordsByCategory = v; return this; }
    public UserContextBuilder ideal(Ideal v)                              { this.ideal = v; return this; }
    public UserContextBuilder self(Self v)                                { this.self = v; return this; }
    public UserContextBuilder drinking(Drinking v)                        { this.drinking = v; return this; }
    public UserContextBuilder smoking(Smoking v)                          { this.smoking = v; return this; }
    public UserContextBuilder importantPoints(Set<IdealPointType> v)      { this.importantPoints = v; return this; }

    public UserContext build() {
        return new UserContext(
                id, age, signupAt, regionCountry, lat, lon, purpose,
                keywords, customKeywords, keywordsByCategory,
                ideal, self, drinking, smoking, importantPoints
        );
    }

    /* ─── 자주 쓰는 프리셋 ─── */

    public static Ideal noIdeal() {
        return new Ideal(null, null, null, null, Set.of(), null);
    }

    public static Self blankSelf() {
        return new Self(null, null, null, null, Set.of(), null);
    }

    public static Self self(HairLength hair, BodyType body, HeightBand height,
                            Tendency tendency, Set<Fashion> fashion, Grooming grooming) {
        return new Self(hair, body, height, tendency, fashion, grooming);
    }

    public static Ideal ideal(HairLength hair, BodyType body, HeightBand height,
                              Tendency tendency, Set<Fashion> fashion, Grooming grooming) {
        return new Ideal(hair, body, height, tendency, fashion, grooming);
    }
}
