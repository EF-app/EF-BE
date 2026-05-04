package com.nokcha.efbe.domain.postIt.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.entity.PostLike;
import com.nokcha.efbe.domain.postIt.repository.PostItRepository;
import com.nokcha.efbe.domain.postIt.repository.PostLikeRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 포스트잇 좋아요 서비스 (토글)
@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostItRepository postItRepository;
    private final UserRepository userRepository;

    // 좋아요 추가 (중복 시 예외)
    @Transactional
    public void createLike(Long postId, Long userId) {
        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LIKE);
        }
        PostIt post = postItRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        try {
            postLikeRepository.save(PostLike.builder().post(post).user(user).build());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_LIKE, e);
        }
    }

    // 좋아요 취소
    @Transactional
    public void deleteLike(Long postId, Long userId) {
        PostLike like = postLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LIKE));
        postLikeRepository.delete(like);
    }
}
