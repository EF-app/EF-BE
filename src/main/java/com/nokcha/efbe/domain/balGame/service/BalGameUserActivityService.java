package com.nokcha.efbe.domain.balGame.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.util.CursorCodec;
import com.nokcha.efbe.domain.balGame.dto.response.BalGameUserActivityEntryRspDto;
import com.nokcha.efbe.domain.balGame.repository.BalVoteRepository;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameUserActivityEntryCursor;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameUserActivityEntryRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BalGameUserActivityService {

    private static final int DEFAULT_FEED_SIZE = 20;
    private static final int MAX_FEED_SIZE = 50;

    private final BalVoteRepository balVoteRepository;
    private final CursorCodec cursorCodec;

    // 내가 투표한 게임 목록 (커서 기반, bal_vote.create_time DESC + bal_vote.id DESC)
    // 게임 상태: PUBLISHED + ARCHIVED 만 노출 (DRAFT/HIDDEN 제외)
    @Transactional(readOnly = true)
    public CursorPageResponse<BalGameUserActivityEntryRspDto> getMyVotedGames(Long userId, String cursor, Integer size) {
        int pageSize = clampSize(size);
        BalGameUserActivityEntryCursor decoded = cursorCodec.decode(cursor, BalGameUserActivityEntryCursor.class);

        List<BalGameUserActivityEntryRow> rows = balVoteRepository.findMyVotedGames(userId, decoded, pageSize + 1);
        boolean hasMore = rows.size() > pageSize;
        List<BalGameUserActivityEntryRow> page = hasMore ? rows.subList(0, pageSize) : rows;

        List<BalGameUserActivityEntryRspDto> items = page.stream().map(BalGameUserActivityEntryRspDto::from).toList();
        if (!hasMore) return CursorPageResponse.last(items);

        BalGameUserActivityEntryRow tail = page.get(page.size() - 1);
        String nextCursor = cursorCodec.encode(new BalGameUserActivityEntryCursor(tail.myVotedAt(), tail.voteId()));
        return CursorPageResponse.of(items, nextCursor);
    }

    private int clampSize(Integer size) {
        if (size == null || size <= 0) return DEFAULT_FEED_SIZE;
        if (size > MAX_FEED_SIZE) throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        return size;
    }
}
