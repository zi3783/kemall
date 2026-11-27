package com.kemall.iam.tenancy.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.kemall.iam.tenancy.domain.model.Tenant;
import com.kemall.iam.tenancy.domain.repository.TenantRepository;
import com.kemall.iam.tenancy.infrastructure.persistence.entity.TenantPO;
import com.kemall.iam.tenancy.infrastructure.persistence.mapper.TenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 租户仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class TenantRepositoryImpl implements TenantRepository {

    private final TenantMapper tenantMapper;

    @Override
    public Optional<Tenant> findById(Long tenantId) {
        return Optional.ofNullable(tenantMapper.selectById(tenantId)).map(this::toDomain);
    }

    @Override
    public boolean existsByCode(String tenantCode) {
        Long count = tenantMapper.selectCount(Wrappers.<TenantPO>lambdaQuery()
                .eq(TenantPO::getTenantCode, tenantCode));
        return count != null && count > 0;
    }

    @Override
    public List<Tenant> findPage(String keyword, Integer status, long offset, int size) {
        return tenantMapper.selectPageList(keyword, status, offset, size)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long count(String keyword, Integer status) {
        return tenantMapper.selectPageCount(keyword, status);
    }

    @Override
    public void insert(Tenant tenant) {
        LocalDateTime now = LocalDateTime.now();
        TenantPO po = new TenantPO()
                .setId(tenant.getId() == null ? IdWorker.getId() : tenant.getId())
                .setTenantCode(tenant.getTenantCode())
                .setTenantName(tenant.getTenantName())
                .setPlanCode(tenant.getPlanCode())
                .setStatus(tenant.getStatus())
                .setExpiresAt(tenant.getExpiresAt())
                .setCreatedAt(now)
                .setUpdatedAt(now);
        tenantMapper.insert(po);
        tenant.setId(po.getId());
        tenant.setCreatedAt(now);
    }

    @Override
    public void updateStatus(Long tenantId, Integer status) {
        tenantMapper.update(null, Wrappers.<TenantPO>lambdaUpdate()
                .eq(TenantPO::getId, tenantId)
                .set(TenantPO::getStatus, status)
                .set(TenantPO::getUpdatedAt, LocalDateTime.now()));
    }

    private Tenant toDomain(TenantPO po) {
        if (po == null) {
            return null;
        }
        return Tenant.builder()
                .id(po.getId())
                .tenantCode(po.getTenantCode())
                .tenantName(po.getTenantName())
                .planCode(po.getPlanCode())
                .status(po.getStatus())
                .expiresAt(po.getExpiresAt())
                .createdAt(po.getCreatedAt())
                .build();
    }
}
