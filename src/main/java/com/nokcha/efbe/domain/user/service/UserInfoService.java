package com.nokcha.efbe.domain.user.service;

import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.user.dto.request.UserScodeReqDto;
import com.nokcha.efbe.domain.user.dto.request.UserWithdrawalReqDto;
import com.nokcha.efbe.domain.user.dto.response.UserSummaryRspDto;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserWithdrawal;
import com.nokcha.efbe.domain.user.entity.WithdrawStatus;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import com.nokcha.efbe.domain.user.repository.UserWithdrawalRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final UserRepository userRepository;
    private final UserWithdrawalRepository userWithdrawalRepository;
    private final AreaRepository areaRepository;
    private final SecurityUtil securityUtil;
    private final PasswordEncoder passwordEncoder;

    // 내 정보 요약 — 닉네임 / 지역 / 나이. 글쓰기 화면 / My 탭 공용.
    @Transactional(readOnly = true)
    public UserSummaryRspDto getMySummary() {
        Long userId = securityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        CodeArea area = user.getAreaId() == null
                ? null
                : areaRepository.findById(user.getAreaId()).orElse(null);
        return UserSummaryRspDto.of(user, area);
    }

    @Transactional
    public void updateScode(UserScodeReqDto reqDto) {
        if (!reqDto.getScode().equals(reqDto.getScodeConfirm())) {
            throw new BusinessException(ErrorCode.SCODE_CONFIRM_MISMATCH);
        }

        User user = userRepository.findById(securityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));

        user.updateScode(passwordEncoder.encode(reqDto.getScode()));
    }

    @Transactional
    public void withdraw(UserWithdrawalReqDto reqDto, HttpServletRequest request) {
        User user = userRepository.findById(securityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));

        UserWithdrawal withdrawal = userWithdrawalRepository.findByUserId(user.getId())
                .orElse(null);

        if (withdrawal != null
                && (withdrawal.getStatus() == WithdrawStatus.REQUESTED
                || withdrawal.getStatus() == WithdrawStatus.COMPLETED)) {
            throw new BusinessException(ErrorCode.ALREADY_WITHDRAWN_USER);
        }

        LocalDateTime now = LocalDateTime.now();
        if (withdrawal == null) {
            withdrawal = UserWithdrawal.builder()
                    .userId(user.getId())
                    .status(WithdrawStatus.REQUESTED)
                    .build();
        }

        withdrawal.request(reqDto.getWithdrawReason(), reqDto.getDetailText(), resolveClientIp(request), now);

        userWithdrawalRepository.save(withdrawal);
    }

    @Transactional
    public void cancelWithdrawal() {
        Long userId = securityUtil.getCurrentUserId();
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));

        UserWithdrawal withdrawal = userWithdrawalRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WITHDRAWAL_REQUEST_NOT_FOUND));

        if (withdrawal.getStatus() != WithdrawStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.INVALID_WITHDRAWAL_STATUS);
        }

        withdrawal.cancel(LocalDateTime.now(), null, null);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) return null;

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
