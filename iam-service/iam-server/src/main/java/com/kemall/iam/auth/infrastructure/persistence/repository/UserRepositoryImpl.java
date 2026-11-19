package com.kemall.iam.auth.infrastructure.persistence.repository;

import com.kemall.iam.auth.domain.model.User;
import com.kemall.iam.auth.domain.repository.UserRepository;
import com.kemall.iam.auth.infrastructure.persistence.entity.SysUserEntity;
import com.kemall.iam.auth.infrastructure.persistence.mapper.SysUserMapper;
import com.kemall.iam.api.enums.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

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
            default -> throw new IllegalArgumentException("不支持的账号类型");
        };

        if (entity == null) {
            return null;
        }

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

    @Override
    public void updateLastLoginTime(Long userId, LocalDateTime loginTime) {
        userMapper.updateLastLoginTime(userId, loginTime);
    }
}