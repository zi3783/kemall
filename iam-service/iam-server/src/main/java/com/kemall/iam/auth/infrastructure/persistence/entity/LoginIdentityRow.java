package com.kemall.iam.auth.infrastructure.persistence.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录身份统计查询行。
 */
@Data
public class LoginIdentityRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerCount;

    private Long platformCount;

    private Long tenantCount;
}
