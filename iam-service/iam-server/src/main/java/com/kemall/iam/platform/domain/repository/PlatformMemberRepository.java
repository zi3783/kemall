package com.kemall.iam.platform.domain.repository;

import com.kemall.iam.platform.domain.model.PlatformMember;
import com.kemall.iam.platform.domain.model.PlatformMemberView;

import java.util.List;
import java.util.Optional;

/**
 * 平台成员仓储端口。
 */
public interface PlatformMemberRepository {

    Optional<PlatformMember> findById(Long platformMemberId);

    Optional<PlatformMember> findByUserId(Long userId);

    List<PlatformMemberView> findPage(String keyword, Integer status, long offset, int size);

    long count(String keyword, Integer status);

    Long insert(PlatformMember member);

    void updateStatus(Long platformMemberId, Integer status);
}
