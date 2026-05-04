package com.nokcha.efbe.domain.postIt.repository;

import com.nokcha.efbe.domain.area.entity.QCodeArea;
import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.QPostIt;
import com.nokcha.efbe.domain.postIt.entity.QPostLike;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItCursor;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItRow;
import com.nokcha.efbe.domain.user.entity.QUser;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// 포스트잇 피드 Querydsl 구현체
// 정렬: createTime DESC, id DESC (안정 정렬)
// 필터: isHidden=false, isDeleted=false, expiresAt>now, 카테고리 코드(선택)
// 조인:
//   - User left join (nickname, birth)
//   - CodeArea left join via user.areaId (country, city)
// 좋아요 수/likedByMe 는 서브쿼리.
@Repository
@RequiredArgsConstructor
public class PostItQueryRepositoryImpl implements PostItQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public List<PostItRow> findActiveFeed(PostCategory categoryCode, LocalDateTime now, PostItCursor cursor, int size, Long viewerId) {
        QPostIt p = QPostIt.postIt;
        QUser u = QUser.user;
        QCodeArea a = QCodeArea.codeArea;
        QPostLike pl = QPostLike.postLike;

        Expression<Long> likeCountSub = JPAExpressions
                .select(pl.count())
                .from(pl)
                .where(pl.post.id.eq(p.id));

        Expression<Boolean> likedByMeExpr;
        if (viewerId == null) {
            likedByMeExpr = Expressions.constant(Boolean.FALSE);
        } else {
            QPostLike pl2 = new QPostLike("pl2");
            likedByMeExpr = JPAExpressions
                    .selectOne()
                    .from(pl2)
                    .where(pl2.post.id.eq(p.id), pl2.user.id.eq(ConstantImpl.create(viewerId)))
                    .exists();
        }

        return query
                .select(Projections.constructor(PostItRow.class,
                        p.id,
                        p.user.id,
                        u.nickname,
                        u.age,
                        a.country,
                        a.city,
                        p.categoryCode,
                        p.content,
                        p.isAnonymous,
                        p.expiresAt,
                        p.pinnedUntil,
                        p.replyCount,
                        likeCountSub,
                        likedByMeExpr,
                        p.isHidden,
                        p.isDeleted,
                        p.createTime))
                .from(p)
                .leftJoin(u).on(u.id.eq(p.user.id))
                .leftJoin(a).on(a.id.eq(u.areaId))
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
