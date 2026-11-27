package com.kemall.iam.tenancy.application.service;

import com.kemall.iam.api.dto.common.PageResult;
import com.kemall.iam.api.dto.tenancy.AddTenantMemberRequest;
import com.kemall.iam.api.dto.tenancy.TenantMemberInfo;
import com.kemall.iam.api.enums.MemberType;
import com.kemall.iam.auth.application.model.LoginPrincipal;
import com.kemall.iam.tenancy.application.support.AfterCommitExecutor;
import com.kemall.iam.tenancy.application.support.PageParams;
import com.kemall.iam.tenancy.domain.exception.ShopNotFoundException;
import com.kemall.iam.tenancy.domain.exception.TenancyBusinessException;
import com.kemall.iam.tenancy.domain.exception.TenancyErrorCode;
import com.kemall.iam.tenancy.domain.model.Shop;
import com.kemall.iam.tenancy.domain.model.TenantMember;
import com.kemall.iam.tenancy.domain.model.TenantMemberView;
import com.kemall.iam.tenancy.domain.port.MembershipAuthorizationCleaner;
import com.kemall.iam.tenancy.domain.port.MembershipSessionRevoker;
import com.kemall.iam.tenancy.domain.repository.ShopRepository;
import com.kemall.iam.tenancy.domain.repository.TenantMemberRepository;
import com.kemall.iam.tenancy.domain.service.MembershipBelongingService;
import com.kemall.iam.tenancy.domain.service.TenantAccessPolicy;
import com.kemall.iam.user.domain.model.UserAccount;
import com.kemall.iam.user.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 租户/店铺成员应用服务：分页查询、加入、禁用/启用、移除。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantMemberAppService {

    private final TenantMemberRepository tenantMemberRepository;
    private final ShopRepository shopRepository;
    private final TenantAccessPolicy tenantAccessPolicy;
    private final MembershipBelongingService membershipBelongingService;
    private final MembershipSessionRevoker membershipSessionRevoker;
    private final MembershipAuthorizationCleaner membershipAuthorizationCleaner;
    private final UserAccountRepository userAccountRepository;

    public PageResult<TenantMemberInfo> page(LoginPrincipal principal, Long tenantIdParam,
                                             Long shopIdParam, Integer memberType,
                                             Integer status, int page, int size) {
        Long tenantId;
        Long shopId;
        if (tenantAccessPolicy.isPlatform(principal)) {
            tenantId = tenantAccessPolicy.resolveTenantId(principal, tenantIdParam);
            shopId = shopIdParam;
        } else if (tenantAccessPolicy.isShopWorkspace(principal)) {
            tenantId = principal.getTenantId();
            Long ownShopId = principal.getShopId();
            if (tenantId == null) {
                throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED);
            }
            if (shopIdParam != null && !Objects.equals(shopIdParam, ownShopId)) {
                throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED);
            }
            shopId = ownShopId;
        } else {
            tenantId = tenantAccessPolicy.resolveTenantId(principal, tenantIdParam);
            shopId = shopIdParam;
        }

        if (shopId != null && shopId > 0) {
            requireShopOfTenant(tenantId, shopId);
        }
        validateMemberType(memberType);
        validateMemberStatus(status);

        PageParams params = PageParams.of(page, size);
        List<TenantMemberInfo> records = tenantMemberRepository
                .findPage(tenantId, shopId, memberType, status, params.offset(), params.size())
                .stream()
                .map(this::toInfo)
                .toList();
        long total = tenantMemberRepository.count(tenantId, shopId, memberType, status);
        return PageResult.of(records, total, params.page(), params.size());
    }

    @Transactional(rollbackFor = Exception.class)
    public Long add(LoginPrincipal principal, AddTenantMemberRequest request) {
        if (tenantAccessPolicy.isShopWorkspace(principal)) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED);
        }
        Long tenantId = tenantAccessPolicy.resolveTenantId(principal, request.getTenantId());
        membershipBelongingService.requireActiveTenant(tenantId);

        Long shopId = request.getShopId() == null ? 0L : request.getShopId();
        if (shopId > 0) {
            membershipBelongingService.requireTenantShop(tenantId, shopId);
        }
        MemberType memberType = MemberType.fromCode(request.getMemberType());
        if (memberType == null) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_REQUEST_INVALID,
                    Map.of("field", "memberType"));
        }

        UserAccount user = resolveUser(request);
        if (user == null || !user.canLogin()) {
            throw new TenancyBusinessException(TenancyErrorCode.USER_ACCOUNT_UNAVAILABLE,
                    Map.of("userId", String.valueOf(request.getUserId())));
        }

        Optional<TenantMember> existing =
                tenantMemberRepository.findByTenantShopUser(tenantId, shopId, user.getId());
        if (existing.isPresent()) {
            TenantMember member = existing.get();
            if (!member.isRemoved()) {
                throw new TenancyBusinessException(TenancyErrorCode.MEMBER_ALREADY_EXISTS,
                        Map.of("memberId", member.getId()));
            }
            tenantMemberRepository.restore(member.getId(), memberType.getCode(), LocalDateTime.now());
            log.info("[TENANCY] 成员重新加入 tenantId={} memberId={}", tenantId, member.getId());
            return member.getId();
        }

        TenantMember member = TenantMember.builder()
                .tenantId(tenantId)
                .shopId(shopId)
                .userId(user.getId())
                .memberType(memberType.getCode())
                .status(1)
                .joinedAt(LocalDateTime.now())
                .build();
        try {
            return tenantMemberRepository.insert(member);
        } catch (DuplicateKeyException e) {
            throw new TenancyBusinessException(TenancyErrorCode.MEMBER_ALREADY_EXISTS,
                    Map.of("tenantId", tenantId, "shopId", shopId, "userId", user.getId()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(LoginPrincipal principal, Long memberId,
                             Long tenantIdParam, Integer targetStatus) {
        Long tenantId = resolveTenantForWrite(principal, tenantIdParam);
        TenantMember member = membershipBelongingService.requireMemberOfTenant(tenantId, memberId);
        if (member.isRemoved()) {
            throw new TenancyBusinessException(TenancyErrorCode.MEMBER_UNAVAILABLE,
                    Map.of("memberId", memberId));
        }
        if (targetStatus == null || (targetStatus != 1 && targetStatus != 2)) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_REQUEST_INVALID,
                    Map.of("status", String.valueOf(targetStatus)));
        }
        tenantMemberRepository.updateStatus(memberId, targetStatus);
        if (targetStatus == 2) {
            AfterCommitExecutor.afterCommit(() ->
                    membershipSessionRevoker.revokeMember(member.getUserId(), memberId));
        }
        log.info("[TENANCY] 成员状态变更 memberId={} status={}", memberId, targetStatus);
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(LoginPrincipal principal, Long memberId, Long tenantIdParam) {
        Long tenantId = resolveTenantForWrite(principal, tenantIdParam);
        TenantMember member = membershipBelongingService.requireMemberOfTenant(tenantId, memberId);
        if (member.isRemoved()) {
            return;
        }
        tenantMemberRepository.markRemoved(memberId, LocalDateTime.now());
        membershipAuthorizationCleaner.clearMemberRoles(tenantId, memberId);
        AfterCommitExecutor.afterCommit(() ->
                membershipSessionRevoker.revokeMember(member.getUserId(), memberId));
        log.info("[TENANCY] 成员移除 tenantId={} memberId={}", tenantId, memberId);
    }

    // ==================== Utils ====================

    private Long resolveTenantForWrite(LoginPrincipal principal, Long tenantIdParam) {
        if (tenantAccessPolicy.isShopWorkspace(principal)) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED);
        }
        return tenantAccessPolicy.resolveTenantId(principal, tenantIdParam);
    }

    private Shop requireShopOfTenant(Long tenantId, Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException(shopId));
        if (!shop.belongsTo(tenantId)) {
            throw new TenancyBusinessException(TenancyErrorCode.SHOP_NOT_IN_TENANT,
                    Map.of("tenantId", tenantId, "shopId", shopId));
        }
        return shop;
    }

    private UserAccount resolveUser(AddTenantMemberRequest request) {
        if (request.getUserId() != null) {
            return userAccountRepository.findById(request.getUserId());
        }
        return userAccountRepository.findByIdentity(
                request.getIdentityType(), request.getIdentityValue());
    }

    private void validateMemberType(Integer memberType) {
        if (memberType != null && MemberType.fromCode(memberType) == null) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_REQUEST_INVALID,
                    Map.of("field", "memberType"));
        }
    }

    private void validateMemberStatus(Integer status) {
        if (status != null && status != 1 && status != 2) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_REQUEST_INVALID,
                    Map.of("field", "status"));
        }
    }

    private TenantMemberInfo toInfo(TenantMemberView view) {
        TenantMember member = view.member();
        return TenantMemberInfo.builder()
                .id(member.getId())
                .tenantId(member.getTenantId())
                .shopId(member.getShopId())
                .userId(member.getUserId())
                .username(view.username())
                .nickname(view.nickname())
                .memberType(MemberType.fromCode(member.getMemberType()))
                .status(member.getStatus())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
