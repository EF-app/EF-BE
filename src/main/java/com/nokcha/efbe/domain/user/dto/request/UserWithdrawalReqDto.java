package com.nokcha.efbe.domain.user.dto.request;

import com.nokcha.efbe.domain.user.entity.WithdrawReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "회원 탈퇴 요청")
public class UserWithdrawalReqDto {

    @NotNull
    @Schema(description = "탈퇴 사유", example = "TAKING_BREAK")
    private WithdrawReason withdrawReason;

    @Size(max = 2000)
    @Schema(description = "상세 사유", example = "잠시 앱을 쉬고 싶어서 탈퇴합니다.")
    private String detailText;
}
