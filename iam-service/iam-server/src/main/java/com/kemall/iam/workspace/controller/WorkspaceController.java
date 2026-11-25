package com.kemall.iam.workspace.controller;

import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.api.dto.workspace.SwitchWorkspaceRequest;
import com.kemall.iam.api.dto.workspace.SwitchWorkspaceResponse;
import com.kemall.iam.api.dto.workspace.WorkspaceInfo;
import com.kemall.iam.auth.application.support.SecurityPrincipalHolder;
import com.kemall.iam.workspace.application.service.WorkspaceAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工作空间中心（需登录）。
 */
@RestController
@RequestMapping("/api/iam/v1/workspaces")
@RequiredArgsConstructor
@Tag(name = "工作空间")
public class WorkspaceController {

    private final WorkspaceAppService workspaceAppService;

    @Operation(summary = "我可见的工作空间列表")
    @GetMapping
    public ApiResult<List<WorkspaceInfo>> listWorkspaces() {
        return ApiResult.success(workspaceAppService.listWorkspaces(
                SecurityPrincipalHolder.currentUserId()));
    }

    @Operation(summary = "当前工作空间")
    @GetMapping("/current")
    public ApiResult<WorkspaceInfo> currentWorkspace() {
        return ApiResult.success(workspaceAppService.currentWorkspace(
                SecurityPrincipalHolder.currentUserId(),
                SecurityPrincipalHolder.currentSessionId()));
    }

    @Operation(summary = "切换工作空间（重新签发 Token）")
    @PostMapping("/switch")
    public ApiResult<SwitchWorkspaceResponse> switchWorkspace(
            @Valid @RequestBody SwitchWorkspaceRequest request) {
        return ApiResult.success(workspaceAppService.switchWorkspace(
                SecurityPrincipalHolder.currentUserId(),
                SecurityPrincipalHolder.currentSessionId(),
                request));
    }
}
