package com.nokcha.efbe.domain.user.service;

import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.user.dto.request.UserScodeReqDto;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    @Transactional
    public void updateScode(UserScodeReqDto reqDto) {
        if (!reqDto.getScode().equals(reqDto.getScodeConfirm())) {
            throw new BusinessException(ErrorCode.SCODE_CONFIRM_MISMATCH);
        }

        User user = userRepository.findById(securityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));

        user.updateScode(reqDto.getScode());
    }
}
