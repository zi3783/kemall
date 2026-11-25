package com.kemall.iam.api.dto.user;

import com.kemall.iam.api.enums.IdentityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BindIdentityRequest {

    @NotNull(message = "身份类型不能为空")
    private IdentityType identityType;

    @NotBlank(message = "手机号/邮箱不能为空")
    private String identityValue;

    @NotBlank(message = "验证码不能为空")
    private String verificationCode;
}
