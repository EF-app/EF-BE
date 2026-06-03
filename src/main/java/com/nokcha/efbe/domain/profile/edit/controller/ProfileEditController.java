package com.nokcha.efbe.domain.profile.edit.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.profile.edit.dto.request.UpdateAboutMeReqDto;
import com.nokcha.efbe.domain.profile.edit.dto.request.UpdateBasicReqDto;
import com.nokcha.efbe.domain.profile.edit.dto.request.UpdateBioReqDto;
import com.nokcha.efbe.domain.profile.edit.dto.request.UpdateIdealReqDto;
import com.nokcha.efbe.domain.profile.edit.dto.request.UpdateKeywordsReqDto;
import com.nokcha.efbe.domain.profile.edit.dto.request.UpdateLifestyleReqDto;
import com.nokcha.efbe.domain.profile.edit.dto.request.UpdateMbtiReqDto;
import com.nokcha.efbe.domain.profile.edit.dto.request.UpdateMyStyleReqDto;
import com.nokcha.efbe.domain.profile.edit.dto.request.UpdatePurposeReqDto;
import com.nokcha.efbe.domain.profile.edit.dto.response.ProfileFullRspDto;
import com.nokcha.efbe.domain.profile.edit.service.ProfileEditService;
import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "My Profile Edit", description = "마이 프로필 수정 — 섹션별 부분 갱신")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/me/profile")
public class ProfileEditController {

    private final ProfileEditService profileEditService;

    @Operation(summary = "프로필 수정 화면용 풀 조회", description = "8개 섹션 + 사진 + 닉네임/지역 까지 한 번에 반환")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public RspTemplate<ProfileFullRspDto> getFullProfile() {
        return new RspTemplate<>(HttpStatus.OK, "프로필 조회 성공", profileEditService.getFullProfile());
    }

    @Operation(summary = "기본 정보(닉네임/지역) 수정", description = "닉네임은 마지막 변경 7일 후부터 가능. 변경 안 할 필드는 null")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/basic")
    public RspTemplate<Void> updateBasic(@Valid @RequestBody UpdateBasicReqDto req) {
        profileEditService.updateBasic(req);
        return new RspTemplate<>(HttpStatus.OK, "기본 정보가 변경되었습니다.");
    }

    @Operation(summary = "프로필 사진 추가", description = "최대 5장 제한. sortOrder 는 BE 가 max+1 로 자동 부여")
    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RspTemplate<PhotoCreatedRsp> addPhoto(@RequestParam("image") MultipartFile image) {
        UserProfileImage saved = profileEditService.addPhoto(image);
        return new RspTemplate<>(HttpStatus.CREATED, "사진이 추가되었습니다.",
                new PhotoCreatedRsp(saved.getId(), saved.getUrl(), saved.getSortOrder()));
    }

    @Operation(summary = "프로필 사진 삭제", description = "본인 소유 row 만 삭제 가능. R2 객체는 별도 배치 정리")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/photos/{photoId}")
    public RspTemplate<Void> deletePhoto(@PathVariable Long photoId) {
        profileEditService.deletePhoto(photoId);
        return new RspTemplate<>(HttpStatus.OK, "사진이 삭제되었습니다.");
    }

    @Operation(summary = "관심 대상(purpose) 수정")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/purpose")
    public RspTemplate<Void> updatePurpose(@Valid @RequestBody UpdatePurposeReqDto req) {
        profileEditService.updatePurpose(req);
        return new RspTemplate<>(HttpStatus.OK, "관심 대상이 변경되었습니다.");
    }

    @Operation(summary = "관심사 키워드 수정", description = "추천 키워드 + 자유 입력 전체 교체")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/keywords")
    public RspTemplate<Void> updateKeywords(@Valid @RequestBody UpdateKeywordsReqDto req) {
        profileEditService.updateKeywords(req);
        return new RspTemplate<>(HttpStatus.OK, "관심사 키워드가 변경되었습니다.");
    }

    @Operation(summary = "생활 습관 수정", description = "음주/주종/흡연/타투 카테고리만 허용. 다른 카테고리 SELF row 는 보존")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/lifestyle")
    public RspTemplate<Void> updateLifestyle(@Valid @RequestBody UpdateLifestyleReqDto req) {
        profileEditService.updateLifestyle(req);
        return new RspTemplate<>(HttpStatus.OK, "생활 습관이 변경되었습니다.");
    }

    @Operation(summary = "나에 대해 수정", description = "일상/종교/이쪽지인/커밍아웃")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/about-me")
    public RspTemplate<Void> updateAboutMe(@Valid @RequestBody UpdateAboutMeReqDto req) {
        profileEditService.updateAboutMe(req);
        return new RspTemplate<>(HttpStatus.OK, "나에 대해 항목이 변경되었습니다.");
    }

    @Operation(summary = "내 스타일 수정", description = "머리/체형/키/성향/패션/꾸미는 스타일")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/my-style")
    public RspTemplate<Void> updateMyStyle(@Valid @RequestBody UpdateMyStyleReqDto req) {
        profileEditService.updateMyStyle(req);
        return new RspTemplate<>(HttpStatus.OK, "내 스타일이 변경되었습니다.");
    }

    @Operation(summary = "MBTI 단독 수정")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/mbti")
    public RspTemplate<Void> updateMbti(@Valid @RequestBody UpdateMbtiReqDto req) {
        profileEditService.updateMbti(req);
        return new RspTemplate<>(HttpStatus.OK, "MBTI 가 변경되었습니다.");
    }

    @Operation(summary = "이상형 수정", description = "끌리는 스타일(IDEAL personalIds) + 중요 포인트(idealPointTypes)")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/ideal")
    public RspTemplate<Void> updateIdeal(@Valid @RequestBody UpdateIdealReqDto req) {
        profileEditService.updateIdeal(req);
        return new RspTemplate<>(HttpStatus.OK, "이상형이 변경되었습니다.");
    }

    @Operation(summary = "자기소개(bio_message) 수정", description = "최대 300자. null/blank 면 비움")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/bio-message")
    public RspTemplate<Void> updateBio(@Valid @RequestBody UpdateBioReqDto req) {
        profileEditService.updateBio(req);
        return new RspTemplate<>(HttpStatus.OK, "자기소개가 변경되었습니다.");
    }

    public record PhotoCreatedRsp(Long id, String url, Integer sortOrder) {}
}
