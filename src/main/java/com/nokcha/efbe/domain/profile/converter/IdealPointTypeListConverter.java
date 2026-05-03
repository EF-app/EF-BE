package com.nokcha.efbe.domain.profile.converter;

import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class IdealPointTypeListConverter implements AttributeConverter<List<IdealPointType>, String> {

    // 이상형 포인트 목록 문자열 변환
    @Override
    public String convertToDatabaseColumn(List<IdealPointType> attribute) {
        if (attribute == null || attribute.isEmpty()) return "";

        return attribute.stream()
                .map(IdealPointType::name)
                .collect(Collectors.joining(","));
    }

    // 문자열을 이상형 포인트 목록으로 변환
    @Override
    public List<IdealPointType> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(IdealPointType::valueOf)
                .toList();
    }
}
