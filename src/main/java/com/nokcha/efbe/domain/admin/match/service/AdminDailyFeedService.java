package com.nokcha.efbe.domain.admin.match.service;

import com.nokcha.efbe.domain.admin.match.dto.response.AdminDailyFeedPageRspDto;
import com.nokcha.efbe.domain.admin.match.repository.AdminDailyFeedRepository;
import com.nokcha.efbe.domain.match.entity.MatchDailyFeed.SlotType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** 관리자 match_daily_feed 조회 — viewerId range / 디버깅 / CS 응대용. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDailyFeedService {

    private final AdminDailyFeedRepository adminDailyFeedRepo;

    public AdminDailyFeedPageRspDto search(
            Long viewerIdFrom, Long viewerIdTo, Long targetId,
            LocalDate feedDate, SlotType slotType, Short matchRank,
            int page, int size
    ) {
        return adminDailyFeedRepo.search(viewerIdFrom, viewerIdTo, targetId, feedDate, slotType, matchRank, page, size);
    }
}
