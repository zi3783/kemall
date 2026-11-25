package com.kemall.iam.auth.domain.repository;

import com.kemall.iam.auth.domain.model.User;
import com.kemall.iam.api.enums.AccountType;

import java.time.LocalDateTime;

/**
 * 用户仓储端口。
 * <p>
 * 领域层通过它获取/变更用户聚合；
 * 具体实现落在 infrastructure.persistence。
 */
public interface UserRepository {

    /**
     * 根据登录账号查询用户
     */
    User findByAccount(String account, AccountType accountType);

    /**
     * 根据主键查询用户
     */
    User findById(Long userId);

    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long userId, LocalDateTime loginTime);

    /**
     * 更新密码哈希（BCrypt 加密后的字符串）
     */
    void updatePasswordHash(Long userId, String newPasswordHash);
}
