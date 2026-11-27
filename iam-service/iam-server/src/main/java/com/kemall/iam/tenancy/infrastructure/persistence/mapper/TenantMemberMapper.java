package com.kemall.iam.tenancy.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kemall.iam.tenancy.infrastructure.persistence.entity.TenantMemberPO;
import com.kemall.iam.tenancy.infrastructure.persistence.entity.TenantMemberRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租户/店铺成员 Mapper。
 */
@Mapper
public interface TenantMemberMapper extends BaseMapper<TenantMemberPO> {

    List<TenantMemberRow> selectPageList(@Param("tenantId") Long tenantId,
                                         @Param("shopId") Long shopId,
                                         @Param("memberType") Integer memberType,
                                         @Param("status") Integer status,
                                         @Param("offset") long offset,
                                         @Param("size") int size);

    long selectPageCount(@Param("tenantId") Long tenantId,
                         @Param("shopId") Long shopId,
                         @Param("memberType") Integer memberType,
                         @Param("status") Integer status);

    List<Long> selectUserIdsByTenant(@Param("tenantId") Long tenantId);

    List<Long> selectUserIdsByShop(@Param("tenantId") Long tenantId,
                                   @Param("shopId") Long shopId);
}
