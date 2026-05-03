package com.nokcha.efbe.domain.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "code_personal")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodePersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String bigCategory;

    @Column(nullable = false, length = 100)
    private String smallCategory;

    @Builder
    public CodePersonal(String bigCategory, String smallCategory) {
        this.bigCategory = bigCategory;
        this.smallCategory = smallCategory;
    }
}
