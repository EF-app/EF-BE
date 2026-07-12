package com.nokcha.efbe.domain.profile.repository;

import com.nokcha.efbe.domain.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);

    // 어드민 목록에서 profile_status 배치 조회용
    List<UserProfile> findByUserIdIn(Collection<Long> userIds);

    // 탈퇴 파기용
    @Modifying
    @Query("delete from UserProfile up where up.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
