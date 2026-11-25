package com.kemall.iam.user.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户账号聚合根（user 子域）。
 * <p>
 * 只承载账号与资料相关状态；认证/会话在 auth 子域。
 */
@Data
@Builder
public class UserAccount {

    private Long id;

    private String username;

    private String passwordHash;

    private String nickname;

    private String avatarUrl;

    private Integer status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    /**
     * 账号是否允许登录（status = 1 正常）
     */
    public boolean canLogin() {
        return status != null && status == 1;
    }

    /**
     * 展示名：昵称为空时回退用户名
     */
    public String displayName() {
        return nickname == null || nickname.isBlank() ? username : nickname;
    }
}
