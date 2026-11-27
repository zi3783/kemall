package com.kemall.iam.tenancy.domain.repository;

import com.kemall.iam.tenancy.domain.model.TenantMember;
import com.kemall.iam.tenancy.domain.model.TenantMemberView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 租户/店铺成员仓储端口。
 */
public interface TenantMemberRepository {

    Optional<TenantMember> findById(Long memberId);

    Optional<TenantMember> findByTenantShopUser(Long tenantId, Long shopId, Long userId);

    List<TenantMemberView> findPage(Long tenantId, Long shopId, Integer memberType,
                                    Integer status, long offset, int size);

    long count(Long tenantId, Long shopId, Integer memberType, Integer status);

    Long insert(TenantMember member);

    void updateStatus(Long memberId, Integer status);

    /**
     * 重新加入：恢复软删除行。
     */
    void restore(Long memberId, Integer memberType, LocalDateTime joinedAt);

    /**
     * 逻辑移除：status=2 + deleted_at。
     */
    void markRemoved(Long memberId, LocalDateTime removedAt);

    List<Long> listUserIdsByTenant(Long tenantId);

    List<Long> listUserIdsByShop(Long tenantId, Long shopId);
}
