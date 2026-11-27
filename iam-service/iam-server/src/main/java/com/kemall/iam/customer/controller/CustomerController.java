package com.kemall.iam.customer.controller;

import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.api.dto.customer.CustomerProfileInfo;
import com.kemall.iam.api.dto.customer.CustomerRegisterResponse;
import com.kemall.iam.api.dto.user.IdentityInfo;
import com.kemall.iam.api.dto.user.RegisterRequest;
import com.kemall.iam.api.dto.user.UpdateProfileRequest;
import com.kemall.iam.auth.application.support.SecurityPrincipalHolder;
import com.kemall.iam.customer.application.service.CustomerAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 消费者接口。
 * <p>
 * 除注册外只需登录，不要求选择工作空间；
 * 手机号/邮箱的绑定与解绑复用 /api/iam/v1/user/identity 系列接口。
 */
@RestController
@RequestMapping("/api/iam/v1/customer")
@RequiredArgsConstructor
@Tag(name = "消费者")
public class CustomerController {

    private final CustomerAppService customerAppService;

    @Operation(summary = "消费者注册（白名单）")
    @PostMapping("/register")
    public ApiResult<CustomerRegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResult.success(customerAppService.register(request));
    }

    @Operation(summary = "消费者个人资料")
    @GetMapping("/profile")
    public ApiResult<CustomerProfileInfo> profile() {
        return ApiResult.success(
                customerAppService.getProfile(SecurityPrincipalHolder.currentUserId()));
    }

    @Operation(summary = "修改消费者昵称/头像")
    @PutMapping("/profile")
    public ApiResult<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        customerAppService.updateProfile(SecurityPrincipalHolder.currentUserId(), request);
        return ApiResult.success();
    }

    @Operation(summary = "消费者已绑定身份")
    @GetMapping("/identities")
    public ApiResult<List<IdentityInfo>> identities() {
        return ApiResult.success(
                customerAppService.listIdentities(SecurityPrincipalHolder.currentUserId()));
    }
}
