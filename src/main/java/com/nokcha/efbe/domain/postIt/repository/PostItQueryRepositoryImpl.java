package com.nokcha.efbe.domain.postIt.repository;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.QPostIt;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItCursor;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItRow;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// 포스트잇 피드 Querydsl 구현체
// 정렬: createTime DESC, id DESC (안정 정렬)
// 필터: isHidden=false, isDeleted=false, expiresAt>now, 카테고리 코드(선택)
@Repository
@RequiredArgsConstructor
public class PostItQueryRepositoryImpl implements PostItQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public List<PostItRow> findActiveFeed(PostCategory categoryCode, LocalDateTime now, PostItCursor cursor, int size) {
        QPostIt p = QPostIt.postIt;

        return query
                .select(Projections.constructor(PostItRow.class,
                        p.id,
                        p.user.id,
                        p.categoryCode,
                        p.content,
                        p.isAnonymous,
                        p.expiresAt,
                        p.pinnedUntil,
                        p.replyCount,
                        p.isHidden,
                        p.isDeleted,
                        p.createTime))
                .from(p)
                .where(
                        p.isHidden.isFalse(),
                        p.isDeleted.isFalse(),
                        p.expiresAt.gt(now),
                        categoryEq(categoryCode),
                        cursorAfter(cursor)
                )
                .orderBy(p.createTime.desc(), p.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression categoryEq(PostCategory categoryCode) {
        if (categoryCode == null) return null;
        return QPostIt.postIt.categoryCode.eq(categoryCode);
    }

    // 커서 이후 페이지: (createTime, id) DESC 정렬의 lexicographic next
    private BooleanExpression cursorAfter(PostItCursor c) {
        if (c == null || c.createTime() == null || c.id() == null) return null;
        QPostIt p = QPostIt.postIt;
        return p.createTime.lt(c.createTime())
                .or(p.createTime.eq(c.createTime()).and(p.id.lt(c.id())));
    }
}
