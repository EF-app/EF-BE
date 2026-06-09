package com.nokcha.efbe.domain.area.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "code_area",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_code_area_country_city", columnNames = {"country", "city"})
        },
        // 좌표 필터를 위한 복합 인덱스 — findEligibleIds 가 latitude/longitude BETWEEN 범위 검색.
        indexes = {
                @Index(name = "idx_code_area_lat_lon", columnList = "latitude, longitude")
        })
public class CodeArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String country;

    @Column(nullable = false, length = 30)
    private String city;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;
}