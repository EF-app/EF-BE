package com.efbe.domain.profile.entity;

import com.efbe.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "user_personal",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_personal_user_id_personal_id_type",
                        columnNames = {"user_id", "personal_id", "type"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPersonal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long personalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserPersonalType type;

    @Builder
    public UserPersonal(Long userId, Long personalId, UserPersonalType type) {
        this.userId = userId;
        this.personalId = personalId;
        this.type = type;
    }
}