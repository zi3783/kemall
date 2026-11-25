package com.kemall.iam.api.dto.user;

import com.kemall.iam.api.enums.IdentityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IdentityInfo {

    private IdentityType identityType;

    /** 脱敏后的值，如 138****8000 / a***@qq.com */
    private String maskedValue;

    private LocalDateTime verifiedAt;
}
