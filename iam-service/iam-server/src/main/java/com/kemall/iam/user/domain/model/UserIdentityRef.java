package com.kemall.iam.user.domain.model;

import com.kemall.iam.api.enums.IdentityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录身份值对象（对应 user_identity）。
 */
@Data
@Builder
public class UserIdentityRef {

    private IdentityType identityType;

    private String identityValue;

    private LocalDateTime verifiedAt;

    public boolean isVerified() {
        return verifiedAt != null;
    }
}
