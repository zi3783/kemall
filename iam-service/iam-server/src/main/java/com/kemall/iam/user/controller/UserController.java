package com.kemall.iam.user.controller;

import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.api.dto.user.BindIdentityRequest;
import com.kemall.iam.api.dto.user.CurrentUserInfo;
import com.kemall.iam.api.dto.user.IdentityInfo;
import com.kemall.iam.api.dto.user.PublicUserProfile;
import com.kemall.iam.api.dto.user.UnbindIdentityRequest;
import com.kemall.iam.api.dto.user.UpdateProfileRequest;
import com.kemall.iam.api.dto.user.UserProfileInfo;
import com.kemall.iam.api.enums.IdentityType;
import com.kemall.iam.auth.application.support.SecurityPrincipalHolder;
import com.kemall.iam.user.application.service.UserProfileAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户资料控制器（需登录）。
 */
@RestController
@RequestMapping("/api/iam/v1/user")
@RequiredArgsConstructor
@Tag(name = "用户资料")
public class UserController {

    private final UserProfileAppService userProfileAppService;

    @Operation(summary = "当前登录用户信息")
    @GetMapping("/me")
    public ApiResult<CurrentUserInfo> currentUser() {
        return ApiResult.success(userProfileAppService.getCurrentUser(SecurityPrincipalHolder.currentUserId()));
    }

    @Operation(summary = "本人资料")
    @GetMapping("/profile")
    public ApiResult<UserProfileInfo> myProfile() {
        return ApiResult.success(userProfileAppService.getMyProfile(SecurityPrincipalHolder.currentUserId()));
    }

    @Operation(summary = "他人公开资料")
    @GetMapping("/profile/{userId}")
    public ApiResult<PublicUserProfile> publicProfile(@PathVariable Long userId) {
        return ApiResult.success(userProfileAppService.getPublicProfile(userId));
    }

    @Operation(summary = "修改昵称/头像")
    @PutMapping("/profile")
    public ApiResult<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        userProfileAppService.updateProfile(SecurityPrincipalHolder.currentUserId(), request);
        return ApiResult.success();
    }

    @Operation(summary = "绑定手机号/邮箱")
    @PostMapping("/identity/bind")
    public ApiResult<IdentityInfo> bindIdentity(@Valid @RequestBody BindIdentityRequest request) {
        return ApiResult.success(userProfileAppService.bindIdentity(
                SecurityPrincipalHolder.currentUserId(), request));
    }

    @Operation(summary = "解绑手机号/邮箱")
    @DeleteMapping("/identity/{identityType}")
    public ApiResult<Void> unbindIdentity(@PathVariable IdentityType identityType,
                                          @Valid @RequestBody UnbindIdentityRequest request) {
        userProfileAppService.unbindIdentity(
                SecurityPrincipalHolder.currentUserId(), identityType, request);
        return ApiResult.success();
    }
}
