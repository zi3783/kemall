package com.kemall.iam.auth.infrastructure.persistence.mapper;

import com.kemall.iam.api.dto.auth.WorkspaceInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkspaceMapper {

    List<WorkspaceInfo> selectAvailableWorkspaces(
        @Param("userId") Long userId
    );
}