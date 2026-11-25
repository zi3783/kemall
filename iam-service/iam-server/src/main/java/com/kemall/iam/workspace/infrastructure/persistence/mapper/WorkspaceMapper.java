package com.kemall.iam.workspace.infrastructure.persistence.mapper;

import com.kemall.iam.workspace.domain.model.Workspace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作空间查询 Mapper（查询模型见 {@link Workspace}）。
 */
@Mapper
public interface WorkspaceMapper {

    List<Workspace> selectAvailableWorkspaces(@Param("userId") Long userId);

    Workspace selectTenantWorkspace(@Param("userId") Long userId,
                                    @Param("tenantId") Long tenantId);

    Workspace selectShopWorkspace(@Param("userId") Long userId,
                                  @Param("tenantId") Long tenantId,
                                  @Param("shopId") Long shopId);

    Workspace selectPlatformWorkspace(@Param("userId") Long userId);
}
