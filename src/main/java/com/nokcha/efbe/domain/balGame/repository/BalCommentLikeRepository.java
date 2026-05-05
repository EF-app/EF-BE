package com.nokcha.efbe.domain.balGame.repository;

import com.nokcha.efbe.domain.balGame.entity.BalCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

// 댓글 좋아요 레포지토리
public interface BalCommentLikeRepository extends JpaRepository<BalCommentLike, Long> {

    // 댓글 + 유저 단건 조회 (중복/취소 판정)
    Optional<BalCommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

    // 좋아요 존재 여부 (단건용)
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    // 유저가 좋아요한 댓글 ID 집합 (댓글 트리 DTO 변환 시 N+1 제거용 배치 조회)
    @Query("select l.comment.id from BalCommentLike l " +
            "where l.comment.id in :commentIds and l.user.id = :userId")
    Set<Long> findLikedCommentIds(@Param("commentIds") Collection<Long> commentIds,
                                  @Param("userId") Long userId);
}
