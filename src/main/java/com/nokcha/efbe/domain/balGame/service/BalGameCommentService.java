package com.nokcha.efbe.domain.balGame.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.balGame.dto.request.CommentCreateReqDto;
import com.nokcha.efbe.domain.balGame.dto.response.CommentRspDto;
import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.repository.BalCommentLikeRepository;
import com.nokcha.efbe.domain.balGame.repository.BalGameCommentRepository;
import com.nokcha.efbe.domain.balGame.repository.BalGameRepository;
import com.nokcha.efbe.domain.balGame.repository.BalVoteRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 밸런스 게임 댓글 서비스 (계층 구조 + 삭제/숨김 표시 정책)
@Service
@RequiredArgsConstructor
public class BalGameCommentService {

    private static final int HOME_RECENT_DEFAULT_SIZE = 3;
    private static final int HOME_RECENT_MAX_SIZE = 10;

    private final BalGameCommentRepository balGameCommentRepository;
    private final BalGameRepository balGameRepository;
    private final BalVoteRepository balVoteRepository;
    private final BalCommentLikeRepository balCommentLikeRepository;
    private final UserRepository userRepository;
    private final NicknameService nicknameService;

    // 댓글/대댓글 작성 (투표 완료자만 가능, 익명 닉네임 자동 부여)
    @Transactional
    public CommentRspDto createComment(Long gameId, Long userId, CommentCreateReqDto req) {
        BalGame game = balGameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_GAME));
        if (game.getStatus() != BalGameStatus.PUBLISHED && game.getStatus() != BalGameStatus.ARCHIVED) {
            throw new BusinessException(ErrorCode.GAME_NOT_PUBLISHED);
        }
        ensureVoter(gameId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        BalGameComment parent = null;
        if (req.getParentId() != null) {
            parent = balGameCommentRepository.findById(req.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_COMMENT));
            // 대댓글의 대댓글 금지 (1단계 깊이)
            if (parent.getParent() != null) {
                throw new BusinessException(ErrorCode.COMMENT_REPLY_DEPTH_EXCEEDED);
            }
        }

        String nickname = nicknameService.resolveOrCreate(game, user);
        BalGameComment comment = BalGameComment.builder()
                .uuid(java.util.UUID.randomUUID().toString())
                .game(game).user(user).parent(parent)
                .nickname(nickname).content(req.getContent())
                .build();
        BalGameComment saved = balGameCommentRepository.save(comment);
        // 댓글 카운트는 JPQL bulk UPDATE 로 갱신 — entity setter 경로를 피해 update_time 비갱신 정책 유지
        balGameRepository.updateCommentCount(gameId, 1);

        return CommentRspDto.from(saved, userId, false);
    }

    // 본인 댓글 삭제 (소프트 삭제 - 본문은 표시 정책으로 치환)
    @Transactional
    public void deleteComment(Long gameId, Long commentId, Long userId) {
        BalGameComment comment = balGameCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_COMMENT));
        if (!comment.getGame().getId().equals(gameId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_COMMENT);
        }
        if (comment.getUser() == null || !comment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_OWNER);
        }
        if (Boolean.TRUE.equals(comment.getIsDeleted())) return;
        comment.softDelete();

        // 댓글 카운트는 JPQL bulk UPDATE 로 갱신 (update_time 비갱신 정책 유지).
        // comment.getGame() 은 LAZY 프록시이며 .getId() 는 추가 SELECT 없이 식별자만 꺼냄.
        Long commentGameId = comment.getGame() != null ? comment.getGame().getId() : null;
        if (commentGameId != null) {
            balGameRepository.updateCommentCount(commentGameId, -1);
        }
    }

    // 게임 댓글 트리 조회 (오래된 순 - 맨 아래가 최신, 신고/숨김 필터 적용)
    @Transactional(readOnly = true)
    public List<CommentRspDto> getComments(Long gameId, Long viewerId) {
        if (!balGameRepository.existsById(gameId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_GAME);
        }
        ensureVoter(gameId, viewerId);

        List<BalGameComment> visible = balGameCommentRepository.findVisibleCommentsAsc(gameId, viewerId);
        return assembleHierarchy(visible, viewerId);
    }

    // 부모-자식 트리로 재구성. 부모가 삭제되어도 자식이 있으면 본문만 치환되어 유지됨
    // 좋아요 여부는 배치 1쿼리로 조회하여 N+1 제거
    private List<CommentRspDto> assembleHierarchy(List<BalGameComment> flat, Long viewerId) {
        Set<Long> likedIds = (viewerId == null || flat.isEmpty())
                ? Collections.emptySet()
                : balCommentLikeRepository.findLikedCommentIds(
                        flat.stream().map(BalGameComment::getId).toList(), viewerId);

        Map<Long, CommentRspDto> dtoById = new HashMap<>();
        List<CommentRspDto> roots = new ArrayList<>();
        for (BalGameComment c : flat) {
            CommentRspDto dto = CommentRspDto.from(c, viewerId, likedIds.contains(c.getId()));
            dtoById.put(c.getId(), dto);
        }
        for (BalGameComment c : flat) {
            CommentRspDto dto = dtoById.get(c.getId());
            Long parentId = c.getParent() == null ? null : c.getParent().getId();
            if (parentId != null && dtoById.containsKey(parentId)) {
                dtoById.get(parentId).getChildren().add(dto);
            } else {
                roots.add(dto);
            }
        }
        return roots;
    }

    // 메인홈용 — 특정 게임의 최신 top-level 댓글 N개 (기본 3, 최대 10).
    // 투표 여부와 무관하게 메인홈 카드에서 미리보기로 노출되므로 ensureVoter 호출하지 않음.
    @Transactional(readOnly = true)
    public List<CommentRspDto> getRecentComments(Long gameId, Long viewerId, Integer size) {
        if (!balGameRepository.existsById(gameId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_GAME);
        }
        int limit = (size == null || size <= 0) ? HOME_RECENT_DEFAULT_SIZE
                : Math.min(size, HOME_RECENT_MAX_SIZE);
        List<BalGameComment> recent = balGameCommentRepository.findRecentTopComments(
                gameId, PageRequest.of(0, limit));

        Set<Long> likedIds = (viewerId == null || recent.isEmpty())
                ? Collections.emptySet()
                : balCommentLikeRepository.findLikedCommentIds(
                        recent.stream().map(BalGameComment::getId).toList(), viewerId);

        return recent.stream()
                .map(c -> CommentRspDto.from(c, viewerId, likedIds.contains(c.getId())))
                .toList();
    }

    // 투표 완료자만 댓글창 접근 가능
    private void ensureVoter(Long gameId, Long userId) {
        if (userId == null || !balVoteRepository.existsByGameIdAndUserId(gameId, userId)) {
            throw new BusinessException(ErrorCode.NOT_VOTED_FOR_COMMENT);
        }
    }
}
