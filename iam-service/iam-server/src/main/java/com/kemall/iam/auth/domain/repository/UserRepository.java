package com.kemall.iam.auth.domain.repository;

import com.kemall.iam.auth.domain.model.User;
import com.kemall.iam.api.enums.AccountType;

import java.time.LocalDateTime;

public interface UserRepository {

    /**
     * 根据登录账号查询用户
     */
    User findByAccount(String account, AccountType accountType);

    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long userId, LocalDateTime loginTime);
}