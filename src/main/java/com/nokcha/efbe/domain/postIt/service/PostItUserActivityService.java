package com.nokcha.efbe.domain.postIt.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.util.CursorCodec;
import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.postIt.dto.response.UserActivityPostItRspDto;
import com.nokcha.efbe.domain.postIt.dto.response.UserActivityReactedPostItRspDto;
import com.nokcha.efbe.domain.postIt.repository.PostItRepository;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItCursor;
import com.nokcha.efbe.domain.postIt.repository.projection.UserActivityPostItRow;
import com.nokcha.efbe.domain.postIt.repository.projection.UserActivityReactedPostItCursor;
import com.nokcha.efbe.domain.postIt.repository.projection.UserActivityReactedPostItRow;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostItUserActivityService {

    private static final int DEFAULT_FEED_SIZE = 20;
    private static final int MAX_FEED_SIZE = 50;

    private final PostItRepository postItRepository;
    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final CursorCodec cursorCodec;

    // "내가 붙인" — 본인 작성 글 + likeCount + chatCount
    // 본인 area 는 1회 lookup 후 모든 row 에 재사용 (viewer == owner 라 동일 area)
    @Transactional(readOnly = true)
    public CursorPageResponse<UserActivityPostItRspDto> getMyPosts(Long userId, String cursor, Integer size) {
        int pageSize = clampSize(size);
        PostItCursor decoded = cursorCodec.decode(cursor, PostItCursor.class);

        OwnerSnapshot owner = resolveOwnerSnapshot(userId);

        List<UserActivityPostItRow> rows = postItRepository.findMyPostsWithCounts(userId, decoded, pageSize + 1);
        boolean hasMore = rows.size() > pageSize;
        List<UserActivityPostItRow> page = hasMore ? rows.subList(0, pageSize) : rows;

        List<UserActivityPostItRspDto> items = page.stream()
                .map(row -> UserActivityPostItRspDto.from(row, owner.nickname, owner.age, owner.country, owner.city))
                .toList();
        if (!hasMore) return CursorPageResponse.last(items);

        UserActivityPostItRow tail = page.get(page.size() - 1);
        String nextCursor = cursorCodec.encode(new PostItCursor(tail.createTime(), tail.id()));
        return CursorPageResponse.of(items, nextCursor);
    }

    // "내가 반응한" — 좋아요/채팅(partner)으로 반응한 상대 글 (본인 글 제외)
    // 정렬: post_it.create_time DESC, post_it.id DESC
    @Transactional(readOnly = true)
    public CursorPageResponse<UserActivityReactedPostItRspDto> getMyReactions(Long userId, String cursor, Integer size) {
        int pageSize = clampSize(size);
        UserActivityReactedPostItCursor decoded = cursorCodec.decode(cursor, UserActivityReactedPostItCursor.class);

        List<UserActivityReactedPostItRow> rows = postItRepository.findMyReactedPosts(userId, decoded, pageSize + 1);
        boolean hasMore = rows.size() > pageSize;
        List<UserActivityReactedPostItRow> page = hasMore ? rows.subList(0, pageSize) : rows;

        List<UserActivityReactedPostItRspDto> items = page.stream()
                .map(UserActivityReactedPostItRspDto::from)
                .toList();
        if (!hasMore) return CursorPageResponse.last(items);

        UserActivityReactedPostItRow tail = page.get(page.size() - 1);
        String nextCursor = cursorCodec.encode(new UserActivityReactedPostItCursor(tail.createTime(), tail.id()));
        return CursorPageResponse.of(items, nextCursor);
    }

    private int clampSize(Integer size) {
        if (size == null || size <= 0) return DEFAULT_FEED_SIZE;
        if (size > MAX_FEED_SIZE) throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        return size;
    }

    // 본인 nickname/age/country/city 1회 캐시 — "내가 붙인" 카드 표시 마스킹 정책에 사용
    private OwnerSnapshot resolveOwnerSnapshot(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        Long areaId = user.getAreaId();
        if (areaId == null) {
            return new OwnerSnapshot(user.getNickname(), user.getAge(), null, null);
        }
        CodeArea area = areaRepository.findById(areaId).orElse(null);
        if (area == null) {
            return new OwnerSnapshot(user.getNickname(), user.getAge(), null, null);
        }
        return new OwnerSnapshot(user.getNickname(), user.getAge(), area.getCountry(), area.getCity());
    }

    private record OwnerSnapshot(String nickname, Integer age, String country, String city) {}
}
