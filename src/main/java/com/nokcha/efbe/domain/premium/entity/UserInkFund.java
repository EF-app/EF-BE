package com.nokcha.efbe.domain.premium.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInkFund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId;

    @Column
    private Integer fund;   // 현재 잔액

    @Column
    private Integer totalCharged;   // 누적 충전

    @Column
    private Integer totalUsed;  // 누적 사용

    @Builder
    public UserInkFund(Long userId, Integer fund, Integer totalCharged, Integer totalUsed) {
        this.userId = userId;
        this.fund = fund;
        this.totalCharged = totalCharged;
        this.totalUsed = totalUsed;
    }
}
