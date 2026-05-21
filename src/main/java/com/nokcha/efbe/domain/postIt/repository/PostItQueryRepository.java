package com.nokcha.efbe.domain.postIt.repository;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItCursor;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItRow;
import com.nokcha.efbe.domain.postIt.repository.projection.UserActivityPostItRow;
import com.nokcha.efbe.domain.postIt.repository.projection.UserActivityReactedPostItCursor;
import com.nokcha.efbe.domain.postIt.repository.projection.UserActivityReactedPostItRow;

import java.time.LocalDateTime;
import java.util.List;

// 포스트잇 Querydsl 인터페이스 (커서 페이지네이션 + 카테고리 동적 조건 + 사용자 활동)
public interface PostItQueryRepository {

    // 활성 피드 — isHidden=false, isDeleted=false, expiresAt>now, 카테고리 코드(선택)
    // createTime DESC, id DESC
    // size+1 fetch 로 hasMore 판정 가능
    // viewerId == null 이면 likedByMe 는 모두 false
    // blockedUserIds: 조회 유저가 차단한 작성자 id 목록 — 해당 작성자 글은 피드에서 제외 (비었으면 미적용)
    List<PostItRow> findActiveFeed(PostCategory categoryCode, LocalDateTime now, PostItCursor cursor, int size, Long viewerId, List<Long> blockedUserIds);

    // "내가 붙인" — userId 본인 작성 글 + likeCount + chatCount(post_chat_room 전체)
    // 정렬: post_it.create_time DESC, post_it.id DESC (안정 정렬)
    // 숨김/삭제 글은 표시 정책으로 content 치환되어 그대로 노출 (본인 글이라 목록에서 숨기지 않음)
    List<UserActivityPostItRow> findMyPostsWithCounts(Long userId, PostItCursor cursor, int size);

    // "내가 반응한" — 내가 좋아요(post_like) 했거나 partner_id 로 참여한 채팅방(post_chat_room) 의 상대 글
    // 정렬: post_it.create_time DESC, post_it.id DESC
    // 본인이 작성한 글은 제외(작성자 본인 ≠ 반응자)
    List<UserActivityReactedPostItRow> findMyReactedPosts(Long userId, UserActivityReactedPostItCursor cursor, int size);
}
