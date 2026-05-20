package com.nokcha.efbe.domain.postIt.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.postIt.dto.response.PostLikeRspDto;
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
// 추가/취소 후 갱신된 카운트·viewer 상태를 반환해 FE 가 ±1 계산으로 NaN 나는 일을 막는다.
@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostItRepository postItRepository;
    private final UserRepository userRepository;

    // 좋아요 추가 (중복 시 예외) — id 기반
    @Transactional
    public PostLikeRspDto createLike(Long postId, Long userId) {
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
        return PostLikeRspDto.builder()
                .postId(postId)
                .likeCount(postLikeRepository.countByPostId(postId))
                .likedByMe(true)
                .build();
    }

    // 좋아요 취소 — id 기반
    @Transactional
    public PostLikeRspDto deleteLike(Long postId, Long userId) {
        PostLike like = postLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LIKE));
        postLikeRepository.delete(like);
        return PostLikeRspDto.builder()
                .postId(postId)
                .likeCount(postLikeRepository.countByPostId(postId))
                .likedByMe(false)
                .build();
    }
}
