package com.kemall.iam.permission.infrastructure.persistence.mapper;

import com.kemall.iam.permission.domain.model.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 成员-角色分配查询 Mapper。
 */
@Mapper
public interface RoleAssignmentQueryMapper {

    List<Role> selectMemberRoles(@Param("tenantId") Long tenantId,
                                 @Param("memberId") Long memberId,
                                 @Param("scopeType") Integer scopeType);

    List<Role> selectPlatformRoles(@Param("platformMemberId") Long platformMemberId);

    Long countActiveTenantMember(@Param("tenantId") Long tenantId,
                                 @Param("memberId") Long memberId);

    Long countActivePlatformMember(@Param("platformMemberId") Long platformMemberId);
}
