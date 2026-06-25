package com.nokcha.efbe.common.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Native query 결과의 DATETIME 컬럼 → {@link LocalDateTime} 변환.
 *  JDBC 드라이버/버전에 따라 {@code LocalDateTime} 또는 {@link Timestamp} 가 반환되므로 통일한다.
 *  JPA derived query / JPQL 은 Hibernate 가 자동 매핑하므로 이 유틸 불필요.
 *
 *  호출자가 컬럼의 nullable 여부를 메서드명으로 표시:
 *    - {@link #toLocalDateTime}        — NOT NULL 컬럼. null 또는 예상 외 타입은 throw.
 *    - {@link #toLocalDateTimeOrNull}  — nullable 컬럼. null 은 통과, 예상 외 타입만 throw.
 */
public final class JdbcTimeMapper {

    private JdbcTimeMapper() {}

    public static LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        throw new IllegalStateException(
                "DATETIME 타입 예상 외: " + (value == null ? "null" : value.getClass()));
    }

    public static LocalDateTime toLocalDateTimeOrNull(Object value) {
        if (value == null) return null;
        return toLocalDateTime(value);
    }
}
