package com.nokcha.efbe.domain.postIt.repository;

import com.nokcha.efbe.domain.area.entity.QCodeArea;
import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.chat.entity.QChatParticipant;
import com.nokcha.efbe.domain.chat.entity.QChatRoom;
import com.nokcha.efbe.domain.postIt.entity.QPostIt;
import com.nokcha.efbe.domain.postIt.entity.QPostLike;
import com.nokcha.efbe.domain.postIt.repository.projection.AdminPostItRow;
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
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
        QChatRoom r = QChatRoom.chatRoom;

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
        QChatParticipant cpMine = new QChatParticipant("cpMine");
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
        QChatRoom r = QChatRoom.chatRoom;

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

        QChatRoom r2 = new QChatRoom("rMine");
        QChatParticipant cpMine = new QChatParticipant("cpMine");
        Expression<Boolean> chattedByMeExpr = JPAExpressions
                .selectOne()
                .from(r2)
                .where(
                        r2.post.id.eq(p.id),
                        r2.pairUserAId.eq(userId).or(r2.pairUserBId.eq(userId)),
                        JPAExpressions
                                .selectOne()
                                .from(cpMine)
                                .where(
                                        cpMine.chatRoom.eq(r2),
                                        cpMine.user.id.eq(userId),
                                        cpMine.leftAt.isNull()
                                )
                                .exists()
                )
                .exists();

        // "내가 반응한" 조건: 좋아요 또는 채팅(partner) 중 하나 이상 — 본인 작성 글 제외
        QChatParticipant cpReacted = new QChatParticipant("cpReacted");
        BooleanExpression reactedByMe = JPAExpressions
                .selectOne()
                .from(pl2)
                .where(pl2.post.id.eq(p.id), pl2.user.id.eq(ConstantImpl.create(userId)))
                .exists()
                .or(JPAExpressions
                        .selectOne()
                        .from(r2)
                        .where(
                                r2.post.id.eq(p.id),
                                r2.pairUserAId.eq(userId).or(r2.pairUserBId.eq(userId)),
                                JPAExpressions
                                        .selectOne()
                                        .from(cpReacted)
                                        .where(
                                                cpReacted.chatRoom.eq(r2),
                                                cpReacted.user.id.eq(userId),
                                                cpReacted.leftAt.isNull()
                                        )
                                        .exists()
                        )
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

    // 어드민 목록 — keyword(nickname/content LIKE) + categoryCode/isHidden/isDeleted/userId 동적 필터.
    // 모든 상태 노출, 익명 마스킹 없음. likeCount 는 post_like 서브쿼리.
    @Override
    public Page<AdminPostItRow> findAdminPostIts(String keyword,
                                                 PostCategory categoryCode,
                                                 Boolean isHidden,
                                                 Boolean isDeleted,
                                                 Long userId,
                                                 Pageable pageable) {
        QPostIt p = QPostIt.postIt;
        QUser u = QUser.user;
        QCodeArea a = QCodeArea.codeArea;
        QPostLike pl = QPostLike.postLike;

        Expression<Long> likeCountSub = JPAExpressions
                .select(pl.count())
                .from(pl)
                .where(pl.post.id.eq(p.id));

        BooleanExpression[] where = new BooleanExpression[]{
                keywordLike(keyword),
                categoryCode == null ? null : p.categoryCode.eq(categoryCode),
                isHidden == null ? null : p.isHidden.eq(isHidden),
                isDeleted == null ? null : p.isDeleted.eq(isDeleted),
                userId == null ? null : p.user.id.eq(userId)
        };

        List<AdminPostItRow> content = query
                .select(Projections.constructor(AdminPostItRow.class,
                        p.id,
                        p.user.id,
                        u.uuid,
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
                        p.reportCount,
                        p.replyCount,
                        likeCountSub,
                        p.isHidden,
                        p.isDeleted,
                        p.createTime,
                        p.updateTime))
                .from(p)
                .leftJoin(u).on(u.id.eq(p.user.id))
                .leftJoin(a).on(a.id.eq(u.areaId))
                .where(where)
                .orderBy(p.createTime.desc(), p.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(p.count())
                .from(p)
                .leftJoin(u).on(u.id.eq(p.user.id))
                .where(where);

        return new PageImpl<>(content, pageable, countQuery.fetchOne() == null ? 0L : countQuery.fetchOne());
    }

    // keyword 가 비어있지 않으면 users.nickname OR post_it.content LIKE
    private BooleanExpression keywordLike(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String pattern = "%" + keyword.trim() + "%";
        return QUser.user.nickname.like(pattern)
                .or(QPostIt.postIt.content.like(pattern));
    }
}
