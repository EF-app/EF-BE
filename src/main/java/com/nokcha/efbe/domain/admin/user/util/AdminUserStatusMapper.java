package com.nokcha.efbe.domain.admin.user.util;

import com.nokcha.efbe.domain.user.entity.BanStatus;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class AdminUserStatusMapper {

    public static String toUserStatus(BanStatus banStatus) {
        if (banStatus == null) {
            return "ACTIVE";
        }
        return switch (banStatus) {
            case NONE -> "ACTIVE";
            case SEVEN_DAYS, THIRTY_DAYS -> "TEMP_SUSPENDED";
            case FOREVER -> "PERMANENTLY_SUSPENDED";
        };
    }
}
