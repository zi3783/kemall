package com.kemall.iam.tenancy.application.support;

/**
 * 分页参数归一化：page 最小 1，size 1-200，默认 20。
 */
public record PageParams(int page, int size, long offset) {

    public static PageParams of(int page, int size) {
        int normalizedPage = page < 1 ? 1 : page;
        int normalizedSize = size < 1 ? 20 : Math.min(size, 200);
        long offset = (long) (normalizedPage - 1) * normalizedSize;
        return new PageParams(normalizedPage, normalizedSize, offset);
    }
}
