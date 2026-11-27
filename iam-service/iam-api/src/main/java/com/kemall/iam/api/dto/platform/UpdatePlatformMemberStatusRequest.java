package com.kemall.iam.api.dto.platform;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 平台成员状态变更请求。
 */
@Data
public class UpdatePlatformMemberStatusRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 目标状态：1 active / 2 disabled */
    @NotNull(message = "目标状态不能为空")
    private Integer status;
}
