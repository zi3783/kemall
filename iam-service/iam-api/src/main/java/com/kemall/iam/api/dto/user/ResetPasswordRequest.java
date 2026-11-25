package com.kemall.iam.api.dto.user;

import com.kemall.iam.api.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    /**
     * 仅支持 MOBILE / EMAIL，用户名不支持自助重置
     */
    @NotNull(message = "账号类型不能为空")
    private AccountType accountType;

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "验证码不能为空")
    private String verificationCode;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$", message = "新密码需 8~64 位且同时包含字母和数字")
    private String newPassword;
}
