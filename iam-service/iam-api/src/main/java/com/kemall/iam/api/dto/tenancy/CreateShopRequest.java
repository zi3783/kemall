package com.kemall.iam.api.dto.tenancy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建店铺请求。
 */
@Data
public class CreateShopRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 平台空间创建时必须指定；租户空间忽略该值，强制取会话租户 */
    private Long tenantId;

    @NotBlank(message = "店铺编码不能为空")
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,63}$",
            message = "店铺编码只能包含小写字母、数字和中划线，长度 2-64")
    private String shopCode;

    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 128, message = "店铺名称最长 128 个字符")
    private String shopName;

    @Size(max = 512, message = "Logo 地址最长 512 个字符")
    private String logoUrl;

    @Size(max = 64, message = "时区最长 64 个字符")
    private String timezone;
}
