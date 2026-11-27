package com.kemall.iam.tenancy.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.kemall.iam.tenancy.domain.model.Shop;
import com.kemall.iam.tenancy.domain.repository.ShopRepository;
import com.kemall.iam.tenancy.infrastructure.persistence.entity.ShopPO;
import com.kemall.iam.tenancy.infrastructure.persistence.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 店铺仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class ShopRepositoryImpl implements ShopRepository {

    private static final String DEFAULT_TIMEZONE = "Asia/Shanghai";

    private final ShopMapper shopMapper;

    @Override
    public Optional<Shop> findById(Long shopId) {
        return Optional.ofNullable(shopMapper.selectById(shopId)).map(this::toDomain);
    }

    @Override
    public boolean existsByTenantAndCode(Long tenantId, String shopCode) {
        Long count = shopMapper.selectCount(Wrappers.<ShopPO>lambdaQuery()
                .eq(ShopPO::getTenantId, tenantId)
                .eq(ShopPO::getShopCode, shopCode));
        return count != null && count > 0;
    }

    @Override
    public List<Shop> findPage(Long tenantId, String keyword, Integer status, long offset, int size) {
        return shopMapper.selectPageList(tenantId, keyword, status, offset, size)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long count(Long tenantId, String keyword, Integer status) {
        return shopMapper.selectPageCount(tenantId, keyword, status);
    }

    @Override
    public void insert(Shop shop) {
        LocalDateTime now = LocalDateTime.now();
        ShopPO po = new ShopPO()
                .setId(shop.getId() == null ? IdWorker.getId() : shop.getId())
                .setTenantId(shop.getTenantId())
                .setShopCode(shop.getShopCode())
                .setShopName(shop.getShopName())
                .setLogoUrl(shop.getLogoUrl())
                .setStatus(shop.getStatus())
                .setTimezone(shop.getTimezone() == null ? DEFAULT_TIMEZONE : shop.getTimezone())
                .setCreatedAt(now)
                .setUpdatedAt(now);
        shopMapper.insert(po);
        shop.setId(po.getId());
        shop.setTimezone(po.getTimezone());
        shop.setCreatedAt(now);
    }

    @Override
    public void updateStatus(Long shopId, Integer status) {
        shopMapper.update(null, Wrappers.<ShopPO>lambdaUpdate()
                .eq(ShopPO::getId, shopId)
                .set(ShopPO::getStatus, status)
                .set(ShopPO::getUpdatedAt, LocalDateTime.now()));
    }

    private Shop toDomain(ShopPO po) {
        if (po == null) {
            return null;
        }
        return Shop.builder()
                .id(po.getId())
                .tenantId(po.getTenantId())
                .shopCode(po.getShopCode())
                .shopName(po.getShopName())
                .logoUrl(po.getLogoUrl())
                .status(po.getStatus())
                .timezone(po.getTimezone())
                .createdAt(po.getCreatedAt())
                .build();
    }
}
