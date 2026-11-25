package com.kemall.iam.user.application.support;

import com.kemall.iam.api.enums.IdentityType;
import com.kemall.iam.user.domain.exception.IdentityFormatInvalidException;

import java.util.regex.Pattern;

/**
 * 身份值格式校验与脱敏工具。
 */
public final class IdentitySupport {

    private static final Pattern MOBILE = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private IdentitySupport() {}

    public static void validateFormat(IdentityType type, String value) {
        if (value == null || !matches(type, value)) {
            throw new IdentityFormatInvalidException();
        }
    }

    public static boolean matches(IdentityType type, String value) {
        if (type == null || value == null) return false;
        return switch (type) {
            case MOBILE -> MOBILE.matcher(value).matches();
            case EMAIL -> EMAIL.matcher(value).matches();
        };
    }

    /**
     * 归一化：手机号去空格；邮箱去空格并转小写。
     */
    public static String normalize(IdentityType type, String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return type == IdentityType.EMAIL ? trimmed.toLowerCase() : trimmed;
    }

    /**
     * 脱敏：138****8000 / a***@qq.com
     */
    public static String mask(IdentityType type, String value) {
        if (value == null) return null;
        if (type == IdentityType.MOBILE) {
            return value.length() >= 7
                    ? value.substring(0, 3) + "****" + value.substring(value.length() - 4)
                    : "****";
        }
        int at = value.indexOf('@');
        if (at <= 0) return "***";
        String local = value.substring(0, at);
        String domain = value.substring(at);
        String head = local.length() <= 1 ? local : local.substring(0, 1);
        return head + "***" + domain;
    }
}
