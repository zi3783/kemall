package com.kemall.iam.platform.application.service;

import com.kemall.iam.api.dto.common.PageResult;
import com.kemall.iam.api.dto.permission.RoleInfo;
import com.kemall.iam.api.dto.platform.CreatePlatformMemberRequest;
import com.kemall.iam.api.dto.platform.PlatformMemberInfo;
import com.kemall.iam.api.enums.RoleScopeType;
import com.kemall.iam.auth.application.model.LoginPrincipal;
import com.kemall.iam.permission.domain.repository.RoleAssignmentRepository;
import com.kemall.iam.platform.domain.exception.PlatformBusinessException;
import com.kemall.iam.platform.domain.exception.PlatformErrorCode;
import com.kemall.iam.platform.domain.exception.PlatformMemberNotFoundException;
import com.kemall.iam.platform.domain.model.PlatformMember;
import com.kemall.iam.platform.domain.model.PlatformMemberView;
import com.kemall.iam.platform.domain.port.PlatformSessionRevoker;
import com.kemall.iam.platform.domain.repository.PlatformMemberRepository;
import com.kemall.iam.platform.domain.service.PlatformAccessPolicy;
import com.kemall.iam.tenancy.application.support.AfterCommitExecutor;
import com.kemall.iam.tenancy.application.support.PageParams;
import com.kemall.iam.user.domain.model.UserAccount;
import com.kemall.iam.user.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 平台管理员应用服务：分页查询、详情、创建、禁用/启用。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformMemberAppService {

    private final PlatformMemberRepository platformMemberRepository;
    private final PlatformAccessPolicy platformAccessPolicy;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final UserAccountRepository userAccountRepository;
    private final PlatformSessionRevoker platformSessionRevoker;

    public PageResult<PlatformMemberInfo> page(LoginPrincipal principal, String keyword,
                                               Integer status, int page, int size) {
        platformAccessPolicy.requirePlatformWorkspace(principal);
        validateStatus(status);
        PageParams params = PageParams.of(page, size);
        List<PlatformMemberInfo> records = platformMemberRepository
                .findPage(keyword, status, params.offset(), params.size())
                .stream()
                .map(this::toInfo)
                .toList();
        long total = platformMemberRepository.count(keyword, status);
        return PageResult.of(records, total, params.page(), params.size());
    }

    public PlatformMemberInfo get(LoginPrincipal principal, Long platformMemberId) {
        platformAccessPolicy.requirePlatformWorkspace(principal);
        PlatformMember member = requireMember(platformMemberId);
        List<RoleInfo> roles = roleAssignmentRepository.findPlatformRoles(platformMemberId)
                .stream()
                .map(role -> RoleInfo.builder()
                        .id(role.getId())
                        .tenantId(role.getTenantId())
                        .roleCode(role.getRoleCode())
                        .roleName(role.getRoleName())
                        .scopeType(RoleScopeType.fromCode(role.getScopeType()))
                        .status(role.getStatus())
                        .build())
                .toList();
        return PlatformMemberInfo.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .roles(roles)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public PlatformMemberInfo create(LoginPrincipal principal, CreatePlatformMemberRequest request) {
        platformAccessPolicy.requirePlatformWorkspace(principal);

        UserAccount user = resolveUser(request);
        if (user == null || !user.canLogin()) {
            throw new PlatformBusinessException(PlatformErrorCode.PLATFORM_USER_UNAVAILABLE,
                    Map.of("userId", String.valueOf(request.getUserId())));
        }
        if (platformMemberRepository.findByUserId(user.getId()).isPresent()) {
            throw new PlatformBusinessException(PlatformErrorCode.PLATFORM_MEMBER_ALREADY_EXISTS,
                    Map.of("userId", user.getId()));
        }

        PlatformMember member = PlatformMember.builder()
                .userId(user.getId())
                .status(1)
                .build();
        try {
            platformMemberRepository.insert(member);
        } catch (DuplicateKeyException e) {
            throw new PlatformBusinessException(PlatformErrorCode.PLATFORM_MEMBER_ALREADY_EXISTS,
                    Map.of("userId", user.getId()));
        }
        log.info("[PLATFORM] 创建平台成员 platformMemberId={} userId={}", member.getId(), user.getId());

        return PlatformMemberInfo.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(LoginPrincipal principal, Long platformMemberId, Integer targetStatus) {
        platformAccessPolicy.requirePlatformWorkspace(principal);
        PlatformMember member = requireMember(platformMemberId);
        if (targetStatus == null || (targetStatus != 1 && targetStatus != 2)) {
            throw new PlatformBusinessException(PlatformErrorCode.PLATFORM_REQUEST_INVALID,
                    Map.of("status", String.valueOf(targetStatus)));
        }
        platformMemberRepository.updateStatus(platformMemberId, targetStatus);
        if (targetStatus == 2) {
            AfterCommitExecutor.afterCommit(() ->
                    platformSessionRevoker.revoke(member.getUserId(), platformMemberId));
        }
        log.info("[PLATFORM] 平台成员状态变更 platformMemberId={} status={}",
                platformMemberId, targetStatus);
    }

    private PlatformMember requireMember(Long platformMemberId) {
        return platformMemberRepository.findById(platformMemberId)
                .orElseThrow(() -> new PlatformMemberNotFoundException(platformMemberId));
    }

    private UserAccount resolveUser(CreatePlatformMemberRequest request) {
        if (request.getUserId() != null) {
            return userAccountRepository.findById(request.getUserId());
        }
        return userAccountRepository.findByIdentity(
                request.getIdentityType(), request.getIdentityValue());
    }

    private void validateStatus(Integer status) {
        if (status != null && status != 1 && status != 2) {
            throw new PlatformBusinessException(PlatformErrorCode.PLATFORM_REQUEST_INVALID,
                    Map.of("status", String.valueOf(status)));
        }
    }

    private PlatformMemberInfo toInfo(PlatformMemberView view) {
        PlatformMember member = view.member();
        return PlatformMemberInfo.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .username(view.username())
                .nickname(view.nickname())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
