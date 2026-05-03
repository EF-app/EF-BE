package com.nokcha.efbe.domain.balGame.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.util.CursorCodec;
import com.nokcha.efbe.domain.balGame.dto.response.BalGameDetailRspDto;
import com.nokcha.efbe.domain.balGame.dto.response.BalGameSummaryRspDto;
import com.nokcha.efbe.domain.balGame.dto.response.CommentRspDto;
import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.entity.BalVote;
import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import com.nokcha.efbe.domain.balGame.repository.BalGameCommentRepository;
import com.nokcha.efbe.domain.balGame.repository.BalGameRepository;
import com.nokcha.efbe.domain.balGame.repository.BalVoteRepository;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameCursor;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameSummaryRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 밸런스 게임 본문 서비스 (사용자 측 조회 전용)
@Service
@RequiredArgsConstructor
public class BalGameService {

    private static final int HOME_RECENT_COMMENT_LIMIT = 3;
    private static final int DEFAULT_FEED_SIZE = 20;
    private static final int MAX_FEED_SIZE = 50;
    private static final int DEFAULT_HOME_SIZE = 5;
    private static final int MAX_HOME_SIZE = 20;

    private final BalGameRepository balGameRepository;
    private final BalGameCommentRepository balGameCommentRepository;
    private final BalVoteRepository balVoteRepository;
    private final CursorCodec cursorCodec;

    // 공개된 게임 목록 조회 (커서 기반, createTime DESC + id DESC)
    @Transactional(readOnly = true)
    public CursorPageResponse<BalGameSummaryRspDto> getBalanceGames(BalCategoryCode categoryCode, String cursor, Integer size) {
        int pageSize = clampSize(size);
        BalGameCursor decoded = cursorCodec.decode(cursor, BalGameCursor.class);

        List<BalGameSummaryRow> rows = balGameRepository.findPublicFeed(categoryCode, decoded, pageSize + 1);
        boolean hasMore = rows.size() > pageSize;
        List<BalGameSummaryRow> page = hasMore ? rows.subList(0, pageSize) : rows;

        List<BalGameSummaryRspDto> items = page.stream().map(BalGameSummaryRspDto::from).toList();
        if (!hasMore) return CursorPageResponse.last(items);

        BalGameSummaryRow tail = page.get(page.size() - 1);
        String nextCursor = cursorCodec.encode(new BalGameCursor(tail.createTime(), tail.id()));
        return CursorPageResponse.of(items, nextCursor);
    }

    private int clampSize(Integer size) {
        if (size == null || size <= 0) return DEFAULT_FEED_SIZE;
        if (size > MAX_FEED_SIZE) throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        return size;
    }

    // 홈 배치 조회 — PUBLISHED, update_time DESC, size 개. 각 게임마다 최신 댓글 3개 + 내 투표 포함.
    // N+1 회피: 댓글/내 투표를 게임 ID 일괄 IN 쿼리 한 번씩으로 fetch 후 in-memory 매핑.
    @Transactional(readOnly = true)
    public List<BalGameDetailRspDto> getHomeFeed(Integer size, Long viewerId) {
        int pageSize = clampHomeSize(size);
        List<BalGame> games = balGameRepository.findByStatusOrderByUpdateTimeDescIdDesc(
                BalGameStatus.PUBLISHED, PageRequest.of(0, pageSize));
        if (games.isEmpty()) return Collections.emptyList();

        List<Long> gameIds = games.stream().map(BalGame::getId).toList();

        // 1) 댓글 일괄 fetch — game_id 별 최신순으로 이미 정렬됨 (쿼리에서 game_id, createTime desc).
        //    in-memory 에서 game_id 로 grouping + 게임당 limit 3 적용.
        Map<Long, List<BalGameComment>> commentsByGame = new HashMap<>();
        for (BalGameComment c : balGameCommentRepository.findRecentTopCommentsByGameIds(gameIds)) {
            Long gid = c.getGame().getId();
            List<BalGameComment> list = commentsByGame.computeIfAbsent(gid, k -> new ArrayList<>());
            if (list.size() < HOME_RECENT_COMMENT_LIMIT) list.add(c);
        }

        // 2) 내 투표 일괄 fetch
        Map<Long, BalVoteChoice> myVoteByGame = new HashMap<>();
        if (viewerId != null) {
            for (BalVote v : balVoteRepository.findByGameIdInAndUserId(gameIds, viewerId)) {
                myVoteByGame.put(v.getGame().getId(), v.getChoice());
            }
        }

        // 3) 매핑
        List<BalGameDetailRspDto> result = new ArrayList<>(games.size());
        for (BalGame game : games) {
            BalVoteChoice myChoice = myVoteByGame.get(game.getId());
            List<CommentRspDto> recentDtos = commentsByGame.getOrDefault(game.getId(), Collections.emptyList())
                    .stream()
                    .map(c -> CommentRspDto.from(c, viewerId, false))
                    .toList();
            result.add(BalGameDetailRspDto.of(game, myChoice, recentDtos));
        }
        return result;
    }

    private int clampHomeSize(Integer size) {
        if (size == null || size <= 0) return DEFAULT_HOME_SIZE;
        if (size > MAX_HOME_SIZE) throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        return size;
    }

    // 단건 상세 조회 (홈 진입 - 최신 댓글 3개 + 내 투표 정보 포함)
    @Transactional(readOnly = true)
    public BalGameDetailRspDto getOneBalanceGame(Long gameId, Long viewerId) {
        BalGame game = balGameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_GAME));
        if (game.getStatus() != BalGameStatus.PUBLISHED && game.getStatus() != BalGameStatus.ARCHIVED) {
            throw new BusinessException(ErrorCode.GAME_NOT_PUBLISHED);
        }

        BalVoteChoice myChoice = (viewerId == null) ? null
                : balVoteRepository.findByGameIdAndUserId(gameId, viewerId).map(BalVote::getChoice).orElse(null);

        List<BalGameComment> recent = balGameCommentRepository.findRecentTopComments(
                gameId, PageRequest.of(0, HOME_RECENT_COMMENT_LIMIT));
        List<CommentRspDto> recentDtos = recent.stream()
                .map(c -> CommentRspDto.from(c, viewerId, false))
                .toList();

        return BalGameDetailRspDto.of(game, myChoice, recentDtos == null ? Collections.emptyList() : recentDtos);
    }
}
