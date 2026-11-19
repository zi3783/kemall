package com.kemall.iam.api.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse {

    private LoginUserInfo userInfo;

    private TokenInfo tokenInfo;
}