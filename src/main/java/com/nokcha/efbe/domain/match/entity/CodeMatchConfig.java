package com.nokcha.efbe.domain.match.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 매칭 설정값 1 row — 관리자 홈에서 수정, 배치가 시작 시 전부 로드.
 *  config_value 는 스칼라 또는 JSON 문자열. JSON 파싱은 {@code MatchingConfigLoader} 가 담당.
 *
 *  스키마: {@code src/main/resources/sql/migration_match.sql} 의 1 번 섹션 참고.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "code_match_config")
public class CodeMatchConfig {

    @Id
    @Column(name = "config_key", length = 60, nullable = false)
    private String configKey;

    @Column(name = "config_value", length = 255, nullable = false)
    private String configValue;

    /** "INT" / "DOUBLE" / "JSON" — 로더가 분기에 사용 */
    @Column(name = "value_type", length = 10, nullable = false)
    private String valueType;

    @Column(name = "description", length = 200)
    private String description;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;
}
