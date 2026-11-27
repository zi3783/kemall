package com.kemall.iam.tenancy.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.kemall.iam.tenancy.domain.model.TenantMember;
import com.kemall.iam.tenancy.domain.model.TenantMemberView;
import com.kemall.iam.tenancy.domain.repository.TenantMemberRepository;
import com.kemall.iam.tenancy.infrastructure.persistence.entity.TenantMemberPO;
import com.kemall.iam.tenancy.infrastructure.persistence.entity.TenantMemberRow;
import com.kemall.iam.tenancy.infrastructure.persistence.mapper.TenantMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 租户/店铺成员仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class TenantMemberRepositoryImpl implements TenantMemberRepository {

    private final TenantMemberMapper tenantMemberMapper;

    @Override
    public Optional<TenantMember> findById(Long memberId) {
        return Optional.ofNullable(tenantMemberMapper.selectById(memberId)).map(this::toDomain);
    }

    @Override
    public Optional<TenantMember> findByTenantShopUser(Long tenantId, Long shopId, Long userId) {
        TenantMemberPO po = tenantMemberMapper.selectOne(Wrappers.<TenantMemberPO>lambdaQuery()
                .eq(TenantMemberPO::getTenantId, tenantId)
                .eq(TenantMemberPO::getShopId, shopId)
                .eq(TenantMemberPO::getUserId, userId)
                .last("LIMIT 1"));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<TenantMemberView> findPage(Long tenantId, Long shopId, Integer memberType,
                                           Integer status, long offset, int size) {
        return tenantMemberMapper.selectPageList(tenantId, shopId, memberType, status, offset, size)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public long count(Long tenantId, Long shopId, Integer memberType, Integer status) {
        return tenantMemberMapper.selectPageCount(tenantId, shopId, memberType, status);
    }

    @Override
    public Long insert(TenantMember member) {
        LocalDateTime joinedAt = member.getJoinedAt() == null ? LocalDateTime.now() : member.getJoinedAt();
        TenantMemberPO po = new TenantMemberPO()
                .setId(member.getId() == null ? IdWorker.getId() : member.getId())
                .setTenantId(member.getTenantId())
                .setShopId(member.getShopId() == null ? 0L : member.getShopId())
                .setUserId(member.getUserId())
                .setMemberType(member.getMemberType())
                .setStatus(member.getStatus())
                .setJoinedAt(joinedAt);
        tenantMemberMapper.insert(po);
        member.setId(po.getId());
        member.setJoinedAt(joinedAt);
        return po.getId();
    }

    @Override
    public void updateStatus(Long memberId, Integer status) {
        tenantMemberMapper.update(null, Wrappers.<TenantMemberPO>lambdaUpdate()
                .eq(TenantMemberPO::getId, memberId)
                .set(TenantMemberPO::getStatus, status));
    }

    @Override
    public void restore(Long memberId, Integer memberType, LocalDateTime joinedAt) {
        tenantMemberMapper.update(null, Wrappers.<TenantMemberPO>lambdaUpdate()
                .eq(TenantMemberPO::getId, memberId)
                .set(TenantMemberPO::getStatus, 1)
                .set(TenantMemberPO::getMemberType, memberType)
                .set(TenantMemberPO::getJoinedAt, joinedAt)
                .set(TenantMemberPO::getDeletedAt, null));
    }

    @Override
    public void markRemoved(Long memberId, LocalDateTime removedAt) {
        tenantMemberMapper.update(null, Wrappers.<TenantMemberPO>lambdaUpdate()
                .eq(TenantMemberPO::getId, memberId)
                .set(TenantMemberPO::getStatus, 2)
                .set(TenantMemberPO::getDeletedAt, removedAt));
    }

    @Override
    public List<Long> listUserIdsByTenant(Long tenantId) {
        return tenantMemberMapper.selectUserIdsByTenant(tenantId);
    }

    @Override
    public List<Long> listUserIdsByShop(Long tenantId, Long shopId) {
        return tenantMemberMapper.selectUserIdsByShop(tenantId, shopId);
    }

    private TenantMember toDomain(TenantMemberPO po) {
        if (po == null) {
            return null;
        }
        return TenantMember.builder()
                .id(po.getId())
                .tenantId(po.getTenantId())
                .shopId(po.getShopId())
                .userId(po.getUserId())
                .memberType(po.getMemberType())
                .status(po.getStatus())
                .joinedAt(po.getJoinedAt())
                .deletedAt(po.getDeletedAt())
                .build();
    }

    private TenantMemberView toView(TenantMemberRow row) {
        TenantMember member = TenantMember.builder()
                .id(row.getId())
                .tenantId(row.getTenantId())
                .shopId(row.getShopId())
                .userId(row.getUserId())
                .memberType(row.getMemberType())
                .status(row.getStatus())
                .joinedAt(row.getJoinedAt())
                .build();
        return new TenantMemberView(member, row.getUsername(), row.getNickname());
    }
}
