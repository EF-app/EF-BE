package com.nokcha.efbe.domain.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "interest")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String bigCategory;

    @Column(nullable = false, length = 100)
    private String smallCategory;

    @Column(nullable = false)
    private Integer sortOrder;

    @Builder
    public Interest(String bigCategory, String smallCategory, Integer sortOrder) {
        this.bigCategory = bigCategory;
        this.smallCategory = smallCategory;
        this.sortOrder = sortOrder;
    }
}
