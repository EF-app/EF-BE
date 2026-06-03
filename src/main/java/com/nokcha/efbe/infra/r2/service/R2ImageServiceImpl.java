package com.nokcha.efbe.infra.r2.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.feedback.entity.Feedback;
import com.nokcha.efbe.domain.feedback.entity.FeedbackImage;
import com.nokcha.efbe.domain.feedback.repository.FeedbackImageRepository;
import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import com.nokcha.efbe.domain.user.repository.ProfileImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class R2ImageServiceImpl implements R2ImageService {

    private static final long IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
    private static final Set<String> FEEDBACK_ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "heic", "heif", "bmp"
    );
    private static final Set<String> FEEDBACK_ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "image/heic", "image/heif", "image/bmp"
    );

    private final S3Client s3Client;
    private final ProfileImageRepository profileImageRepository;
    private final FeedbackImageRepository feedbackImageRepository;

    @Value("${cloud.r2.bucket}")
    private String bucket;

    @Value("${cloud.r2.public-url}")
    private String publicUrl;

    // 프로필 이미지 업로드
    @Override
    public UserProfileImage uploadProfileImage(MultipartFile multipartFile, String directory, Long signUpSessionId, int sortOrder) {
        validateProfileImage(multipartFile);

        String originalName = multipartFile.getOriginalFilename();
        String storedName = createStoredName(originalName);
        String objectKey = directory + "/" + storedName;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType(multipartFile.getContentType())
                            .build(),
                    RequestBody.fromBytes(multipartFile.getBytes())
            );
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE, e);
        }

        String imageUrl = publicUrl + "/" + objectKey;

        UserProfileImage userProfileImage = UserProfileImage.builder()
                .signUpSessionId(signUpSessionId)
                .originalName(originalName)
                .storedName(storedName)
                .sortOrder(sortOrder)
                .url(imageUrl)
                .build();

        return profileImageRepository.save(userProfileImage);
    }

    // 프로필 이미지 업로드 (마이 프로필 수정)
    @Override
    public UserProfileImage uploadProfileImageForUser(MultipartFile multipartFile, String directory, Long userId, int sortOrder) {
        validateProfileImage(multipartFile);

        String originalName = multipartFile.getOriginalFilename();
        String storedName = createStoredName(originalName);
        String objectKey = directory + "/" + storedName;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType(multipartFile.getContentType())
                            .build(),
                    RequestBody.fromBytes(multipartFile.getBytes())
            );
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE, e);
        }

        String imageUrl = publicUrl + "/" + objectKey;

        UserProfileImage userProfileImage = UserProfileImage.builder()
                .userId(userId)
                .originalName(originalName)
                .storedName(storedName)
                .sortOrder(sortOrder)
                .url(imageUrl)
                .build();

        return profileImageRepository.save(userProfileImage);
    }

    // 피드백 첨부 이미지 업로드
    @Override
    public FeedbackImage uploadFeedbackImage(MultipartFile multipartFile, String directory, Feedback feedback, int sortOrder) {
        validateFeedbackImage(multipartFile);

        String originalName = multipartFile.getOriginalFilename();
        String storedName = createStoredName(originalName);
        String objectKey = directory + "/" + storedName;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType(multipartFile.getContentType())
                            .build(),
                    RequestBody.fromBytes(multipartFile.getBytes())
            );
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_FEEDBACK_IMAGE, e);
        }

        String imageUrl = publicUrl + "/" + objectKey;

        FeedbackImage feedbackImage = FeedbackImage.builder()
                .feedback(feedback)
                .originalName(originalName)
                .storedName(storedName)
                .sortOrder(sortOrder)
                .url(imageUrl)
                .build();

        return feedbackImageRepository.save(feedbackImage);
    }

    // 프로필 이미지 유효성 검증
    private void validateProfileImage(MultipartFile multipartFile) {
        validateImage(multipartFile,
                ALLOWED_EXTENSIONS, ALLOWED_CONTENT_TYPES, ErrorCode.INVALID_PROFILE_IMAGE);
    }

    // 피드백 이미지 유효성 검증
    private void validateFeedbackImage(MultipartFile multipartFile) {
        validateImage(multipartFile,
                FEEDBACK_ALLOWED_EXTENSIONS, FEEDBACK_ALLOWED_CONTENT_TYPES, ErrorCode.INVALID_FEEDBACK_IMAGE);
    }

    // 이미지 공통 검증
    private void validateImage(MultipartFile multipartFile,
                               Set<String> allowedExtensions, Set<String> allowedContentTypes,
                               ErrorCode errorCode) {
        if (multipartFile == null || multipartFile.isEmpty() || multipartFile.getOriginalFilename() == null) {
            throw new BusinessException(errorCode);
        }

        if (multipartFile.getSize() > R2ImageServiceImpl.IMAGE_SIZE_BYTES) {
            throw new BusinessException(errorCode);
        }

        String originalFilename = multipartFile.getOriginalFilename();
        int extensionIndex = originalFilename.lastIndexOf('.');

        if (extensionIndex < 0) {
            throw new BusinessException(errorCode);
        }

        String extension = originalFilename.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(errorCode);
        }

        String contentType = multipartFile.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(errorCode);
        }
    }

    // R2 저장 파일명 생성
    private String createStoredName(String originalFilename) {
        String extension = "";
        int extensionIndex = originalFilename.lastIndexOf('.');

        if (extensionIndex >= 0) {
            extension = originalFilename.substring(extensionIndex);
        }

        return UUID.randomUUID() + extension;
    }
}
