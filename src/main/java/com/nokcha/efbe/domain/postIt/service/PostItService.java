package com.nokcha.efbe.domain.postIt.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.util.CursorCodec;
import com.nokcha.efbe.domain.payment.entity.CodeItem;
import com.nokcha.efbe.domain.payment.repository.CodeItemRepository;
import com.nokcha.efbe.domain.payment.service.DailyUsageService;
import com.nokcha.efbe.domain.postIt.dto.request.PostCreateReqDto;
import com.nokcha.efbe.domain.postIt.dto.response.PostItRspDto;
import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostChatRoom;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.repository.PostChatRoomRepository;
import com.nokcha.efbe.domain.postIt.repository.PostItRepository;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItCursor;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItRow;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 포스트잇 게시글 서비스 (CRUD + 상단 고정 + 신고 숨김)
@Service
@RequiredArgsConstructor
public class PostItService {

    private static final int FREE_EXPIRE_HOURS = 24;
    private static final int PREMIUM_EXPIRE_HOURS = 72;

    // 일일 한도 (BASIC 플랜 기준 — 프리미엄은 별도 처리 지점, 현재는 일괄 적용)
    private static final String ACTION_POST_WRITE = "POST_WRITE";
    private static final String ACTION_POST_LIGHTNING = "POST_LIGHTNING";
    private static final int FREE_POST_WRITE_LIMIT = 2;
    private static final int FREE_POST_LIGHTNING_LIMIT = 1;
    private static final int DEFAULT_FEED_SIZE = 20;
    private static final int MAX_FEED_SIZE = 50;

    private final PostItRepository postItRepository;
    private final PostChatRoomRepository postChatRoomRepository;
    private final UserRepository userRepository;
    private final DailyUsageService dailyUsageService;
    private final CodeItemRepository itemCatalogRepository;
    private final CursorCodec cursorCodec;

    // 포스트잇 작성 - 카테고리 코드가 LIGHTN 이면 익명 강제 불가, 일일 한도는 별도 카운터
    // 무료 한도: POST_WRITE 2회/일, POST_LIGHTNING 은 별도 카운터로 1회/일
    @Transactional
    public PostItRspDto createPostIt(Long userId, PostCreateReqDto req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        PostCategory categoryCode = req.getCategoryCode();
        boolean lightning = categoryCode == PostCategory.LIGHTN;
        boolean anonymous = Boolean.TRUE.equals(req.getIsAnonymous());
        if (lightning && anonymous) {
            throw new BusinessException(ErrorCode.POST_LIGHTNING_ANONYMOUS);
        }

        // 일일 한도 체크 (번개는 별도 카운터, 일반글은 공통 카운터)
        if (lightning) {
            dailyUsageService.consume(userId, ACTION_POST_LIGHTNING, FREE_POST_LIGHTNING_LIMIT);
        } else {
            dailyUsageService.consume(userId, ACTION_POST_WRITE, FREE_POST_WRITE_LIMIT);
        }

        int hours = Boolean.TRUE.equals(req.getPremiumDuration()) ? PREMIUM_EXPIRE_HOURS : FREE_EXPIRE_HOURS;
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(hours);

        PostIt post = PostIt.builder()
                .uuid(java.util.UUID.randomUUID().toString())
                .user(user)
                .categoryCode(categoryCode)
                .content(req.getContent())
                .isAnonymous(anonymous && !lightning)
                .expiresAt(expiresAt)
                .build();
        // 작성 직후는 owner view — 익명이어도 userId 노출 (본인 확인용)
        return PostItRspDto.fromOwnerView(postItRepository.save(post));
    }

    // 활성 피드 조회 (커서 기반, createTime DESC + id DESC, 만료/숨김/삭제 제외, 카테고리 옵션)
    // 메인 피드는 본인이어도 익명 글은 userId 마스킹 — frontend 에서 "from 익명" 표시.
    @Transactional(readOnly = true)
    public CursorPageResponse<PostItRspDto> getPostIts(PostCategory categoryCode, String cursor, Integer size) {
        int pageSize = clampSize(size);
        PostItCursor decoded = cursorCodec.decode(cursor, PostItCursor.class);
        LocalDateTime now = LocalDateTime.now();

        List<PostItRow> rows = postItRepository.findActiveFeed(categoryCode, now, decoded, pageSize + 1);
        boolean hasMore = rows.size() > pageSize;
        List<PostItRow> page = hasMore ? rows.subList(0, pageSize) : rows;

        List<PostItRspDto> items = page.stream().map(PostItRspDto::from).toList();
        if (!hasMore) return CursorPageResponse.last(items);

        PostItRow tail = page.get(page.size() - 1);
        String nextCursor = cursorCodec.encode(new PostItCursor(tail.createTime(), tail.id()));
        return CursorPageResponse.of(items, nextCursor);
    }

    private int clampSize(Integer size) {
        if (size == null || size <= 0) return DEFAULT_FEED_SIZE;
        if (size > MAX_FEED_SIZE) throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        return size;
    }

    // 내가 쓴 글 목록 — 쿼리에서 user_id 필터링되므로 모든 행이 본인 글.
    // 응답 표기는 익명 정책 따라 마스킹 ("from 익명" 표시). 본인 식별은 목록 자체로 충분.
    @Transactional(readOnly = true)
    public Page<PostItRspDto> getMyPosts(Long userId, int page, int size) {
        return postItRepository.findByUserIdOrderByCreateTimeDesc(userId, PageRequest.of(page, size))
                .map(PostItRspDto::from);
    }

    // 단건 상세 조회 — 본인이어도 익명 글은 userId 마스킹 (피드 정책과 일관)
    @Transactional(readOnly = true)
    public PostItRspDto getOnePostIt(Long postId) {
        PostIt post = postItRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));
        return PostItRspDto.from(post);
    }

    // Soft delete - 연결된 채팅방도 is_active=FALSE 로 전환
    @Transactional
    public void deletePostIt(Long postId, Long userId) {
        PostIt post = postItRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));
        if (post.getUser() == null || !post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.POST_NOT_OWNER);
        }
        post.softDelete();

        List<PostChatRoom> rooms = postChatRoomRepository.findByPostId(postId);
        rooms.forEach(PostChatRoom::deactivate);
    }

    // 상단 고정 활성화 - POST_PIN 아이템 1회 소비 선행, 지속 시간은 마스터의 effect_duration_min
    @Transactional
    public PostItRspDto activatePin(Long postId, Long userId) {
        PostIt post = postItRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));
        if (post.getUser() == null || !post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.POST_NOT_OWNER);
        }
        // TODO(v1.2 별 차감): SUPER_LIKE / PRE_MESSAGE / PROFILE_BOOST / UNDO 등을 user_star_balance 에서 직접 차감하는 로직 추가 예정
        // (이전 InventoryService.consumeItemByCode(POST_PIN) 호출 자리 — 상단 고정 활성화 로직은 그대로 유지)
        int minutes = itemCatalogRepository.findByItemCode(CodeItem.CODE_POST_PIN)
                .map(item -> item.getEffectDurationMin() == null ? 0 : item.getEffectDurationMin())
                .orElse(0);
        post.activatePin(LocalDateTime.now().plusMinutes(minutes));
        // owner 액션 응답 — userId 노출
        return PostItRspDto.fromOwnerView(post);
    }
}
