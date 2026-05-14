package com.nokcha.efbe.domain.user.account.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.user.account.dto.request.AccountRevealReqDto;
import com.nokcha.efbe.domain.user.account.dto.request.PasswordChangeReqDto;
import com.nokcha.efbe.domain.user.account.dto.request.ScodeChangeReqDto;
import com.nokcha.efbe.domain.user.account.dto.request.ScodeResetReqDto;
import com.nokcha.efbe.domain.user.account.dto.request.ScodeVerifyReqDto;
import com.nokcha.efbe.domain.user.account.dto.response.AccountMaskedRspDto;
import com.nokcha.efbe.domain.user.account.dto.response.AccountRevealRspDto;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final PasswordEncoder passwordEncoder;

    // 계정 정보 마스킹 조회 — 화면 진입 시
    @Transactional(readOnly = true)
    public AccountMaskedRspDto getMaskedAccount() {
        User user = getCurrentUser();
        String email = user.getEmail();

        return AccountMaskedRspDto.builder()
                .maskedLoginId(maskLoginId(user.getLoginId()))
                .maskedEmail(email == null ? null : maskEmail(email))
                .hasEmail(email != null && !email.isBlank())
                .hasScode(user.getScode() != null && !user.getScode().isBlank())
                .build();
    }

    // 계정 정보 전체 조회 — 비밀번호 재인증 후 평문 반환
    @Transactional(readOnly = true)
    public AccountRevealRspDto revealAccount(AccountRevealReqDto reqDto) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(reqDto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        return AccountRevealRspDto.builder()
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .build();
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(PasswordChangeReqDto reqDto) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(reqDto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        if (!reqDto.getNewPassword().equals(reqDto.getNewPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        if (passwordEncoder.matches(reqDto.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        user.updatePassword(passwordEncoder.encode(reqDto.getNewPassword()));
    }

    // 보안코드 검증 — 변경 화면 첫 단계에서 즉시 일치 여부만 확인 (변경 없음)
    @Transactional(readOnly = true)
    public void verifyScode(ScodeVerifyReqDto reqDto) {
        User user = getCurrentUser();
        String currentScode = user.getScode();

        if (currentScode == null || currentScode.isBlank()) {
            throw new BusinessException(ErrorCode.WRONG_SCODE);
        }

        if (!passwordEncoder.matches(reqDto.getScode(), currentScode)) {
            throw new BusinessException(ErrorCode.WRONG_SCODE);
        }
    }

    // 보안코드 변경 — 기존 보안코드 검증 후 변경
    @Transactional
    public void changeScode(ScodeChangeReqDto reqDto) {
        User user = getCurrentUser();
        String currentScode = user.getScode();

        if (currentScode == null || currentScode.isBlank()) {
            throw new BusinessException(ErrorCode.WRONG_SCODE);
        }

        if (!passwordEncoder.matches(reqDto.getOldScode(), currentScode)) {
            throw new BusinessException(ErrorCode.WRONG_SCODE);
        }

        if (!reqDto.getNewScode().equals(reqDto.getNewScodeConfirm())) {
            throw new BusinessException(ErrorCode.SCODE_CONFIRM_MISMATCH);
        }

        if (passwordEncoder.matches(reqDto.getNewScode(), currentScode)) {
            throw new BusinessException(ErrorCode.SAME_AS_OLD_SCODE);
        }

        user.updateScode(passwordEncoder.encode(reqDto.getNewScode()));
    }

    // 보안코드 초기화 — 기존 보안코드를 잊은 경우 비밀번호 재인증으로 새 값 설정
    @Transactional
    public void resetScode(ScodeResetReqDto reqDto) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(reqDto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        if (!reqDto.getNewScode().equals(reqDto.getNewScodeConfirm())) {
            throw new BusinessException(ErrorCode.SCODE_CONFIRM_MISMATCH);
        }

        user.updateScode(passwordEncoder.encode(reqDto.getNewScode()));
    }

    private User getCurrentUser() {
        return userRepository.findById(securityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));
    }

    // 앞쪽 최소 1글자 보존, 뒤쪽 마스킹
    private static String maskLoginId(String loginId) {
        if (loginId == null || loginId.isEmpty()) {
            return loginId;
        }

        int len = loginId.length();
        if (len == 1) {
            return loginId;
        }
        if (len == 2) {
            return loginId.charAt(0) + "*";
        }
        if (len == 3) {
            return loginId.charAt(0) + "**";
        }
        if (len == 4) {
            return loginId.substring(0, 2) + "**";
        }
        return loginId.substring(0, 4) + "*".repeat(len - 4);
    }

    // 로컬 앞 2 + ****, 도메인 SLD 앞 1 + ***, TLD 유지
    private static String maskEmail(String email) {
        int atIdx = email.indexOf('@');
        if (atIdx <= 0 || atIdx == email.length() - 1) {
            return email;
        }

        String local = email.substring(0, atIdx);
        String domain = email.substring(atIdx + 1);

        String maskedLocal = (local.length() >= 2 ? local.substring(0, 2) : local) + "****";

        int dotIdx = domain.indexOf('.');
        String maskedDomain;
        if (dotIdx <= 0) {
            maskedDomain = (domain.isEmpty() ? "" : domain.charAt(0) + "***");
        } else {
            String sld = domain.substring(0, dotIdx);
            String tld = domain.substring(dotIdx);
            maskedDomain = sld.charAt(0) + "***" + tld;
        }

        return maskedLocal + "@" + maskedDomain;
    }
}
