package com.kemall.iam.auth.infrastructure.persistence.mapper;

import com.kemall.iam.auth.infrastructure.persistence.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * sys_user 持久化 Mapper。
 */
@Mapper
public interface SysUserMapper {

    SysUserEntity selectByUsername(@Param("username") String username);

    SysUserEntity selectByIdentity(
            @Param("identityType") String identityType,
            @Param("identityValue") String identityValue);

    SysUserEntity selectById(@Param("id") Long id);

    void updateLastLoginTime(
            @Param("userId") Long userId,
            @Param("loginTime") LocalDateTime loginTime);

    void updatePasswordHash(
            @Param("userId") Long userId,
            @Param("passwordHash") String passwordHash);
}
