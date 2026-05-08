package com.nokcha.efbe.domain.notice.service;

import com.nokcha.efbe.domain.admin.entity.Admin;
import com.nokcha.efbe.domain.notice.dto.request.NoticeReqDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticeDetailRspDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticePageRspDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticeSummaryRspDto;
import com.nokcha.efbe.domain.notice.entity.NoticeCategory;
import com.nokcha.efbe.domain.notice.entity.Notice;
import com.nokcha.efbe.domain.notice.entity.NoticeStatus;
import com.nokcha.efbe.domain.notice.repository.NoticeRepository;
import com.nokcha.efbe.domain.admin.repository.AdminRepository;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private static final int NOTICE_PAGE_SIZE = 10;

    private final NoticeRepository noticeRepository;
    private final SecurityUtil securityUtil;
    private final AdminRepository adminRepository;

    // 공지사항 작성
    @Transactional
    public NoticeDetailRspDto createNotice(NoticeReqDto reqDto) {
        securityUtil.validateCurrentAdmin();

        Notice notice = noticeRepository.save(Notice.builder()
                .title(reqDto.getTitle())
                .content(reqDto.getContent())
                .category(resolveCategory(reqDto))
                .viewCount(0L)
                .status(resolveStatus(reqDto))
                .scheduledAt(resolveScheduledAt(reqDto))
                .publishedAt(resolvePublishedAt(reqDto))
                .build());

        return NoticeDetailRspDto.from(notice, getAuthorNickname(notice));
    }

    // 공지사항 수정
    @Transactional
    public NoticeDetailRspDto updateNotice(Long noticeId, NoticeReqDto reqDto) {
        securityUtil.validateCurrentAdmin();

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTICE));

        NoticeStatus status = resolveStatus(reqDto);
        LocalDateTime scheduledAt = resolveScheduledAt(reqDto);
        validateScheduledAt(status, scheduledAt);

        notice.update(reqDto.getTitle(), reqDto.getContent(), resolveCategory(reqDto), status, scheduledAt);
        return NoticeDetailRspDto.from(notice, getAuthorNickname(notice));
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(Long noticeId) {
        securityUtil.validateCurrentAdmin();

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTICE));

        noticeRepository.delete(notice);
    }

    // 공지사항 목록 조회
    @Transactional(readOnly = true)
    public NoticePageRspDto getNotices(int page, NoticeCategory category) {
        Pageable pageable = PageRequest.of(page, NOTICE_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Notice> noticePage = category == null
                ? noticeRepository.findAllByStatus(NoticeStatus.PUBLISHED, pageable)
                : noticeRepository.findAllByStatusAndCategory(NoticeStatus.PUBLISHED, category, pageable);

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

    // 공지사항 상세 조회
    @Transactional
    public NoticeDetailRspDto getOneNotice(Long noticeId) {
        Notice notice = noticeRepository.findByIdAndStatus(noticeId, NoticeStatus.PUBLISHED)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTICE));

        notice.increaseViewCount();
        return NoticeDetailRspDto.from(notice, getAuthorNickname(notice));
    }

    // 예약 발행 메소드
    @Transactional
    public void publishDueScheduledNotices() {
        LocalDateTime now = LocalDateTime.now();
        List<Notice> dueNotices = noticeRepository.findAllByStatusAndScheduledAtLessThanEqual(NoticeStatus.SCHEDULED, now);
        if (dueNotices.isEmpty()) {
            return;
        }

        dueNotices.forEach(notice -> notice.publish(now));
    }

    // 공지사항 작성자 닉네임 조회
    private String getAuthorNickname(Notice notice) {
        if (notice.getCreateUser() == null) return "알 수 없음";

        return adminRepository.findById(notice.getCreateUser())
                .map(Admin::getNickname)
                .orElse("알 수 없음");
    }

    private NoticeStatus resolveStatus(NoticeReqDto reqDto) {
        return reqDto.getStatus() == null ? NoticeStatus.PUBLISHED : reqDto.getStatus();
    }

    private NoticeCategory resolveCategory(NoticeReqDto reqDto) {
        return reqDto.getCategory() == null ? NoticeCategory.NOTICE : reqDto.getCategory();
    }

    private LocalDateTime resolveScheduledAt(NoticeReqDto reqDto) {
        validateScheduledAt(resolveStatus(reqDto), reqDto.getScheduledAt());
        return resolveStatus(reqDto) == NoticeStatus.SCHEDULED ? reqDto.getScheduledAt() : null;
    }

    private LocalDateTime resolvePublishedAt(NoticeReqDto reqDto) {
        return resolveStatus(reqDto) == NoticeStatus.PUBLISHED ? LocalDateTime.now() : null;
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
}
