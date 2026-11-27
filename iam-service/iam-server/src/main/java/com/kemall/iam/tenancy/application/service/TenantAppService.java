package com.kemall.iam.tenancy.application.service;

import com.kemall.iam.api.dto.common.PageResult;
import com.kemall.iam.api.dto.tenancy.CreateTenantRequest;
import com.kemall.iam.api.dto.tenancy.TenantInfo;
import com.kemall.iam.api.enums.TenantStatus;
import com.kemall.iam.auth.application.model.LoginPrincipal;
import com.kemall.iam.tenancy.application.support.AfterCommitExecutor;
import com.kemall.iam.tenancy.application.support.PageParams;
import com.kemall.iam.tenancy.domain.exception.TenancyBusinessException;
import com.kemall.iam.tenancy.domain.exception.TenancyErrorCode;
import com.kemall.iam.tenancy.domain.exception.TenantNotFoundException;
import com.kemall.iam.tenancy.domain.model.Tenant;
import com.kemall.iam.tenancy.domain.port.MembershipSessionRevoker;
import com.kemall.iam.tenancy.domain.repository.TenantRepository;
import com.kemall.iam.tenancy.domain.service.TenantAccessPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 租户应用服务：查询、创建、状态管理。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantAppService {

    private final TenantRepository tenantRepository;
    private final TenantAccessPolicy tenantAccessPolicy;
    private final MembershipSessionRevoker membershipSessionRevoker;

    public PageResult<TenantInfo> page(LoginPrincipal principal, String keyword,
                                       Integer status, int page, int size) {
        tenantAccessPolicy.requirePlatform(principal);
        validateStatus(status);
        PageParams params = PageParams.of(page, size);
        List<TenantInfo> records = tenantRepository
                .findPage(keyword, status, params.offset(), params.size())
                .stream()
                .map(this::toInfo)
                .toList();
        long total = tenantRepository.count(keyword, status);
        return PageResult.of(records, total, params.page(), params.size());
    }

    public TenantInfo get(LoginPrincipal principal, Long tenantId) {
        tenantAccessPolicy.requireTenantReadable(principal, tenantId);
        return toInfo(requireTenant(tenantId));
    }

    @Transactional(rollbackFor = Exception.class)
    public TenantInfo create(LoginPrincipal principal, CreateTenantRequest request) {
        tenantAccessPolicy.requirePlatform(principal);
        if (tenantRepository.existsByCode(request.getTenantCode())) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANT_CODE_DUPLICATED,
                    Map.of("tenantCode", request.getTenantCode()));
        }
        Tenant tenant = Tenant.builder()
                .tenantCode(request.getTenantCode())
                .tenantName(request.getTenantName())
                .planCode(request.getPlanCode())
                .status(TenantStatus.ACTIVE.getCode())
                .expiresAt(request.getExpiresAt())
                .build();
        try {
            tenantRepository.insert(tenant);
        } catch (DuplicateKeyException e) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANT_CODE_DUPLICATED,
                    Map.of("tenantCode", request.getTenantCode()));
        }
        log.info("[TENANCY] 创建租户 tenantId={} code={}", tenant.getId(), tenant.getTenantCode());
        return toInfo(tenant);
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(LoginPrincipal principal, Long tenantId, Integer targetStatus) {
        tenantAccessPolicy.requirePlatform(principal);
        Tenant tenant = requireTenant(tenantId);
        TenantStatus target = TenantStatus.fromCode(targetStatus);
        if (target == null || !tenant.canTransitTo(targetStatus)) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_REQUEST_INVALID,
                    Map.of("status", String.valueOf(targetStatus)));
        }
        tenantRepository.updateStatus(tenantId, targetStatus);
        if (target == TenantStatus.FROZEN || target == TenantStatus.CLOSED) {
            AfterCommitExecutor.afterCommit(() -> membershipSessionRevoker.revokeTenant(tenantId));
        }
        log.info("[TENANCY] 租户状态变更 tenantId={} status={}", tenantId, targetStatus);
    }

    private Tenant requireTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));
    }

    private void validateStatus(Integer status) {
        if (status != null && TenantStatus.fromCode(status) == null) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_REQUEST_INVALID,
                    Map.of("status", String.valueOf(status)));
        }
    }

    private TenantInfo toInfo(Tenant tenant) {
        return TenantInfo.builder()
                .id(tenant.getId())
                .tenantCode(tenant.getTenantCode())
                .tenantName(tenant.getTenantName())
                .planCode(tenant.getPlanCode())
                .status(tenant.getStatus())
                .expiresAt(tenant.getExpiresAt())
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}
