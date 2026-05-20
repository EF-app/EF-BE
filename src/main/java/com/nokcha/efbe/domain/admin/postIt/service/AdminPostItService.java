package com.nokcha.efbe.domain.admin.postIt.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.postIt.dto.request.AdminPostItHideReqDto;
import com.nokcha.efbe.domain.admin.postIt.dto.response.AdminPostItRspDto;
import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.repository.PostItRepository;
import com.nokcha.efbe.domain.postIt.repository.PostLikeRepository;
import com.nokcha.efbe.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPostItService {

    private final PostItRepository postItRepository;
    private final PostLikeRepository postLikeRepository;
    private final AreaRepository areaRepository;

    @Transactional(readOnly = true)
    public Page<AdminPostItRspDto> getPostIts(String keyword,
                                              PostCategory categoryCode,
                                              Boolean isHidden,
                                              Boolean isDeleted,
                                              Long userId,
                                              Pageable pageable) {
        return postItRepository
                .findAdminPostIts(keyword, categoryCode, isHidden, isDeleted, userId, pageable)
                .map(AdminPostItRspDto::from);
    }

    // 상세 — id 단건. 모든 상태 노출, 본문 치환 없이 원본.
    @Transactional(readOnly = true)
    public AdminPostItRspDto getPostIt(Long id) {
        PostIt post = postItRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));
        return buildEntityDto(post);
    }

    // 숨김 처리 — 이미 삭제된 글은 거부, 이미 숨김 상태면 멱등(현재 값 그대로 반환).
    @Transactional
    public AdminPostItRspDto hide(Long id, AdminPostItHideReqDto req) {
        PostIt post = postItRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));
        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (!Boolean.TRUE.equals(post.getIsHidden())) {
            post.hideByAdmin();
        }
        String reason = req == null ? null : req.getReason();
        log.info("[AdminPostIt] hide post id={} reason={}", id, reason);
        // TODO(audit-log): audit_log 테이블 신설 시 reason 영구 기록
        return buildEntityDto(post);
    }

    // 숨김 해제 — isHidden=true 인 글만. 성공 시 is_hidden=false + report_count=0 리셋.
    @Transactional
    public AdminPostItRspDto restore(Long id) {
        PostIt post = postItRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));
        if (!Boolean.TRUE.equals(post.getIsHidden())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        post.restoreByAdmin();
        log.info("[AdminPostIt] restore post id={}", id);
        return buildEntityDto(post);
    }

    // PostIt → DTO 변환 (likeCount + area 조회 포함).
    private AdminPostItRspDto buildEntityDto(PostIt post) {
        long likeCount = postLikeRepository.countByPostId(post.getId());
        CodeArea area = null;
        User user = post.getUser();
        if (user != null && user.getAreaId() != null) {
            area = areaRepository.findById(user.getAreaId()).orElse(null);
        }
        return AdminPostItRspDto.from(post, likeCount, area);
    }
}
