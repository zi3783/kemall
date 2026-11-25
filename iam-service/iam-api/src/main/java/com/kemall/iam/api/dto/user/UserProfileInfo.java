package com.kemall.iam.api.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 本人资料（含用户名、注册时间等仅本人可见字段）。
 */
@Data
@Builder
public class UserProfileInfo {

    private Long userId;

    private String username;

    private String nickname;

    private String avatarUrl;

    private LocalDateTime createdAt;
}
