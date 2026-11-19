package com.kemall.iam.auth.controller;

import com.kemall.iam.api.dto.auth.LoginRequest;
import com.kemall.iam.api.dto.auth.LoginResponse;
import com.kemall.iam.auth.application.service.AuthAppService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iam/v1/auth")
@RequiredArgsConstructor
@Tag(name = "用户登录")
public class AuthController {

    private final AuthAppService authAppService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authAppService.login(request);
    }
}