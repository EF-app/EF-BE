package com.nokcha.efbe.domain.admin.match.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.match.dto.request.AdminMatchConfigUpdateReqDto;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminMatchConfigItemRspDto;
import com.nokcha.efbe.domain.match.entity.CodeMatchConfig;
import com.nokcha.efbe.domain.match.repository.MatchConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * code_match_config 관리자 운영.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMatchConfigService {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final double WEIGHT_SUM_TOLERANCE = 0.01;
    private static final List<String> WEIGHT_KEYS = List.of(
            "weight_keyword", "weight_ideal", "weight_lifestyle", "weight_location"
    );

    private final MatchConfigRepository repo;

    @Transactional(readOnly = true)
    public List<AdminMatchConfigItemRspDto> getAll() {
        return repo.findAll().stream()
                .sorted(Comparator.comparing(CodeMatchConfig::getConfigKey))
                .map(AdminMatchConfigItemRspDto::from)
                .toList();
    }

    @Transactional
    public List<AdminMatchConfigItemRspDto> update(AdminMatchConfigUpdateReqDto req, String adminIdentifier) {
        // 현재 row 전부 로드 — 키 존재 확인 + 가중치 합 시뮬레이션용
        Map<String, CodeMatchConfig> current = repo.findAll().stream()
                .collect(Collectors.toMap(CodeMatchConfig::getConfigKey, e -> e));

        // 키 존재 + 값 파싱 검증
        for (AdminMatchConfigUpdateReqDto.Entry entry : req.entries()) {
            CodeMatchConfig row = current.get(entry.configKey());
            if (row == null) {
                throw new BusinessException(ErrorCode.INVALID_MATCH_CONFIG_KEY,
                        "존재하지 않는 키: " + entry.configKey());
            }
            validateValue(entry.configKey(), entry.configValue(), row.getValueType());
        }

        // 가중치 합 1.0 검증 — 변경 적용한 가상 결과로 계산
        Map<String, String> simulated = current.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getConfigValue()));
        for (AdminMatchConfigUpdateReqDto.Entry entry : req.entries()) {
            simulated.put(entry.configKey(), entry.configValue());
        }
        validateWeightSum(simulated);

        // 4) 검증 통과 → JPA dirty checking 으로 update.
        for (AdminMatchConfigUpdateReqDto.Entry entry : req.entries()) {
            CodeMatchConfig row = current.get(entry.configKey());
            row.applyUpdate(entry.configValue(), adminIdentifier);
        }

        log.info("[AdminMatchConfig] 갱신 — admin={}, count={}, keys={}",
                adminIdentifier, req.entries().size(),
                req.entries().stream().map(AdminMatchConfigUpdateReqDto.Entry::configKey).toList());

        return getAll();
    }

    /* ─── 검증 helpers ─── */

    private void validateValue(String key, String value, String valueType) {
        switch (valueType) {
            case "INT" -> {
                try { Integer.parseInt(value); }
                catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.INVALID_MATCH_CONFIG_VALUE,
                            key + ": INT 파싱 실패 (" + value + ")");
                }
            }
            case "DOUBLE" -> {
                try { Double.parseDouble(value); }
                catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.INVALID_MATCH_CONFIG_VALUE,
                            key + ": DOUBLE 파싱 실패 (" + value + ")");
                }
            }
            case "JSON" -> validateJson(key, value);
            default -> throw new BusinessException(ErrorCode.INVALID_MATCH_CONFIG_VALUE,
                    key + ": 알 수 없는 valueType " + valueType);
        }
    }

    /**
     * JSON 키별 기대 schema 검증.
     *  - radius_steps_km    : int[]
     *  - region_tiers       : double[][]
     *  - category_mate_cats : List<String>
     *  그 외 JSON 키는 단순 파싱 가능 여부만.
     */
    private void validateJson(String key, String value) {
        try {
            switch (key) {
                case "radius_steps_km"    -> OM.readValue(value, int[].class);
                case "region_tiers"       -> OM.readValue(value, double[][].class);
                case "category_mate_cats" -> OM.readValue(value, new TypeReference<List<String>>() {});
                default                   -> OM.readTree(value);
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_MATCH_CONFIG_VALUE,
                    key + ": JSON 형식 오류 — " + e.getMessage());
        }
    }

    private void validateWeightSum(Map<String, String> simulated) {
        double sum = 0;
        for (String k : WEIGHT_KEYS) {
            String v = simulated.get(k);
            if (v == null) return; // 키 자체가 미시드 (방어적). 검증 skip
            try { sum += Double.parseDouble(v); }
            catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.INVALID_MATCH_CONFIG_VALUE,
                        k + ": DOUBLE 파싱 실패");
            }
        }
        if (Math.abs(sum - 1.0) > WEIGHT_SUM_TOLERANCE) {
            throw new BusinessException(ErrorCode.INVALID_MATCH_CONFIG_VALUE,
                    String.format("sortKey 가중치 합이 1.0 이어야 합니다 (현재 %.3f)", sum));
        }
    }
}
