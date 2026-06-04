package com.nokcha.efbe.domain.user.service;

import com.nokcha.efbe.common.util.LoginUtil;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.user.dto.response.AccountMaskedRspDto;
import com.nokcha.efbe.domain.user.dto.response.AccountRevealRspDto;
import com.nokcha.efbe.domain.user.dto.request.*;
import com.nokcha.efbe.domain.user.dto.response.UserSummaryRspDto;
import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import com.nokcha.efbe.domain.user.entity.*;
import com.nokcha.efbe.domain.user.repository.ProfileImageRepository;
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
    private final ProfileImageRepository profileImageRepository;
    private final SecurityUtil securityUtil;
    private final LoginUtil loginUtil;
    private final PasswordEncoder passwordEncoder;

    // 내 정보 요약 — 닉네임/지역/나이/대표 사진 (MY 탭 ProfileHeroCard 용)
    @Transactional(readOnly = true)
    public UserSummaryRspDto getMySummary() {
        Long userId = securityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        CodeArea area = user.getAreaId() == null
                ? null
                : areaRepository.findById(user.getAreaId()).orElse(null);
        String profileImageUrl = profileImageRepository.findByUserIdOrderBySortOrderAsc(userId)
                .stream().findFirst().map(UserProfileImage::getUrl).orElse(null);
        return UserSummaryRspDto.of(user, area, profileImageUrl);
    }

    // FCM 토큰 등록/갱신
    @Transactional
    public void updateFcmToken(FcmTokenReqDto reqDto) {
        User user = getCurrentUser();
        String fcmToken = reqDto.getFcmToken().trim();

        userRepository.findByFcmToken(fcmToken)
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(User::clearFcmToken);

        user.updateFcmToken(fcmToken);
    }

    // FCM 토큰 삭제
    @Transactional
    public void deleteFcmToken() {
        User user = getCurrentUser();
        user.clearFcmToken();
    }

    // 보안코드 설정
    @Transactional
    public void createScode(UserScodeReqDto reqDto) {
        if (!reqDto.getScode().equals(reqDto.getScodeConfirm())) {
            throw new BusinessException(ErrorCode.SCODE_CONFIRM_MISMATCH);
        }

        User user = userRepository.findById(securityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));

        user.updateScode(passwordEncoder.encode(reqDto.getScode()));
    }

    // 탈퇴
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

        withdrawal.request(reqDto.getWithdrawReason(), reqDto.getDetailText(), loginUtil.resolveClientIp(request), now);
        user.requestWithdrawal();   // 유저 테이블 수정

        userWithdrawalRepository.save(withdrawal);
    }

    // 탈퇴 취소
    @Transactional
    public void cancelWithdrawal() {
        Long userId = securityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));

        UserWithdrawal withdrawal = userWithdrawalRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WITHDRAWAL_REQUEST_NOT_FOUND));

        if (withdrawal.getStatus() != WithdrawStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.INVALID_WITHDRAWAL_STATUS);
        }

        withdrawal.cancel(LocalDateTime.now(), null, null);
        user.changeStatus(UserStatus.ACTIVE);
    }

    // 비밀번호 인증 전 계정 정보 마스킹 조회
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

    // 비밀번호 인증 후 계정 정보 전체 조회
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
        // login null이거나 빈 값일 리가 없는데 왜 이 로직이 필요하지
        if (loginId == null || loginId.isEmpty()) {
            return loginId;
        }

        // 로그인 아이디는 무조건 4~16자 사이임 -> 이 로직 변경 필요
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
