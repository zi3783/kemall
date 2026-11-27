package com.kemall.iam.tenancy.domain.model;

/**
 * 成员列表视图：成员 + 账号展示信息。
 */
public record TenantMemberView(TenantMember member, String username, String nickname) {
}
