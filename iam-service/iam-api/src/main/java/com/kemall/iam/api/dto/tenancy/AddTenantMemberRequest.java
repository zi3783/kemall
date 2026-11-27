package com.kemall.iam.api.dto.tenancy;

import com.kemall.iam.api.enums.IdentityType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 成员加入请求。
 * <p>
 * 用户定位方式二选一：userId 或 identityType + identityValue（手机号/邮箱）。
 */
@Data
public class AddTenantMemberRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 平台空间必填；租户空间忽略，强制取会话租户 */
    private Long tenantId;

    /** 0 或不传表示加入租户级；大于 0 表示加入指定店铺 */
    private Long shopId;

    private Long userId;

    private IdentityType identityType;

    private String identityValue;

    @NotNull(message = "成员类型不能为空")
    private Integer memberType;

    @AssertTrue(message = "userId 与 identityType/identityValue 必须二选一")
    public boolean isUserReferenceValid() {
        boolean byUserId = userId != null;
        boolean byIdentity = identityType != null && identityValue != null && !identityValue.isBlank();
        return byUserId ^ byIdentity;
    }
}
