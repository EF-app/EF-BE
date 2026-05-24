package com.nokcha.efbe.domain.postIt.repository;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.repository.projection.AdminPostItRow;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItCursor;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItRow;
import com.nokcha.efbe.domain.postIt.repository.projection.UserActivityPostItRow;
import com.nokcha.efbe.domain.postIt.repository.projection.UserActivityReactedPostItCursor;
import com.nokcha.efbe.domain.postIt.repository.projection.UserActivityReactedPostItRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

// 포스트잇 Querydsl 인터페이스 (커서 페이지네이션 + 카테고리 동적 조건 + 사용자 활동)
public interface PostItQueryRepository {

    // 활성 피드 — isHidden=false, isDeleted=false, expiresAt>now, 카테고리 코드(선택), 차단한 작성자 제외
    List<PostItRow> findActiveFeed(PostCategory categoryCode, LocalDateTime now, PostItCursor cursor, int size, Long viewerId, List<Long> blockedUserIds);

    // "내가 붙인" — userId 본인 작성 글 + likeCount + chatCount(post_chat_room 전체)
    List<UserActivityPostItRow> findMyPostsWithCounts(Long userId, PostItCursor cursor, int size);

    // "내가 반응한" — 내가 좋아요(post_like) 했거나 partner_id 로 참여한 채팅방(post_chat_room) 의 상대 글
    List<UserActivityReactedPostItRow> findMyReactedPosts(Long userId, UserActivityReactedPostItCursor cursor, int size);

    // 어드민 목록 — keyword(nickname OR content LIKE) / categoryCode / isHidden / isDeleted / userId 동적 필터.
    Page<AdminPostItRow> findAdminPostIts(String keyword,
                                          PostCategory categoryCode,
                                          Boolean isHidden,
                                          Boolean isDeleted,
                                          Long userId,
                                          Pageable pageable);
}
