package com.nokcha.efbe.infra.r2.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.profile.entity.ProfileImage;
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

    private static final long MAX_PROFILE_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private final S3Client s3Client;
    private final ProfileImageRepository profileImageRepository;

    @Value("${cloud.r2.bucket}")
    private String bucket;

    @Value("${cloud.r2.public-url}")
    private String publicUrl;

    // 프로필 이미지 업로드
    @Override
    public ProfileImage uploadProfileImage(MultipartFile multipartFile, String directory, Long signUpSessionId, int sortOrder) {
        validateImage(multipartFile);

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

        ProfileImage profileImage = ProfileImage.builder()
                .signUpSessionId(signUpSessionId)
                .originalName(originalName)
                .storedName(storedName)
                .sortOrder(sortOrder)
                .url(imageUrl)
                .build();

        return profileImageRepository.save(profileImage);
    }

    // 프로필 이미지 유효성 검증
    private void validateImage(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty() || multipartFile.getOriginalFilename() == null) {
            throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE);
        }

        if (multipartFile.getSize() > MAX_PROFILE_IMAGE_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE);
        }

        String originalFilename = multipartFile.getOriginalFilename();
        int extensionIndex = originalFilename.lastIndexOf('.');

        if (extensionIndex < 0) {
            throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE);
        }

        String extension = originalFilename.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE);
        }

        String contentType = multipartFile.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE);
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
