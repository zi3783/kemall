package com.kemall.iam.tenancy.domain.service;

import com.kemall.iam.tenancy.domain.exception.ShopNotFoundException;
import com.kemall.iam.tenancy.domain.exception.TenancyBusinessException;
import com.kemall.iam.tenancy.domain.exception.TenancyErrorCode;
import com.kemall.iam.tenancy.domain.exception.TenantMemberNotFoundException;
import com.kemall.iam.tenancy.domain.exception.TenantNotFoundException;
import com.kemall.iam.tenancy.domain.model.Shop;
import com.kemall.iam.tenancy.domain.model.Tenant;
import com.kemall.iam.tenancy.domain.model.TenantMember;
import com.kemall.iam.tenancy.domain.repository.ShopRepository;
import com.kemall.iam.tenancy.domain.repository.TenantMemberRepository;
import com.kemall.iam.tenancy.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 成员与租户、店铺归属关系校验（清单第 8 项）。
 * <p>
 * 所有涉及租户/店铺/成员的写操作入口都必须经过这里，
 * 保证"成员属于租户、店铺属于租户、店铺成员属于对应店铺"。
 */
@Component
@RequiredArgsConstructor
public class MembershipBelongingService {

    private final TenantRepository tenantRepository;
    private final ShopRepository shopRepository;
    private final TenantMemberRepository tenantMemberRepository;

    public Tenant findTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));
    }

    /**
     * 写操作前校验租户可用（状态正常且未过期）。
     */
    public Tenant requireActiveTenant(Long tenantId) {
        Tenant tenant = findTenant(tenantId);
        if (!tenant.isUsable(LocalDateTime.now())) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANT_UNAVAILABLE,
                    Map.of("tenantId", tenantId));
        }
        return tenant;
    }

    /**
     * 校验店铺存在、属于指定租户且处于可用状态。
     */
    public Shop requireTenantShop(Long tenantId, Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException(shopId));
        if (!shop.belongsTo(tenantId)) {
            throw new TenancyBusinessException(TenancyErrorCode.SHOP_NOT_IN_TENANT,
                    Map.of("tenantId", tenantId, "shopId", shopId));
        }
        if (!shop.isActive()) {
            throw new TenancyBusinessException(TenancyErrorCode.SHOP_UNAVAILABLE,
                    Map.of("shopId", shopId));
        }
        return shop;
    }

    /**
     * 校验成员属于指定租户且未移除。
     */
    public TenantMember requireMemberOfTenant(Long tenantId, Long memberId) {
        TenantMember member = tenantMemberRepository.findById(memberId)
                .orElseThrow(() -> new TenantMemberNotFoundException(memberId));
        if (member.isRemoved()) {
            throw new TenancyBusinessException(TenancyErrorCode.MEMBER_UNAVAILABLE,
                    Map.of("memberId", memberId));
        }
        if (!member.belongsToTenant(tenantId)) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED,
                    Map.of("tenantId", tenantId, "memberId", memberId));
        }
        return member;
    }

    /**
     * 校验成员属于指定租户下的指定店铺。
     */
    public TenantMember requireMemberOfShop(Long tenantId, Long shopId, Long memberId) {
        TenantMember member = requireMemberOfTenant(tenantId, memberId);
        if (!member.belongsToShop(tenantId, shopId)) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED,
                    Map.of("tenantId", tenantId, "shopId", shopId, "memberId", memberId));
        }
        return member;
    }
}
