package com.kemall.iam.platform.domain.port;

/**
 * 平台成员会话撤销端口：禁用平台成员后立即失效其平台空间会话。
 */
public interface PlatformSessionRevoker {

    void revoke(Long userId, Long platformMemberId);
}
