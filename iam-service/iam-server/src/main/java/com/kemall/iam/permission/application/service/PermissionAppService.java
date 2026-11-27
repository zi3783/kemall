package com.kemall.iam.permission.application.service;

import com.kemall.iam.api.dto.permission.CurrentPermissionInfo;
import com.kemall.iam.api.dto.permission.PermissionInfo;
import com.kemall.iam.api.dto.permission.RoleDetailInfo;
import com.kemall.iam.api.dto.permission.RoleInfo;
import com.kemall.iam.api.dto.permission.RolePermissionPatchRequest;
import com.kemall.iam.api.enums.RoleScopeType;
import com.kemall.iam.auth.application.model.LoginPrincipal;
import com.kemall.iam.permission.application.port.PermissionCache;
import com.kemall.iam.permission.application.support.PermissionScopeFactory;
import com.kemall.iam.permission.domain.exception.PermissionNotFoundException;
import com.kemall.iam.permission.domain.model.CurrentPermissions;
import com.kemall.iam.permission.domain.model.Permission;
import com.kemall.iam.permission.domain.model.PermissionScope;
import com.kemall.iam.permission.domain.model.Role;
import com.kemall.iam.permission.domain.repository.PermissionRepository;
import com.kemall.iam.permission.domain.repository.RoleRepository;
import com.kemall.iam.permission.domain.service.PermissionResolver;
import com.kemall.iam.permission.domain.service.RoleAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限应用服务：角色查询、角色-权限关联、当前权限、方法级鉴权数据源。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionAppService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleAccessService roleAccessService;
    private final PermissionResolver permissionResolver;
    private final PermissionCache permissionCache;

    @Value("${iam.permission.cache-ttl-seconds:300}")
    private long cacheTtlSeconds;

    /**
     * 当前工作空间可管理的角色列表（层级边界：租户空间能看到自己的租户+店铺角色）。
     */
    public List<RoleInfo> listRoles(LoginPrincipal principal) {
        PermissionScope scope = PermissionScopeFactory.from(principal);
        if (scope.isEmpty() || scope.workspaceType() == null) {
            return List.of();
        }
        return roleRepository.findManageableRoles(scope.workspaceType(), scope.tenantId())
                .stream()
                .map(this::toRoleInfo)
                .toList();
    }

    /**
     * 角色详情 + 已关联权限。
     */
    public RoleDetailInfo getRole(LoginPrincipal principal, Long roleId) {
        PermissionScope scope = PermissionScopeFactory.from(principal);
        Role role = roleAccessService.requireRole(roleId, scope);
        List<PermissionInfo> permissions = roleRepository.findPermissionsByRoleId(roleId)
                .stream()
                .map(this::toPermissionInfo)
                .toList();
        return RoleDetailInfo.builder()
                .role(toRoleInfo(role))
                .permissions(permissions)
                .build();
    }

    /**
     * 角色-权限增量关联（PATCH 语义）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void patchRolePermissions(LoginPrincipal principal,
                                     Long roleId,
                                     RolePermissionPatchRequest request) {
        PermissionScope scope = PermissionScopeFactory.from(principal);
        roleAccessService.requireRole(roleId, scope);

        List<String> addCodes = distinctCodes(request.getAddPermCodes());
        List<String> removeCodes = distinctCodes(request.getRemovePermCodes());

        Set<Long> existingIds = new HashSet<>(roleRepository.findPermissionIdsByRoleId(roleId));

        if (!addCodes.isEmpty()) {
            List<Permission> addPermissions = permissionRepository.findByCodes(addCodes);
            if (addPermissions.size() != addCodes.size()) {
                List<String> missing = new ArrayList<>(addCodes);
                missing.removeAll(addPermissions.stream().map(Permission::getPermCode).toList());
                throw new PermissionNotFoundException(missing);
            }
            List<Long> toAdd = addPermissions.stream()
                    .map(Permission::getId)
                    .filter(id -> !existingIds.contains(id))
                    .toList();
            roleRepository.addPermissions(roleId, toAdd);
        }

        if (!removeCodes.isEmpty()) {
            List<Long> toRemove = permissionRepository.findByCodes(removeCodes)
                    .stream()
                    .map(Permission::getId)
                    .filter(existingIds::contains)
                    .toList();
            roleRepository.removePermissions(roleId, toRemove);
        }

        permissionCache.invalidateAll();
        log.info("[PERMISSION] 角色权限更新 roleId={} add={} remove={}", roleId, addCodes, removeCodes);
    }

    /**
     * 当前用户在当前工作空间的有效权限。
     */
    public CurrentPermissionInfo currentPermissions(LoginPrincipal principal) {
        PermissionScope scope = PermissionScopeFactory.from(principal);
        return toCurrentPermissionInfo(scope, permissionResolver.resolve(scope));
    }

    /**
     * 刷新当前会话权限缓存并返回最新权限。
     */
    public CurrentPermissionInfo refreshCurrentPermissions(LoginPrincipal principal) {
        PermissionScope scope = PermissionScopeFactory.from(principal);
        permissionCache.evict(cacheKey(principal.getSessionId(), scope));
        CurrentPermissions current = permissionResolver.resolve(scope);
        permissionCache.put(cacheKey(principal.getSessionId(), scope), current.permCodes(), cacheTtlSeconds);
        return toCurrentPermissionInfo(scope, current);
    }

    /**
     * 方法级鉴权数据源：供 JWT 过滤器装载 Spring Security Authorities。
     */
    public Set<String> loadAuthorities(String sessionId, PermissionScope scope) {
        String key = cacheKey(sessionId, scope);
        return permissionCache.get(key).orElseGet(() -> {
            Set<String> codes = permissionResolver.resolve(scope).permCodes();
            permissionCache.put(key, codes, cacheTtlSeconds);
            return codes;
        });
    }

    /**
     * 权限字典。
     */
    public List<PermissionInfo> listPermissions() {
        return permissionRepository.findAll().stream().map(this::toPermissionInfo).toList();
    }

    // ==================== Utils ====================

    private String cacheKey(String sessionId, PermissionScope scope) {
        String workspace = scope.workspaceType() == null ? "NONE" : scope.workspaceType().name();
        return sessionId
                + ":" + workspace
                + ":" + nullSafe(scope.tenantId())
                + ":" + nullSafe(scope.memberId())
                + ":" + nullSafe(scope.platformMemberId());
    }

    private String nullSafe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private List<String> distinctCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        return codes.stream()
                .filter(code -> code != null && !code.isBlank())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private CurrentPermissionInfo toCurrentPermissionInfo(PermissionScope scope, CurrentPermissions current) {
        return CurrentPermissionInfo.builder()
                .workspaceType(scope.workspaceType())
                .roles(current.roles().stream().map(this::toRoleInfo).toList())
                .permissions(current.permissions().stream().map(this::toPermissionInfo).toList())
                .permCodes(new ArrayList<>(new LinkedHashSet<>(current.permCodes())))
                .build();
    }

    private RoleInfo toRoleInfo(Role role) {
        return RoleInfo.builder()
                .id(role.getId())
                .tenantId(role.getTenantId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .scopeType(RoleScopeType.fromCode(role.getScopeType()))
                .status(role.getStatus())
                .build();
    }

    private PermissionInfo toPermissionInfo(Permission permission) {
        return PermissionInfo.builder()
                .id(permission.getId())
                .permCode(permission.getPermCode())
                .permName(permission.getPermName())
                .permType(permission.getPermType())
                .build();
    }
}
