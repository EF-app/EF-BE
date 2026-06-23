package com.nokcha.efbe.domain.match.repository;

import java.util.Map;

/**
 * MatchingConfigLoader 가 의존하는 좁은 인터페이스 — 테스트에서 lambda stub 가능.
 *  production 에선 {@link MatchConfigRepository} 가 구현.
 */
public interface MatchConfigSource {

    /** 전체 설정값을 Map(key → value) 으로 반환. */
    Map<String, String> findAllAsMap();
}
