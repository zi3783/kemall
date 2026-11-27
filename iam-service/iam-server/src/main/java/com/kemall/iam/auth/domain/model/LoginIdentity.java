package com.kemall.iam.auth.domain.model;

/**
 * 登录身份解析结果：账号在消费者 / 平台 / 租户三个维度上的身份情况。
 */
public record LoginIdentity(boolean customer, boolean platformMember, long tenantMembershipCount) {

    /**
     * 是否需要选择工作空间：存在平台或租户身份时才需要。
     */
    public boolean workspaceRequired() {
        return platformMember || tenantMembershipCount > 0;
    }
}
