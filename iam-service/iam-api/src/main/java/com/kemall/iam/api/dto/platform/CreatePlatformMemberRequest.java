package com.kemall.iam.api.dto.platform;

import com.kemall.iam.api.enums.IdentityType;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建平台管理员请求：把已有全局账号提拔为平台成员。
 * <p>
 * 用户定位方式二选一：userId 或 identityType + identityValue（手机号/邮箱）。
 */
@Data
public class CreatePlatformMemberRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private IdentityType identityType;

    private String identityValue;

    @AssertTrue(message = "userId 与 identityType/identityValue 必须二选一")
    public boolean isUserReferenceValid() {
        boolean byUserId = userId != null;
        boolean byIdentity = identityType != null && identityValue != null && !identityValue.isBlank();
        return byUserId ^ byIdentity;
    }
}
