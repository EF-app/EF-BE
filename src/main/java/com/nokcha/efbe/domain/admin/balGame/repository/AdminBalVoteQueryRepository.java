package com.nokcha.efbe.domain.admin.balGame.repository;

import com.nokcha.efbe.domain.admin.balGame.repository.projection.AdminBalVoteBucketRow;
import com.nokcha.efbe.domain.admin.balGame.repository.projection.AdminBalVoteRow;
import com.nokcha.efbe.domain.area.entity.QCodeArea;
import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import com.nokcha.efbe.domain.balGame.entity.QBalVote;
import com.nokcha.efbe.domain.user.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

// 어드민 측 BalVote
@Repository
@RequiredArgsConstructor
public class AdminBalVoteQueryRepository {

    private static final String AGE_LABEL_20_24 = "20~24";
    private static final String AGE_LABEL_25_29 = "25~29";
    private static final String AGE_LABEL_30_34 = "30~34";
    private static final String AGE_LABEL_35_39 = "35~39";
    private static final String AGE_LABEL_40_44 = "40~44";
    private static final String AGE_LABEL_45_49 = "45~49";
    private static final String AGE_LABEL_50_PLUS = "50대 이상";
    private static final String AGE_LABEL_UNSET = "미설정";
    private static final String AREA_LABEL_UNSET = "미설정";

    private final JPAQueryFactory query;

    // 개별 투표자 목록
    public Page<AdminBalVoteRow> findAdminVotes(Long gameId, BalVoteChoice choice, Pageable pageable) {
        QBalVote v = QBalVote.balVote;
        QUser u = QUser.user;
        QCodeArea a = QCodeArea.codeArea;

        BooleanExpression where = v.game.id.eq(gameId);
        if (choice != null) where = where.and(v.choice.eq(choice));

        List<AdminBalVoteRow> content = query
                .select(Projections.constructor(AdminBalVoteRow.class,
                        v.id,
                        v.user.id,
                        u.uuid,
                        u.nickname,
                        u.age,
                        a.country,
                        a.city,
                        v.choice,
                        v.createTime,
                        v.updateTime))
                .from(v)
                .leftJoin(u).on(u.id.eq(v.user.id))
                .leftJoin(a).on(a.id.eq(u.areaId))
                .where(where)
                .orderBy(v.createTime.desc(), v.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query.select(v.count()).from(v).where(where);
        Long total = countQuery.fetchOne();
        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    // 연령대 분포 — CASE WHEN 으로 라벨링 후 GROUP BY label, choice.
    public List<AdminBalVoteBucketRow> aggregateVotesByAge(Long gameId) {
        QBalVote v = QBalVote.balVote;
        QUser u = QUser.user;

        StringExpression ageLabel = new CaseBuilder()
                .when(u.age.between(20, 24)).then(AGE_LABEL_20_24)
                .when(u.age.between(25, 29)).then(AGE_LABEL_25_29)
                .when(u.age.between(30, 34)).then(AGE_LABEL_30_34)
                .when(u.age.between(35, 39)).then(AGE_LABEL_35_39)
                .when(u.age.between(40, 44)).then(AGE_LABEL_40_44)
                .when(u.age.between(45, 49)).then(AGE_LABEL_45_49)
                .when(u.age.goe(50)).then(AGE_LABEL_50_PLUS)
                .otherwise(AGE_LABEL_UNSET);

        return query
                .select(Projections.constructor(AdminBalVoteBucketRow.class,
                        ageLabel,
                        v.choice,
                        v.count()))
                .from(v)
                .leftJoin(u).on(u.id.eq(v.user.id))
                .where(v.game.id.eq(gameId))
                .groupBy(ageLabel, v.choice)
                .fetch();
    }

    // 지역 분포 (country 1단계) — GROUP BY country, choice. country null 은 "미설정".
    public List<AdminBalVoteBucketRow> aggregateVotesByArea(Long gameId) {
        QBalVote v = QBalVote.balVote;
        QUser u = QUser.user;
        QCodeArea a = QCodeArea.codeArea;

        StringExpression areaLabel = new CaseBuilder()
                .when(a.country.isNull()).then(AREA_LABEL_UNSET)
                .otherwise(a.country);

        return query
                .select(Projections.constructor(AdminBalVoteBucketRow.class,
                        areaLabel,
                        v.choice,
                        v.count()))
                .from(v)
                .leftJoin(u).on(u.id.eq(v.user.id))
                .leftJoin(a).on(a.id.eq(u.areaId))
                .where(v.game.id.eq(gameId))
                .groupBy(areaLabel, v.choice)
                .fetch();
    }
}
