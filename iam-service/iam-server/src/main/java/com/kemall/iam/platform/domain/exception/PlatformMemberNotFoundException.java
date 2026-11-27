package com.kemall.iam.platform.domain.exception;

import java.util.Map;

/**
 * 平台成员不存在。
 */
public class PlatformMemberNotFoundException extends PlatformDomainException {

    private static final long serialVersionUID = 1L;

    public PlatformMemberNotFoundException(Long platformMemberId) {
        super(PlatformErrorCode.PLATFORM_MEMBER_NOT_FOUND,
                Map.of("platformMemberId", String.valueOf(platformMemberId)));
    }
}
