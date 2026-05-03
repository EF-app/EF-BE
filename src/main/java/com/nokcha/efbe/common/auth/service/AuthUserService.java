package com.nokcha.efbe.common.auth.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.entity.Admin;
import com.nokcha.efbe.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserService {

    private final AdminRepository adminRepository;

    // 로그인 아이디로 관리자 조회
    public Admin getAdmin(String loginId) {
        return adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));
    }

    // 관리자 권한 검증
    public void validateAdmin(String loginId) {
        getAdmin(loginId);
    }
}
