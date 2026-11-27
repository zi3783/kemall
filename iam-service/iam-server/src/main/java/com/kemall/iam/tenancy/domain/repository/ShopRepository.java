package com.kemall.iam.tenancy.domain.repository;

import com.kemall.iam.tenancy.domain.model.Shop;

import java.util.List;
import java.util.Optional;

/**
 * 店铺仓储端口。
 */
public interface ShopRepository {

    Optional<Shop> findById(Long shopId);

    boolean existsByTenantAndCode(Long tenantId, String shopCode);

    List<Shop> findPage(Long tenantId, String keyword, Integer status, long offset, int size);

    long count(Long tenantId, String keyword, Integer status);

    void insert(Shop shop);

    void updateStatus(Long shopId, Integer status);
}
