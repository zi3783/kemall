package com.kemall.iam.api.dto.user;

import lombok.Builder;
import lombok.Data;

/**
 * 他人公开资料（不含用户名、状态、绑定信息）。
 */
@Data
@Builder
public class PublicUserProfile {

    private Long userId;

    private String nickname;

    private String avatarUrl;
}
