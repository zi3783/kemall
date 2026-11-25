package com.kemall.iam.auth.infrastructure.persistence.repository;

import com.kemall.iam.auth.domain.model.User;
import com.kemall.iam.auth.domain.repository.UserRepository;
import com.kemall.iam.auth.infrastructure.persistence.entity.SysUserEntity;
import com.kemall.iam.auth.infrastructure.persistence.mapper.SysUserMapper;
import com.kemall.iam.api.enums.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * UserRepository 的基础设施实现（DDD Anti-Corruption Layer）。
 * <p>
 * 负责数据库实体与领域模型之间的转换，
 * 领域层从不直接感知 MyBatis-Plus 或 SQL。
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final SysUserMapper userMapper;

    @Override
    public User findByAccount(String account, AccountType accountType) {
        SysUserEntity entity = switch (accountType) {
            case USERNAME -> userMapper.selectByUsername(account);
            case MOBILE -> userMapper.selectByIdentity("MOBILE", account);
            case EMAIL -> userMapper.selectByIdentity("EMAIL", account);
            default -> throw new IllegalArgumentException("不支持的账号类型: " + accountType);
        };
        return toDomain(entity);
    }

    @Override
    public User findById(Long userId) {
        return toDomain(userMapper.selectById(userId));
    }

    @Override
    public void updateLastLoginTime(Long userId, LocalDateTime loginTime) {
        userMapper.updateLastLoginTime(userId, loginTime);
    }

    @Override
    public void updatePasswordHash(Long userId, String newPasswordHash) {
        userMapper.updatePasswordHash(userId, newPasswordHash);
    }

    private User toDomain(SysUserEntity entity) {
        if (entity == null) return null;
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .passwordHash(entity.getPasswordHash())
                .nickname(entity.getNickname())
                .avatarUrl(entity.getAvatarUrl())
                .status(entity.getStatus())
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }
}
