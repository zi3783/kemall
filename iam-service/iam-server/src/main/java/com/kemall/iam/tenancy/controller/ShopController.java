package com.kemall.iam.tenancy.controller;

import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.api.dto.common.PageResult;
import com.kemall.iam.api.dto.tenancy.CreateShopRequest;
import com.kemall.iam.api.dto.tenancy.ShopInfo;
import com.kemall.iam.api.dto.tenancy.UpdateShopStatusRequest;
import com.kemall.iam.auth.application.support.SecurityPrincipalHolder;
import com.kemall.iam.tenancy.application.service.ShopAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺管理接口。
 */
@RestController
@RequestMapping("/api/iam/v1/shops")
@RequiredArgsConstructor
@Tag(name = "店铺管理")
public class ShopController {

    private final ShopAppService shopAppService;

    @Operation(summary = "店铺分页查询")
    @GetMapping
    @PreAuthorize("hasAuthority('iam:shop:list')")
    public ApiResult<PageResult<ShopInfo>> page(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.success(shopAppService.page(
                SecurityPrincipalHolder.current(), tenantId, keyword, status, page, size));
    }

    @Operation(summary = "店铺详情")
    @GetMapping("/{shopId}")
    @PreAuthorize("hasAuthority('iam:shop:read')")
    public ApiResult<ShopInfo> get(@PathVariable Long shopId) {
        return ApiResult.success(shopAppService.get(SecurityPrincipalHolder.current(), shopId));
    }

    @Operation(summary = "创建店铺")
    @PostMapping
    @PreAuthorize("hasAuthority('iam:shop:create')")
    public ApiResult<ShopInfo> create(@Valid @RequestBody CreateShopRequest request) {
        return ApiResult.success(shopAppService.create(SecurityPrincipalHolder.current(), request));
    }

    @Operation(summary = "店铺状态变更")
    @PatchMapping("/{shopId}/status")
    @PreAuthorize("hasAuthority('iam:shop:status')")
    public ApiResult<Void> changeStatus(@PathVariable Long shopId,
                                        @Valid @RequestBody UpdateShopStatusRequest request) {
        shopAppService.changeStatus(
                SecurityPrincipalHolder.current(), shopId, request.getStatus());
        return ApiResult.success();
    }
}
