package com.nokcha.efbe.domain.balGame.repository;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.entity.QBalGame;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameCursor;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameSummaryRow;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

// 밸런스 게임 피드 Querydsl 구현체
// 정렬: createTime DESC, id DESC (안정 정렬)
// 필터: status=PUBLISHED, 카테고리(선택)
@Repository
@RequiredArgsConstructor
public class BalGameQueryRepositoryImpl implements BalGameQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public List<BalGameSummaryRow> findPublicFeed(BalCategoryCode categoryCode, BalGameCursor cursor, int size) {
        QBalGame g = QBalGame.balGame;

        return query
                .select(Projections.constructor(BalGameSummaryRow.class,
                        g.id,
                        g.optionA,
                        g.optionB,
                        g.optionAEmoji,
                        g.optionBEmoji,
                        g.categoryCode,
                        g.status,
                        g.totalCount,
                        g.aCount,
                        g.bCount,
                        g.commentCount,
                        g.scheduledAt,
                        g.createTime))
                .from(g)
                .where(
                        g.status.eq(BalGameStatus.PUBLISHED),
                        categoryEq(categoryCode),
                        cursorAfter(cursor)
                )
                .orderBy(g.createTime.desc(), g.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression categoryEq(BalCategoryCode categoryCode) {
        if (categoryCode == null) return null;
        return QBalGame.balGame.categoryCode.eq(categoryCode);
    }

    // 커서 이후 페이지: (createTime, id) DESC 정렬의 lexicographic next
    // (createTime < c.createTime) OR (createTime == c.createTime AND id < c.id)
    private BooleanExpression cursorAfter(BalGameCursor c) {
        if (c == null || c.createTime() == null || c.id() == null) return null;
        QBalGame g = QBalGame.balGame;
        return g.createTime.lt(c.createTime())
                .or(g.createTime.eq(c.createTime()).and(g.id.lt(c.id())));
    }
}
