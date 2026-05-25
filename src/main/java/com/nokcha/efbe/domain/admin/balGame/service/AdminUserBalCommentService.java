package com.nokcha.efbe.domain.admin.balGame.service;

import com.nokcha.efbe.domain.admin.balGame.dto.response.AdminUserBalCommentRspDto;
import com.nokcha.efbe.domain.balGame.repository.BalGameCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserBalCommentService {

    private final BalGameCommentRepository balGameCommentRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserBalCommentRspDto> getUserComments(Long userId, Pageable pageable) {
        return balGameCommentRepository.findByUser_IdOrderByCreateTimeDesc(userId, pageable)
                .map(AdminUserBalCommentRspDto::from);
    }
}
