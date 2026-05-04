package com.nokcha.efbe.domain.user.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_activity_status")
public class UserActivityStatus extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId;

    @Column
    private Long balgameVotedCount;     // 벨게 투표 수

    @Column
    private Long balgameCommentCount;   // 벨게 댓글 작성 수

    @Column
    private Long postitWrittenCount;    // 포스트잇 작성 수

    @Column
    private Long postitReplySentCount;  // 포스트잇 답장 보낸 수

    @Column
    private Long postitReplyReceivedCount;  // 포스트잇 답장 받은 수

    @Column
    private Long matchLikeReceivedCount;    // 매칭 받은 좋아요 수

    @Column
    private Long matchSuccessCount;     // 매칭 성사 수

    @Builder
    public UserActivityStatus(Long userId, Long balgameVotedCount, Long balgameCommentCount, Long postitWrittenCount, Long postitReplySentCount, Long postitReplyReceivedCount, Long matchLikeReceivedCount, Long matchSuccessCount) {
        this.userId = userId;
        this.balgameVotedCount = balgameVotedCount;
        this.balgameCommentCount = balgameCommentCount;
        this.postitWrittenCount = postitWrittenCount;
        this.postitReplySentCount = postitReplySentCount;
        this.postitReplyReceivedCount = postitReplyReceivedCount;
        this.matchLikeReceivedCount = matchLikeReceivedCount;
        this.matchSuccessCount = matchSuccessCount;
    }
}
