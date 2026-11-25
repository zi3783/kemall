package com.kemall.iam.api.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前登录用户信息。
 */
@Data
@Builder
public class CurrentUserInfo {

    private Long userId;

    private String username;

    private String nickname;

    private String avatarUrl;

    private Integer status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private List<IdentityInfo> identities;
}
