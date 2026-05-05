package com.nokcha.efbe.domain.postIt.repository;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItCursor;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItRow;

import java.time.LocalDateTime;
import java.util.List;

// 포스트잇 피드 Querydsl 인터페이스 (커서 페이지네이션 + 카테고리 동적 조건)
public interface PostItQueryRepository {

    // 활성 피드 — isHidden=false, isDeleted=false, expiresAt>now, 카테고리 코드(선택)
    // createTime DESC, id DESC
    // size+1 fetch 로 hasMore 판정 가능
    // viewerId == null 이면 likedByMe 는 모두 false
    List<PostItRow> findActiveFeed(PostCategory categoryCode, LocalDateTime now, PostItCursor cursor, int size, Long viewerId);
}
