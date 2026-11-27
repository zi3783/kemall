package com.kemall.iam.tenancy.domain.repository;

import com.kemall.iam.tenancy.domain.model.Tenant;

import java.util.List;
import java.util.Optional;

/**
 * 租户仓储端口。
 */
public interface TenantRepository {

    Optional<Tenant> findById(Long tenantId);

    boolean existsByCode(String tenantCode);

    List<Tenant> findPage(String keyword, Integer status, long offset, int size);

    long count(String keyword, Integer status);

    void insert(Tenant tenant);

    void updateStatus(Long tenantId, Integer status);
}
