package com.nokcha.efbe.domain.postIt.repository;

import com.nokcha.efbe.domain.area.entity.QCodeArea;
import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.QPostChatRoom;
import com.nokcha.efbe.domain.postIt.entity.QPostIt;
import com.nokcha.efbe.domain.postIt.entity.QPostLike;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItCursor;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItRow;
import com.nokcha.efbe.domain.postIt.repository.projection.UserActivityPostItRow;
import com.nokcha.efbe.domain.postIt.repository.projection.UserActivityReactedPostItCursor;
import com.nokcha.efbe.domain.postIt.repository.projection.UserActivityReactedPostItRow;
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
    public List<PostItRow> findActiveFeed(PostCategory categoryCode, LocalDateTime now, PostItCursor cursor, int size, Long viewerId, List<Long> blockedUserIds) {
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
                        p.color,
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
                        cursorAfter(cursor),
                        blockedNotIn(blockedUserIds)
                )
                .orderBy(p.createTime.desc(), p.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression categoryEq(PostCategory categoryCode) {
        if (categoryCode == null) return null;
        return QPostIt.postIt.categoryCode.eq(categoryCode);
    }

    // 차단한 작성자 글 제외. 목록이 비면 null 반환 → Querydsl where 무시.
    private BooleanExpression blockedNotIn(List<Long> blockedUserIds) {
        if (blockedUserIds == null || blockedUserIds.isEmpty()) return null;
        return QPostIt.postIt.user.id.notIn(blockedUserIds);
    }

    // 커서 이후 페이지: (createTime, id) DESC 정렬의 lexicographic next
    private BooleanExpression cursorAfter(PostItCursor c) {
        if (c == null || c.createTime() == null || c.id() == null) return null;
        QPostIt p = QPostIt.postIt;
        return p.createTime.lt(c.createTime())
                .or(p.createTime.eq(c.createTime()).and(p.id.lt(c.id())));
    }

    @Override
    public List<UserActivityPostItRow> findMyPostsWithCounts(Long userId, PostItCursor cursor, int size) {
        QPostIt p = QPostIt.postIt;
        QPostLike pl = QPostLike.postLike;
        QPostChatRoom r = QPostChatRoom.postChatRoom;

        Expression<Long> likeCountSub = JPAExpressions
                .select(pl.count())
                .from(pl)
                .where(pl.post.id.eq(p.id));

        Expression<Long> chatCountSub = JPAExpressions
                .select(r.count())
                .from(r)
                .where(r.post.id.eq(p.id));

        // 본인이 자기 글에 좋아요 누른 적 있는지 — exists 서브쿼리
        QPostLike pl2 = new QPostLike("plMine");
        Expression<Boolean> likedByMeExpr = JPAExpressions
                .selectOne()
                .from(pl2)
                .where(pl2.post.id.eq(p.id), pl2.user.id.eq(ConstantImpl.create(userId)))
                .exists();

        return query
                .select(Projections.constructor(UserActivityPostItRow.class,
                        p.id,
                        p.user.id,
                        p.categoryCode,
                        p.content,
                        p.color,
                        p.isAnonymous,
                        p.expiresAt,
                        p.pinnedUntil,
                        p.replyCount,
                        likeCountSub,
                        chatCountSub,
                        likedByMeExpr,
                        p.isHidden,
                        p.isDeleted,
                        p.createTime))
                .from(p)
                .where(
                        p.user.id.eq(userId),
                        cursorAfter(cursor)
                )
                .orderBy(p.createTime.desc(), p.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<UserActivityReactedPostItRow> findMyReactedPosts(Long userId, UserActivityReactedPostItCursor cursor, int size) {
        QPostIt p = QPostIt.postIt;
        QUser u = QUser.user;
        QCodeArea a = QCodeArea.codeArea;
        QPostLike pl = QPostLike.postLike;
        QPostChatRoom r = QPostChatRoom.postChatRoom;

        // 좋아요/채팅 카운트는 게시글 단위 전체 집계 (서브쿼리)
        Expression<Long> likeCountSub = JPAExpressions
                .select(pl.count())
                .from(pl)
                .where(pl.post.id.eq(p.id));

        Expression<Long> chatCountSub = JPAExpressions
                .select(r.count())
                .from(r)
                .where(r.post.id.eq(p.id));

        // viewer 본인이 좋아요/채팅(partner) 한 적 있는지 — exists 서브쿼리
        QPostLike pl2 = new QPostLike("plMine");
        Expression<Boolean> likedByMeExpr = JPAExpressions
                .selectOne()
                .from(pl2)
                .where(pl2.post.id.eq(p.id), pl2.user.id.eq(ConstantImpl.create(userId)))
                .exists();

        QPostChatRoom r2 = new QPostChatRoom("rMine");
        Expression<Boolean> chattedByMeExpr = JPAExpressions
                .selectOne()
                .from(r2)
                .where(r2.post.id.eq(p.id), r2.partner.id.eq(ConstantImpl.create(userId)))
                .exists();

        // "내가 반응한" 조건: 좋아요 또는 채팅(partner) 중 하나 이상 — 본인 작성 글 제외
        BooleanExpression reactedByMe = JPAExpressions
                .selectOne()
                .from(pl2)
                .where(pl2.post.id.eq(p.id), pl2.user.id.eq(ConstantImpl.create(userId)))
                .exists()
                .or(JPAExpressions
                        .selectOne()
                        .from(r2)
                        .where(r2.post.id.eq(p.id), r2.partner.id.eq(ConstantImpl.create(userId)))
                        .exists());

        return query
                .select(Projections.constructor(UserActivityReactedPostItRow.class,
                        p.id,
                        p.user.id,
                        u.nickname,
                        u.age,
                        a.country,
                        a.city,
                        p.categoryCode,
                        p.content,
                        p.color,
                        p.isAnonymous,
                        p.expiresAt,
                        p.pinnedUntil,
                        p.replyCount,
                        likeCountSub,
                        chatCountSub,
                        likedByMeExpr,
                        chattedByMeExpr,
                        p.isHidden,
                        p.isDeleted,
                        p.createTime))
                .from(p)
                .leftJoin(u).on(u.id.eq(p.user.id))
                .leftJoin(a).on(a.id.eq(u.areaId))
                .where(
                        p.user.id.ne(userId),  // 본인 작성 글 제외
                        reactedByMe,
                        reactedCursorAfter(cursor)
                )
                .orderBy(p.createTime.desc(), p.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression reactedCursorAfter(UserActivityReactedPostItCursor c) {
        if (c == null || c.createTime() == null || c.id() == null) return null;
        QPostIt p = QPostIt.postIt;
        return p.createTime.lt(c.createTime())
                .or(p.createTime.eq(c.createTime()).and(p.id.lt(c.id())));
    }
}
