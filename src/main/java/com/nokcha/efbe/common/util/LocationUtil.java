package com.nokcha.efbe.common.util;

import com.nokcha.efbe.domain.area.entity.CodeArea;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class LocationUtil {

    public static String composeLocation(String country, String city) {
        boolean hasCountry = country != null && !country.isBlank();
        boolean hasCity = city != null && !city.isBlank();
        if (!hasCountry && !hasCity) {
            return null;
        }
        if (hasCountry && hasCity) {
            return country + " " + city;    // -> 생각해보니까 이걸 왜 써야하는지 모르겠는데?
        }
        return hasCountry ? country : city;
    }

    public static String composeLocation(CodeArea area) {
        if (area == null) {
            return null;
        }
        return composeLocation(area.getCountry(), area.getCity());
    }
}
