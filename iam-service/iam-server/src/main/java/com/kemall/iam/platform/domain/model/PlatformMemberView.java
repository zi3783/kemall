package com.kemall.iam.platform.domain.model;

/**
 * 平台成员列表视图：成员 + 账号展示信息。
 */
public record PlatformMemberView(PlatformMember member, String username, String nickname) {
}
