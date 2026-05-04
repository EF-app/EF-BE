package com.nokcha.efbe.domain.payment.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

// user_monthly_usage 복합 PK
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMonthlyUsageId implements Serializable {
    private Long userId;
    private LocalDate periodStart;
    private String featureCode;
}
