package com.kemall.common.utils;

public class UserContext {
    private UserContext(){}

    private final static ThreadLocal<Long> userId = new ThreadLocal<>();
    public static Long getUserId() {
        return userId.get();
    }
    public static void setUserId(Long userId) {
        UserContext.userId.set(userId);
    }

    public static void removeUserId() {
        UserContext.userId.remove();
    }
}
