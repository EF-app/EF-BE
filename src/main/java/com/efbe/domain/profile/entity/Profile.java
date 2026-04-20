package com.efbe.domain.profile.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "profile")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Mbti mbti;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Purpose purpose;

    @Column(length = 300)
    private String message;

    @Builder
    public Profile(Long userId, Mbti mbti, Purpose purpose, String message) {
        this.userId = userId;
        this.mbti = mbti;
        this.purpose = purpose;
        this.message = message;
    }

    public void update(Mbti mbti, Purpose purpose, String message) {
        this.mbti = mbti;
        this.purpose = purpose;
        this.message = message;
    }
}