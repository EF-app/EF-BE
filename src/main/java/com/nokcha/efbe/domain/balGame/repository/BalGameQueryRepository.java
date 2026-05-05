package com.nokcha.efbe.domain.balGame.repository;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameCursor;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameSummaryRow;

import java.util.List;

// 밸런스 게임 피드 Querydsl 인터페이스 (커서 페이지네이션 + 카테고리/상태 동적 조건)
public interface BalGameQueryRepository {

    // 공개 피드 — status=PUBLISHED, 카테고리 선택, createTime DESC + id DESC
    // size+1 fetch 로 hasMore 판정 가능
    List<BalGameSummaryRow> findPublicFeed(BalCategoryCode categoryCode, BalGameCursor cursor, int size);
}
