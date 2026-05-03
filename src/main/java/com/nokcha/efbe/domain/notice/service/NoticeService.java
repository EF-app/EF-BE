package com.nokcha.efbe.domain.notice.service;

import com.nokcha.efbe.domain.admin.entity.Admin;
import com.nokcha.efbe.domain.notice.dto.request.NoticeReqDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticeDetailRspDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticePageRspDto;
import com.nokcha.efbe.domain.notice.dto.response.NoticeSummaryRspDto;
import com.nokcha.efbe.domain.notice.entity.Notice;
import com.nokcha.efbe.domain.notice.repository.NoticeRepository;
import com.nokcha.efbe.domain.admin.repository.AdminRepository;
import com.nokcha.efbe.common.auth.service.AuthUserService;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private static final int NOTICE_PAGE_SIZE = 10;

    private final NoticeRepository noticeRepository;
    private final AuthUserService authUserService;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    // 공지사항 작성
    @Transactional
    public NoticeDetailRspDto createNotice(String loginId, NoticeReqDto reqDto) {
        authUserService.validateAdmin(loginId);

        Notice notice = noticeRepository.save(Notice.builder()
                .title(reqDto.getTitle())
                .content(reqDto.getContent())
                .viewCount(0L)
                .build());

        return NoticeDetailRspDto.from(notice, getAuthorNickname(notice));
    }

    // 공지사항 수정
    @Transactional
    public NoticeDetailRspDto updateNotice(String loginId, Long noticeId, NoticeReqDto reqDto) {
        authUserService.validateAdmin(loginId);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTICE));

        notice.update(reqDto.getTitle(), reqDto.getContent());
        return NoticeDetailRspDto.from(notice, getAuthorNickname(notice));
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(String loginId, Long noticeId) {
        authUserService.validateAdmin(loginId);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTICE));

        noticeRepository.delete(notice);
    }

    // 공지사항 목록 조회
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

    // 공지사항 상세 조회
    @Transactional
    public NoticeDetailRspDto getOneNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTICE));

        notice.increaseViewCount();
        return NoticeDetailRspDto.from(notice, getAuthorNickname(notice));
    }

    // 공지사항 작성자 닉네임 조회
    private String getAuthorNickname(Notice notice) {
        if (notice.getCreateUser() == null) return "알 수 없음";

        return adminRepository.findById(notice.getCreateUser())
                .map(Admin::getNickname)
                .orElse("알 수 없음");
    }
}
