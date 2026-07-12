package com.nokcha.efbe.domain.user.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_uuid", columnNames = "uuid"),
                @UniqueConstraint(name = "uk_users_login_id", columnNames = "login_id"),
                @UniqueConstraint(name = "uk_users_phone", columnNames = "phone"),
                @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String uuid;

    // 본인인증(DI) 도입 시 산출·저장 — 영구정지자 재가입 차단 원장(blocked_identity) 대조 키.
    //  원문 DI 는 저장하지 않고 HMAC 해시(hex 64)만. 본인인증 미도입 현재는 전부 NULL.
    @Column(name = "di_hash", length = 64)
    private String diHash;

    // 탈퇴 완료 시 NULL 익명화 대상 — anonymize() 참조. UNIQUE 는 NULL 중복 허용.
    @Column(length = 50, unique = true)
    private String loginId;

    @Column
    private String password;

    @Column(length = 20, unique = true)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String scode;

    @Column(length = 30, unique = true)
    private String nickname;

    @Column
    private LocalDate birth;

    @Column
    private Integer age;

    @Column(nullable = false)
    private Long areaId;

    @Column
    private LocalDateTime lastActiveAt;

    @Column
    private LocalDateTime lastNicknameChangedAt;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Builder
    public User(String uuid, String loginId, String password, String phone, String email, String scode, String nickname, LocalDate birth, Integer age, Long areaId, String fcmToken, LocalDateTime lastNicknameChangedAt, UserStatus status, LocalDateTime lastActiveAt) {
        this.uuid = uuid;
        this.loginId = loginId;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.scode = scode;
        this.nickname = nickname;
        this.birth = birth;
        this.age = age;
        this.areaId = areaId;
        this.fcmToken = fcmToken;
        this.lastNicknameChangedAt = lastNicknameChangedAt;
        this.status = status == null ? UserStatus.ACTIVE : status;
        this.lastActiveAt = lastActiveAt;
    }

    // 마지막 활동 시각 갱신 (주요 액션 시)
    public void updateLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    // FCM 토큰 등록/갱신
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    // FCM 토큰 삭제
    public void clearFcmToken() {
        this.fcmToken = null;
    }

    // 보안코드 설정/수정
    public void updateScode(String scode) {
        this.scode = scode;
    }

    // 비밀번호 수정 (인코딩된 값 전달)
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 닉네임 변경 시각 갱신 (7일 쿨다운)
    public void markNicknameChanged(LocalDateTime when) {
        this.lastNicknameChangedAt = when;
    }

    // 닉네임 변경 + 마지막 변경 시각 동시 갱신 (마이 프로필 수정)
    public void updateNickname(String nickname, LocalDateTime when) {
        this.nickname = nickname;
        this.lastNicknameChangedAt = when;
    }

    // 지역 변경 (마이 프로필 수정)
    public void updateAreaId(Long areaId) {
        this.areaId = areaId;
    }

    // 상태 갱신
    public void changeStatus(UserStatus next) {
        this.status = next;
    }

    // 탈퇴 신청 (30일 유예)
    public void requestWithdrawal() {
        this.status = UserStatus.WITHDRAWING;
    }

    // 탈퇴 완료 (배치) — status 플립만. 실제 PII 파기는 anonymize() 에서 수행.
    public void completeWithdrawal() {
        this.status = UserStatus.WITHDRAWN;
    }

    // 개인정보 파기(익명화) — 탈퇴 완료(30일 경과) 시 파기 배치가 호출.
    //  PII 컬럼을 NULL 로 밀고 WITHDRAWN 으로 전환. id/uuid/area_id 는 회계·통계·표시용으로 유지.
    //  닉네임이 NULL 이 되면 노출 지점에서 "탈퇴한 회원" 으로 표시된다.
    public void anonymize() {
        this.loginId = null;
        this.password = null;
        this.phone = null;
        this.email = null;
        this.scode = null;
        this.nickname = null;
        this.birth = null;
        this.age = null;
        this.fcmToken = null;
        this.diHash = null; // 일반 파기는 차단 흔적 미보존(쿨다운 없음). 영구정지 차단은 ban 시점에 이미 원장 등록됨.
        this.status = UserStatus.WITHDRAWN;
    }

    // 본인인증(DI) 완료 시 di_hash 주입 — 가입 완료/소급 백필에서 호출. 원문 DI 는 받지 않음.
    public void assignDiHash(String diHash) {
        this.diHash = diHash;
    }

    // 탈퇴 대기(유예 30일) 여부 — 로그인 허용하되 철회만 가능한 상태
    public boolean isWithdrawing() {
        return this.status == UserStatus.WITHDRAWING;
    }

    // 탈퇴 완료(파기) 여부 — 로그인 완전 차단 대상
    public boolean isWithdrawn() {
        return this.status == UserStatus.WITHDRAWN;
    }

    // 탈퇴 진행 여부 (신청~완료 전체)
    public boolean isWithdrawnOrWithdrawing() {
        return this.status == UserStatus.WITHDRAWING || this.status == UserStatus.WITHDRAWN;
    }
}
