package com.kemall.iam.api.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnbindIdentityRequest {

    @NotBlank(message = "验证码不能为空")
    private String verificationCode;
}
