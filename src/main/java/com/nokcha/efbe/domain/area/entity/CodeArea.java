package com.nokcha.efbe.domain.area.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "code_area",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_code_area_country_city", columnNames = {"country", "city"})
        })
public class CodeArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String country;

    @Column(nullable = false, length = 30)
    private String city;
}