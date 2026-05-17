package com.nokcha.efbe.domain.admin.notice.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import com.nokcha.efbe.domain.admin.notice.dto.request.NoticeReqDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticeDetailRspDto;
import com.nokcha.efbe.domain.notice.entity.Notice;
import com.nokcha.efbe.domain.notice.entity.NoticeCategory;
import com.nokcha.efbe.domain.notice.entity.NoticeStatus;
import com.nokcha.efbe.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;
    private final AdminAccountRepository adminAccountRepository;

    // 공지사항 작성
    @Transactional
    public NoticeDetailRspDto createNotice(NoticeReqDto reqDto) {
        NoticeCategory category = resolveCategory(reqDto);
        validateOriginalNotice(reqDto, category);

        Notice notice = noticeRepository.save(Notice.builder()
                .title(reqDto.getTitle())
                .content(reqDto.getContent())
                .category(category)
                .viewCount(0L)
                .status(resolveStatus(reqDto))
                .scheduledAt(resolveScheduledAt(reqDto))
                .publishedAt(resolvePublishedAt(reqDto))
                .originalNoticeId(resolveOriginalNoticeId(reqDto, category))
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

        notice.update(reqDto.getTitle(), reqDto.getContent(), resolveCategory(reqDto), status, scheduledAt);
        return NoticeDetailRspDto.from(notice, getAuthorNickname(notice));
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTICE));

        noticeRepository.delete(notice);
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
