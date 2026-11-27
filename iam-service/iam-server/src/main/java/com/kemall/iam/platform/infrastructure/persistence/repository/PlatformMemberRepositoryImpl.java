package com.kemall.iam.platform.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.kemall.iam.platform.domain.model.PlatformMember;
import com.kemall.iam.platform.domain.model.PlatformMemberView;
import com.kemall.iam.platform.domain.repository.PlatformMemberRepository;
import com.kemall.iam.platform.infrastructure.persistence.entity.PlatformMemberPO;
import com.kemall.iam.platform.infrastructure.persistence.entity.PlatformMemberRow;
import com.kemall.iam.platform.infrastructure.persistence.mapper.PlatformMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 平台成员仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class PlatformMemberRepositoryImpl implements PlatformMemberRepository {

    private final PlatformMemberMapper platformMemberMapper;

    @Override
    public Optional<PlatformMember> findById(Long platformMemberId) {
        return Optional.ofNullable(platformMemberMapper.selectById(platformMemberId))
                .map(this::toDomain);
    }

    @Override
    public Optional<PlatformMember> findByUserId(Long userId) {
        PlatformMemberPO po = platformMemberMapper.selectOne(Wrappers.<PlatformMemberPO>lambdaQuery()
                .eq(PlatformMemberPO::getUserId, userId)
                .last("LIMIT 1"));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<PlatformMemberView> findPage(String keyword, Integer status, long offset, int size) {
        return platformMemberMapper.selectPageList(keyword, status, offset, size)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public long count(String keyword, Integer status) {
        return platformMemberMapper.selectPageCount(keyword, status);
    }

    @Override
    public Long insert(PlatformMember member) {
        LocalDateTime now = LocalDateTime.now();
        PlatformMemberPO po = new PlatformMemberPO()
                .setId(member.getId() == null ? IdWorker.getId() : member.getId())
                .setUserId(member.getUserId())
                .setStatus(member.getStatus())
                .setCreatedAt(now)
                .setUpdatedAt(now);
        platformMemberMapper.insert(po);
        member.setId(po.getId());
        member.setCreatedAt(now);
        return po.getId();
    }

    @Override
    public void updateStatus(Long platformMemberId, Integer status) {
        platformMemberMapper.update(null, Wrappers.<PlatformMemberPO>lambdaUpdate()
                .eq(PlatformMemberPO::getId, platformMemberId)
                .set(PlatformMemberPO::getStatus, status)
                .set(PlatformMemberPO::getUpdatedAt, LocalDateTime.now()));
    }

    private PlatformMember toDomain(PlatformMemberPO po) {
        if (po == null) {
            return null;
        }
        return PlatformMember.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .status(po.getStatus())
                .createdAt(po.getCreatedAt())
                .build();
    }

    private PlatformMemberView toView(PlatformMemberRow row) {
        PlatformMember member = PlatformMember.builder()
                .id(row.getId())
                .userId(row.getUserId())
                .status(row.getStatus())
                .createdAt(row.getCreatedAt())
                .build();
        return new PlatformMemberView(member, row.getUsername(), row.getNickname());
    }
}
