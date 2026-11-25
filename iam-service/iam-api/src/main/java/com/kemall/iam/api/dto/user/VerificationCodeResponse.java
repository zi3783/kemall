package com.kemall.iam.api.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerificationCodeResponse {

    /** 是否已发送（防枚举场景也返回成功但 sent=false） */
    private boolean sent;

    /** 调试用验证码（仅 iam.verification.expose-code=true 时返回） */
    private String debugCode;

    /** 有效期（秒） */
    private long expireSeconds;
}
