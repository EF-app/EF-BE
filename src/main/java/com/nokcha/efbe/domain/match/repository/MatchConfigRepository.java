package com.nokcha.efbe.domain.match.repository;

import com.nokcha.efbe.domain.match.entity.CodeMatchConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 통합 지점 2 — code_match_config 전체 조회.
 *  배치 시작 시 {@link com.nokcha.efbe.domain.match.config.MatchingConfigLoader} 가 1 회 호출.
 */
public interface MatchConfigRepository extends JpaRepository<CodeMatchConfig, String>, MatchConfigSource {

    /** 전체 설정값을 Map(key → value) 으로 반환. 로더가 key 별 분기. */
    @Override
    default Map<String, String> findAllAsMap() {
        return findAll().stream()
                .collect(Collectors.toMap(CodeMatchConfig::getConfigKey, CodeMatchConfig::getConfigValue));
    }
}
