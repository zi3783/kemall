package com.kemall.iam.auth.domain.repository;

import com.kemall.iam.auth.domain.model.LoginIdentity;

/**
 * 登录身份解析端口：登录时确定账号具备哪些身份（CUSTOMER/TENANT_MEMBER/PLATFORM_MEMBER）。
 */
public interface LoginIdentityRepository {

    LoginIdentity resolve(Long userId);
}
