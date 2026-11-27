package com.kemall.iam.tenancy.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kemall.iam.tenancy.infrastructure.persistence.entity.TenantPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租户 Mapper。
 */
@Mapper
public interface TenantMapper extends BaseMapper<TenantPO> {

    List<TenantPO> selectPageList(@Param("keyword") String keyword,
                                  @Param("status") Integer status,
                                  @Param("offset") long offset,
                                  @Param("size") int size);

    long selectPageCount(@Param("keyword") String keyword,
                         @Param("status") Integer status);
}
