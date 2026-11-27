package com.kemall.iam.api.dto.tenancy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 创建租户请求。
 */
@Data
public class CreateTenantRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "租户编码不能为空")
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]{2,63}$",
            message = "租户编码只能包含小写字母、数字和中划线，长度 3-64")
    private String tenantCode;

    @NotBlank(message = "租户名称不能为空")
    @Size(max = 128, message = "租户名称最长 128 个字符")
    private String tenantName;

    @Size(max = 64, message = "套餐编码最长 64 个字符")
    private String planCode;

    /** 到期时间，为空表示长期有效 */
    private LocalDateTime expiresAt;
}
