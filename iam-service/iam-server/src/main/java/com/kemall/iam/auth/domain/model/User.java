package com.kemall.iam.auth.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class User {

    private Long id;

    private String username;

    private String passwordHash;

    private String nickname;

    private String avatarUrl;

    private Integer status;

    private LocalDateTime lastLoginAt;

    /**
     * 判断用户是否允许登录
     * 这里假设 sys_user.status = 1 表示正常
     */
    public boolean canLogin() {
        return status != null && status == 1;
    }
}