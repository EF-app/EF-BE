package com.nokcha.efbe.common.auth.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.user.entity.Role;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserService {

    private final UserRepository userRepository;

    // 로그인 아이디로 사용자 조회
    public User getUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));
    }

    // 관리자 권한 검증
    public void validateAdmin(User user) {
        if (user.getRole() != Role.ROLE_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ROLE);
        }
    }
}
