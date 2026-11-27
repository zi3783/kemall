package com.kemall.iam.api.dto.auth;

import com.kemall.iam.api.enums.UserType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginUserInfo {

    private Long userId;

    private String username;

    private String nickname;

    private String avatarUrl;

    /**
     * 账号具备的身份：CUSTOMER / TENANT_MEMBER / PLATFORM_MEMBER，可同时多个。
     */
    private List<UserType> userTypes;

    /**
     * 是否需要选择工作空间：存在租户或平台身份时为 true；
     * 纯消费者为 false，前端可直接进入商城，无需工作空间选择。
     */
    private Boolean workspaceRequired;
}