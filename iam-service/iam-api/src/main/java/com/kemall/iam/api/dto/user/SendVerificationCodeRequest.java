package com.kemall.iam.api.dto.user;

import com.kemall.iam.api.enums.IdentityType;
import com.kemall.iam.api.enums.VerificationScene;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendVerificationCodeRequest {

    @NotNull(message = "验证码场景不能为空")
    private VerificationScene scene;

    @NotNull(message = "目标类型不能为空")
    private IdentityType targetType;

    @NotBlank(message = "目标不能为空")
    private String target;
}
