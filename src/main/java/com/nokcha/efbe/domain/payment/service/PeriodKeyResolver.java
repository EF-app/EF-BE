package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.domain.payment.model.ItemResetPeriod;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

/**
 * 주기 경계가 바뀌면 키가 바뀌어 새 카운터 행이 생김 → 리셋 배치 불필요.
 */
@Component
public class PeriodKeyResolver {

    /** 리셋 없는 아이템(CAPABILITY/PARAM/NONE)의 고정 버킷 키. */
    public static final String NONE_KEY = "NONE";

    public String resolve(ItemResetPeriod period, LocalDate date) {
        return switch (period) {
            case DAILY -> date.toString();                                  // yyyy-MM-dd
            case WEEKLY -> String.format("%d-W%02d",
                    date.get(WeekFields.ISO.weekBasedYear()),
                    date.get(WeekFields.ISO.weekOfWeekBasedYear()));        // yyyy-Www
            case MONTHLY -> String.format("%d-%02d", date.getYear(), date.getMonthValue()); // yyyy-MM
            case NONE -> NONE_KEY;
        };
    }
}
