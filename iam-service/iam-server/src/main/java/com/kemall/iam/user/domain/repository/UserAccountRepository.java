package com.kemall.iam.user.domain.repository;

import com.kemall.iam.api.enums.IdentityType;
import com.kemall.iam.user.domain.model.UserAccount;
import com.kemall.iam.user.domain.model.UserIdentityRef;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户账号仓储端口（user 子域）。
 * <p>
 * 领域/应用层只依赖此接口，持久化细节在 infrastructure 实现。
 */
public interface UserAccountRepository {

    UserAccount findById(Long userId);

    UserAccount findByIdentity(IdentityType identityType, String identityValue);

    boolean existsByUsername(String username);

    boolean existsIdentity(IdentityType identityType, String identityValue);

    /**
     * 新建账号，返回生成的 userId。
     */
    Long create(UserAccount account);

    /**
     * 更新资料（只更新非 null 字段）。
     */
    void updateProfile(Long userId, String nickname, String avatarUrl);

    void updatePasswordHash(Long userId, String newPasswordHash);

    List<UserIdentityRef> listIdentities(Long userId);

    void bindIdentity(Long userId, IdentityType identityType, String identityValue, LocalDateTime verifiedAt);

    void unbindIdentity(Long userId, IdentityType identityType);

    /**
     * 统计已验证身份数量（判断能否解绑）。
     */
    int countVerifiedIdentities(Long userId);
}
