package com.kemall.iam.platform.domain.service;

import com.kemall.iam.auth.application.model.LoginPrincipal;
import com.kemall.iam.platform.domain.exception.PlatformBusinessException;
import com.kemall.iam.platform.domain.exception.PlatformErrorCode;
import org.springframework.stereotype.Component;

/**
 * 平台管理访问策略：所有平台管理操作必须处于 PLATFORM 工作空间。
 * <p>
 * 与 Controller 上的 @PreAuthorize 权限码形成双保险。
 */
@Component
public class PlatformAccessPolicy {

    public void requirePlatformWorkspace(LoginPrincipal principal) {
        if (principal.getPlatformMemberId() == null) {
            throw new PlatformBusinessException(PlatformErrorCode.PLATFORM_ACCESS_DENIED);
        }
    }
}
