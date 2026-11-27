package com.kemall.iam.tenancy.application.service;

import com.kemall.iam.api.dto.common.PageResult;
import com.kemall.iam.api.dto.tenancy.CreateShopRequest;
import com.kemall.iam.api.dto.tenancy.ShopInfo;
import com.kemall.iam.api.enums.ShopStatus;
import com.kemall.iam.auth.application.model.LoginPrincipal;
import com.kemall.iam.tenancy.application.support.AfterCommitExecutor;
import com.kemall.iam.tenancy.application.support.PageParams;
import com.kemall.iam.tenancy.domain.exception.ShopNotFoundException;
import com.kemall.iam.tenancy.domain.exception.TenancyBusinessException;
import com.kemall.iam.tenancy.domain.exception.TenancyErrorCode;
import com.kemall.iam.tenancy.domain.model.Shop;
import com.kemall.iam.tenancy.domain.port.MembershipSessionRevoker;
import com.kemall.iam.tenancy.domain.repository.ShopRepository;
import com.kemall.iam.tenancy.domain.service.MembershipBelongingService;
import com.kemall.iam.tenancy.domain.service.TenantAccessPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 店铺应用服务：查询、创建、状态管理。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShopAppService {

    private final ShopRepository shopRepository;
    private final TenantAccessPolicy tenantAccessPolicy;
    private final MembershipBelongingService membershipBelongingService;
    private final MembershipSessionRevoker membershipSessionRevoker;

    public PageResult<ShopInfo> page(LoginPrincipal principal, Long tenantIdParam, String keyword,
                                     Integer status, int page, int size) {
        validateStatus(status);
        PageParams params = PageParams.of(page, size);

        if (tenantAccessPolicy.isShopWorkspace(principal)) {
            return pageOwnShop(principal, keyword, status, params);
        }

        Long tenantId;
        if (tenantAccessPolicy.isPlatform(principal)) {
            tenantId = tenantIdParam;
        } else {
            tenantId = tenantAccessPolicy.resolveTenantId(principal, tenantIdParam);
        }

        List<ShopInfo> records = shopRepository
                .findPage(tenantId, keyword, status, params.offset(), params.size())
                .stream()
                .map(this::toInfo)
                .toList();
        long total = shopRepository.count(tenantId, keyword, status);
        return PageResult.of(records, total, params.page(), params.size());
    }

    public ShopInfo get(LoginPrincipal principal, Long shopId) {
        Shop shop = requireShop(shopId);
        checkReadable(principal, shop);
        return toInfo(shop);
    }

    @Transactional(rollbackFor = Exception.class)
    public ShopInfo create(LoginPrincipal principal, CreateShopRequest request) {
        if (tenantAccessPolicy.isShopWorkspace(principal)) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED);
        }
        Long tenantId = tenantAccessPolicy.resolveTenantId(principal, request.getTenantId());
        membershipBelongingService.requireActiveTenant(tenantId);

        if (shopRepository.existsByTenantAndCode(tenantId, request.getShopCode())) {
            throw new TenancyBusinessException(TenancyErrorCode.SHOP_CODE_DUPLICATED,
                    Map.of("shopCode", request.getShopCode()));
        }
        Shop shop = Shop.builder()
                .tenantId(tenantId)
                .shopCode(request.getShopCode())
                .shopName(request.getShopName())
                .logoUrl(request.getLogoUrl())
                .status(ShopStatus.ACTIVE.getCode())
                .timezone(request.getTimezone())
                .build();
        try {
            shopRepository.insert(shop);
        } catch (DuplicateKeyException e) {
            throw new TenancyBusinessException(TenancyErrorCode.SHOP_CODE_DUPLICATED,
                    Map.of("shopCode", request.getShopCode()));
        }
        log.info("[TENANCY] 创建店铺 shopId={} tenantId={} code={}",
                shop.getId(), tenantId, shop.getShopCode());
        return toInfo(shop);
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(LoginPrincipal principal, Long shopId, Integer targetStatus) {
        if (tenantAccessPolicy.isShopWorkspace(principal)) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED);
        }
        Shop shop = requireShop(shopId);
        if (!tenantAccessPolicy.isPlatform(principal)
                && !Objects.equals(shop.getTenantId(), principal.getTenantId())) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED);
        }
        ShopStatus target = ShopStatus.fromCode(targetStatus);
        if (target == null || !shop.canTransitTo(targetStatus)) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_REQUEST_INVALID,
                    Map.of("status", String.valueOf(targetStatus)));
        }
        shopRepository.updateStatus(shopId, targetStatus);
        if (target == ShopStatus.DISABLED) {
            AfterCommitExecutor.afterCommit(() ->
                    membershipSessionRevoker.revokeShop(shop.getTenantId(), shopId));
        }
        log.info("[TENANCY] 店铺状态变更 shopId={} status={}", shopId, targetStatus);
    }

    private PageResult<ShopInfo> pageOwnShop(LoginPrincipal principal, String keyword,
                                             Integer status, PageParams params) {
        Long shopId = principal.getShopId();
        if (shopId == null) {
            return PageResult.of(List.of(), 0, params.page(), params.size());
        }
        Shop shop = shopRepository.findById(shopId).orElse(null);
        if (shop == null || !matches(shop, keyword, status)) {
            return PageResult.of(List.of(), 0, params.page(), params.size());
        }
        return PageResult.of(List.of(toInfo(shop)), 1, 1, params.size());
    }

    private boolean matches(Shop shop, String keyword, Integer status) {
        if (status != null && !status.equals(shop.getStatus())) {
            return false;
        }
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        return shop.getShopCode().contains(keyword) || shop.getShopName().contains(keyword);
    }

    private void checkReadable(LoginPrincipal principal, Shop shop) {
        if (tenantAccessPolicy.isPlatform(principal)) {
            return;
        }
        if (tenantAccessPolicy.isShopWorkspace(principal)) {
            if (!Objects.equals(shop.getId(), principal.getShopId())) {
                throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED);
            }
            return;
        }
        if (!Objects.equals(shop.getTenantId(), principal.getTenantId())) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED);
        }
    }

    private Shop requireShop(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException(shopId));
    }

    private void validateStatus(Integer status) {
        if (status != null && ShopStatus.fromCode(status) == null) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_REQUEST_INVALID,
                    Map.of("status", String.valueOf(status)));
        }
    }

    private ShopInfo toInfo(Shop shop) {
        return ShopInfo.builder()
                .id(shop.getId())
                .tenantId(shop.getTenantId())
                .shopCode(shop.getShopCode())
                .shopName(shop.getShopName())
                .logoUrl(shop.getLogoUrl())
                .status(shop.getStatus())
                .timezone(shop.getTimezone())
                .createdAt(shop.getCreatedAt())
                .build();
    }
}
