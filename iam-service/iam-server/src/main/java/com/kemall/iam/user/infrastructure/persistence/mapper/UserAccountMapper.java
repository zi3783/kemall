package com.kemall.iam.user.infrastructure.persistence.mapper;

import com.kemall.iam.user.infrastructure.persistence.entity.UserAccountPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * sys_user 持久化 Mapper（user 子域）。
 */
@Mapper
public interface UserAccountMapper {

    UserAccountPO selectById(@Param("id") Long id);

    /**
     * 通过已验证身份（手机号/邮箱）反查账号。
     */
    UserAccountPO selectByIdentity(
            @Param("identityType") String identityType,
            @Param("identityValue") String identityValue);

    boolean existsByUsername(@Param("username") String username);

    int insert(UserAccountPO po);

    void updateProfile(
            @Param("userId") Long userId,
            @Param("nickname") String nickname,
            @Param("avatarUrl") String avatarUrl);

    void updatePasswordHash(
            @Param("userId") Long userId,
            @Param("passwordHash") String passwordHash);
}
