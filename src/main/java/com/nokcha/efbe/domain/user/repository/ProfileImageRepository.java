package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProfileImageRepository extends JpaRepository<UserProfileImage, Long> {

    // 회원가입 세션 기준 프로필 이미지 조회
    List<UserProfileImage> findBySignUpSessionIdOrderBySortOrderAsc(Long signUpSessionId);

    // 회원 기준 프로필 이미지 조회 (어드민 유저 상세)
    List<UserProfileImage> findByUserIdOrderBySortOrderAsc(Long userId);

    // 회원 목록 기준 대표 프로필 이미지 조회
    List<UserProfileImage> findByUserIdInAndSortOrder(Collection<Long> userIds, Integer sortOrder);

    // 회원가입 세션 기준 프로필 이미지 삭제
    void deleteBySignUpSessionId(Long signUpSessionId);

    // 사진 추가 시 다음 sortOrder 산출용(변경이므로 sortOrder변경)
    Optional<UserProfileImage> findTopByUserIdOrderBySortOrderDesc(Long userId);

    // 사진 삭제 시 본인 소유 검증용
    Optional<UserProfileImage> findByIdAndUserId(Long id, Long userId);

    // 5장 제한 검사용(가입 시엔 List길이 자체로 검사하기 때문에, 변경될 때는 count 필요함)
    long countByUserId(Long userId);
}
