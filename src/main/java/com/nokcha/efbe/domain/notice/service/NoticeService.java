package com.nokcha.efbe.domain.notice.service;

import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import com.nokcha.efbe.domain.notice.dto.response.NoticeDetailRspDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticePageRspDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticeSummaryRspDto;
import com.nokcha.efbe.domain.notice.entity.NoticeCategory;
import com.nokcha.efbe.domain.notice.entity.Notice;
import com.nokcha.efbe.domain.notice.entity.NoticeStatus;
import com.nokcha.efbe.domain.notice.repository.NoticeRepository;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private static final int NOTICE_PAGE_SIZE = 10;

    private final NoticeRepository noticeRepository;
    private final AdminAccountRepository adminAccountRepository;

    // 공지사항 목록 조회
    @Transactional(readOnly = true)
    public NoticePageRspDto getNotices(int page, NoticeCategory category) {
        Pageable pageable = PageRequest.of(page, NOTICE_PAGE_SIZE);
        Page<Notice> noticePage = category == null
                ? noticeRepository.findPublicNoticesByStatus(NoticeStatus.PUBLISHED, pageable)
                : noticeRepository.findPublicNoticesByStatusAndCategory(NoticeStatus.PUBLISHED, category, pageable);

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

    // 공지사항 작성자 닉네임 조회
    private String getAuthorNickname(Notice notice) {
        if (notice.getCreateUser() == null) return "알 수 없음";

        return adminAccountRepository.findById(notice.getCreateUser())
                .map(AdminAccount::getName)
                .orElse("알 수 없음");
    }
}
