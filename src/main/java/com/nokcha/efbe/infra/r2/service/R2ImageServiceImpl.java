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

    // dev 환경에서 R2 endpoint 가 없을 때 실제 업로드를 건너뛰고 placeholder URL 만 저장. application-dev.yml 에서 true 로 설정.
    /*
      운영 시 원복

        R2 계정 발급 받은 후:
        1. application-dev.yml(또는 prod)의 endpoint/access-key/secret-key/public-url을 실제 값으로 채우기
        2. bypass-upload: false 또는 라인 자체 삭제
        3. 재시작 → 정상 업로드 동작
     */
    @Value("${cloud.r2.bypass-upload:false}")
    private boolean bypassUpload;

    // 프로필 이미지 업로드
    @Override
    public ProfileImage uploadProfileImage(MultipartFile multipartFile, String directory, Long signUpSessionId, int sortOrder) {
        validateImage(multipartFile);

        String originalName = multipartFile.getOriginalFilename();
        String storedName = createStoredName(originalName);
        String objectKey = directory + "/" + storedName;

        if (!bypassUpload) {
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
        }

        String imageUrl = (publicUrl == null || publicUrl.isBlank() ? "https://dev-mock-r2.local" : publicUrl) + "/" + objectKey;

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
