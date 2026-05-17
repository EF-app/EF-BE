package com.nokcha.efbe.domain.balGame.repository;

import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.entity.QBalGame;
import com.nokcha.efbe.domain.balGame.entity.QBalVote;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameUserActivityEntryCursor;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameUserActivityEntryRow;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

// 밸런스 게임 사용자 활동 Querydsl 구현체
// 정렬: bal_vote.create_time DESC, bal_vote.id DESC (안정 정렬)
// 필터: 본인 투표(user_id) + 게임 상태 IN (PUBLISHED, ARCHIVED)
// 인덱스: idx_balvote_user (user_id, create_time DESC) 활용
@Repository
@RequiredArgsConstructor
public class BalVoteQueryRepositoryImpl implements BalVoteQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public List<BalGameUserActivityEntryRow> findMyVotedGames(Long userId, BalGameUserActivityEntryCursor cursor, int size) {
        QBalVote v = QBalVote.balVote;
        QBalGame g = QBalGame.balGame;

        return query
                .select(Projections.constructor(BalGameUserActivityEntryRow.class,
                        g.id,
                        g.optionA,
                        g.optionB,
                        g.optionAEmoji,
                        g.optionBEmoji,
                        g.categoryCode,
                        g.status,
                        g.aCount,
                        g.bCount,
                        g.commentCount,
                        v.choice,
                        v.createTime,
                        v.id,
                        g.createTime))
                .from(v)
                .join(v.game, g)
                .where(
                        v.user.id.eq(userId),
                        g.status.in(BalGameStatus.PUBLISHED, BalGameStatus.ARCHIVED),
                        cursorAfter(cursor)
                )
                .orderBy(v.createTime.desc(), v.id.desc())
                .limit(size)
                .fetch();
    }

    // 커서 이후 페이지: (votedAt, voteId) DESC 정렬의 lexicographic next
    private BooleanExpression cursorAfter(BalGameUserActivityEntryCursor c) {
        if (c == null || c.votedAt() == null || c.voteId() == null) return null;
        QBalVote v = QBalVote.balVote;
        return v.createTime.lt(c.votedAt())
                .or(v.createTime.eq(c.votedAt()).and(v.id.lt(c.voteId())));
    }
}
