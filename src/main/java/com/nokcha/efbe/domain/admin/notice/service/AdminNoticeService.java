package com.nokcha.efbe.domain.admin.notice.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import com.nokcha.efbe.domain.admin.notice.dto.request.NoticeReqDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticeDetailRspDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticePageRspDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticeSummaryRspDto;
import com.nokcha.efbe.domain.notice.entity.Notice;
import com.nokcha.efbe.domain.notice.entity.NoticeCategory;
import com.nokcha.efbe.domain.notice.entity.NoticeStatus;
import com.nokcha.efbe.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private static final int NOTICE_PAGE_SIZE = 10;

    private final NoticeRepository noticeRepository;
    private final AdminAccountRepository adminAccountRepository;

    // 공지사항 작성
    @Transactional
    public NoticeDetailRspDto createNotice(NoticeReqDto reqDto) {
        NoticeCategory category = resolveCategory(reqDto);
        validateOriginalNotice(reqDto, category);
        Integer sortOrder = normalizeSortOrder(reqDto.getSortOrder());

        // 고정 항목인 경우 기존 고정 항목 +1
        if (sortOrder != null) noticeRepository.incrementSortOrdersFrom(sortOrder);

        Notice notice = noticeRepository.save(Notice.builder()
                .title(reqDto.getTitle())
                .content(reqDto.getContent())
                .category(category)
                .viewCount(0L)
                .status(resolveStatus(reqDto))
                .scheduledAt(resolveScheduledAt(reqDto))
                .publishedAt(resolvePublishedAt(reqDto))
                .originalNoticeId(resolveOriginalNoticeId(reqDto, category))
                .sortOrder(sortOrder)
                .build());

        return NoticeDetailRspDto.from(notice, getAuthorNickname(notice));
    }

    // 공지사항 수정
    @Transactional
    public NoticeDetailRspDto updateNotice(Long noticeId, NoticeReqDto reqDto) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTICE));

        if (resolveCategory(reqDto) == NoticeCategory.AMEND && notice.getCategory() != NoticeCategory.AMEND) {
            throw new BusinessException(ErrorCode.INVALID_AMEND_NOTICE_UPDATE);
        }

        NoticeStatus status = resolveStatus(reqDto);
        LocalDateTime scheduledAt = resolveScheduledAt(reqDto);
        validateScheduledAt(status, scheduledAt);
        Integer newSortOrder = normalizeSortOrderForUpdate(notice, reqDto.getSortOrder());
        adjustSortOrder(notice, newSortOrder);

        notice.update(reqDto.getTitle(), reqDto.getContent(), resolveCategory(reqDto), status, scheduledAt, newSortOrder);
        return NoticeDetailRspDto.from(notice, getAuthorNickname(notice));
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTICE));

        noticeRepository.delete(notice);
    }

    // 관리자 공지사항 목록 조회 - status와 무관하게 전체 조회
    @Transactional(readOnly = true)
    public NoticePageRspDto getNotices(int page) {
        Pageable pageable = PageRequest.of(page, NOTICE_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Notice> noticePage = noticeRepository.findAll(pageable);

        return NoticePageRspDto.builder()
                .notices(noticePage.getContent().stream()
                        .map(notice -> NoticeSummaryRspDto.from(notice, getAuthorNickname(notice)))
                        .toList())
                .page(noticePage.getNumber())
                .size(noticePage.getSize())
                .totalPages(noticePage.getTotalPages())
                .totalElements(noticePage.getTotalElements())
                .last(noticePage.isLast())
                .build();
    }

    // 관리자 공지사항 상세 조회 - status와 무관하게 단건 조회
    @Transactional(readOnly = true)
    public NoticeDetailRspDto getOneNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTICE));

        return NoticeDetailRspDto.from(notice, getAuthorNickname(notice));
    }

    // 예약 발행
    @Transactional
    public void publishDueScheduledNotices() {
        LocalDateTime now = LocalDateTime.now();
        List<Notice> dueNotices = noticeRepository.findAllByStatusAndScheduledAtLessThanEqual(NoticeStatus.SCHEDULED, now);
        if (dueNotices.isEmpty()) return;

        dueNotices.forEach(notice -> notice.publish(now));
    }

    // 공지사항 작성자 닉네임 조회
    private String getAuthorNickname(Notice notice) {
        if (notice.getCreateUser() == null) return "알 수 없음";

        return adminAccountRepository.findById(notice.getCreateUser())
                .map(AdminAccount::getName)
                .orElse("알 수 없음");
    }

    private NoticeStatus resolveStatus(NoticeReqDto reqDto) {
        return reqDto.getStatus() == null ? NoticeStatus.PUBLISHED : reqDto.getStatus();
    }

    private NoticeCategory resolveCategory(NoticeReqDto reqDto) {
        return reqDto.getCategory() == null ? NoticeCategory.NOTICE : reqDto.getCategory();
    }

    private Long resolveOriginalNoticeId(NoticeReqDto reqDto, NoticeCategory category) {
        return category == NoticeCategory.AMEND ? reqDto.getOriginalNoticeId() : null;
    }

    private LocalDateTime resolveScheduledAt(NoticeReqDto reqDto) {
        validateScheduledAt(resolveStatus(reqDto), reqDto.getScheduledAt());
        return resolveStatus(reqDto) == NoticeStatus.SCHEDULED ? reqDto.getScheduledAt() : null;
    }

    private LocalDateTime resolvePublishedAt(NoticeReqDto reqDto) {
        return resolveStatus(reqDto) == NoticeStatus.PUBLISHED ? LocalDateTime.now() : null;
    }

    private Integer normalizeSortOrder(Integer requestedSortOrder) {
        if (requestedSortOrder == null) return null;

        int maxSortOrder = noticeRepository.findMaxSortOrder();
        if (requestedSortOrder <= 1) return 1;
        if (requestedSortOrder > maxSortOrder + 1) return maxSortOrder + 1;

        return requestedSortOrder;
    }

    private Integer normalizeSortOrderForUpdate(Notice notice, Integer requestedSortOrder) {
        if (requestedSortOrder == null) return null;

        int maxSortOrder = noticeRepository.findMaxSortOrder();
        if (notice.getSortOrder() != null) maxSortOrder--;

        if (requestedSortOrder <= 1) return 1;
        if (requestedSortOrder > maxSortOrder + 1) return maxSortOrder + 1;

        return requestedSortOrder;
    }

    private void adjustSortOrder(Notice notice, Integer newSortOrder) {
        Integer currentSortOrder = notice.getSortOrder();

        if (Objects.equals(currentSortOrder, newSortOrder)) return;

        if (currentSortOrder == null) {
            if (newSortOrder != null) noticeRepository.incrementSortOrdersFrom(newSortOrder);
            return;
        }

        if (newSortOrder == null) {
            noticeRepository.decrementSortOrdersAfter(currentSortOrder);
            return;
        }

        if (newSortOrder < currentSortOrder) {
            noticeRepository.incrementSortOrdersBetween(newSortOrder, currentSortOrder);
            return;
        }

        noticeRepository.decrementSortOrdersBetween(currentSortOrder, newSortOrder);
    }

    private void validateScheduledAt(NoticeStatus status, LocalDateTime scheduledAt) {
        if (status == NoticeStatus.SCHEDULED) {
            if (scheduledAt == null || !scheduledAt.isAfter(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.INVALID_SCHEDULED_AT);
            }
            // 10분 단위
            if (scheduledAt.getMinute() % 10 != 0 || scheduledAt.getSecond() != 0 || scheduledAt.getNano() != 0) {
                throw new BusinessException(ErrorCode.INVALID_SCHEDULED_AT);
            }
            return;
        }

        if (scheduledAt != null) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULED_AT);
        }
    }

    private void validateOriginalNotice(NoticeReqDto reqDto, NoticeCategory category) {
        if (category == NoticeCategory.AMEND) {
            if (reqDto.getOriginalNoticeId() == null) {
                throw new BusinessException(ErrorCode.ORIGINAL_NOTICE_REQUIRED);
            }
            noticeRepository.findById(reqDto.getOriginalNoticeId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTICE));
            return;
        }

        if (reqDto.getOriginalNoticeId() != null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}
