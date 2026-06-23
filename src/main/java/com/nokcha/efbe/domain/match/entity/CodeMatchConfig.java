package com.nokcha.efbe.domain.match.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 매칭 설정값 1 row — 관리자 홈에서 수정, 배치가 시작 시 전부 로드.
 *  config_value 는 스칼라 또는 JSON 문자열. JSON 파싱은 {@code MatchingConfigLoader} 가 담당.
 *
 *  create/update 시각·사용자는 {@link BaseEntity} 의 audit 4 필드로 자동 관리.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "code_match_config")
public class CodeMatchConfig extends BaseEntity {

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

    /** 관리자 PATCH 진입점 — value 갱신 (updateUser/updateTime 은 AuditingEntityListener 가 자동). */
    public void applyUpdate(String newValue) {
        this.configValue = newValue;
    }
}
