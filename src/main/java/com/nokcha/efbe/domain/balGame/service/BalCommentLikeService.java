package com.nokcha.efbe.domain.balGame.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.balGame.entity.BalCommentLike;
import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import com.nokcha.efbe.domain.balGame.repository.BalCommentLikeRepository;
import com.nokcha.efbe.domain.balGame.repository.BalGameCommentRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 댓글 좋아요 서비스 (토글 - 추가/취소)
@Service
@RequiredArgsConstructor
public class BalCommentLikeService {

    private final BalCommentLikeRepository balCommentLikeRepository;
    private final BalGameCommentRepository balGameCommentRepository;
    private final UserRepository userRepository;

    // 좋아요 추가 - commentUuid 기반, 카운트 1 증가, 중복 시 예외
    @Transactional
    public void createLike(String commentUuid, Long userId) {
        BalGameComment comment = balGameCommentRepository.findByUuid(commentUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_COMMENT));
        Long commentId = comment.getId();
        if (balCommentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LIKE);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        try {
            balCommentLikeRepository.save(BalCommentLike.builder().comment(comment).user(user).build());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_LIKE, e);
        }
        comment.increaseLikes();
    }

    // 좋아요 취소 - commentUuid 기반, 카운트 1 감소
    @Transactional
    public void deleteLike(String commentUuid, Long userId) {
        BalGameComment comment = balGameCommentRepository.findByUuid(commentUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_COMMENT));
        Long commentId = comment.getId();
        BalCommentLike like = balCommentLikeRepository.findByCommentIdAndUserId(commentId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LIKE));
        balCommentLikeRepository.delete(like);
        comment.decreaseLikes();
    }
}
