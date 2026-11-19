package com.kemall.iam.api.dto.auth;

import com.kemall.iam.api.enums.AccountType;
import com.kemall.iam.api.enums.ClientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotNull(message = "账号类型不能为空")
    private AccountType accountType;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotNull(message = "客户端类型不能为空")
    private ClientType clientType;

    private String deviceId;

    private String deviceName;
}