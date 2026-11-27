package com.kemall.iam.tenancy.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kemall.iam.tenancy.infrastructure.persistence.entity.ShopPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 店铺 Mapper。
 */
@Mapper
public interface ShopMapper extends BaseMapper<ShopPO> {

    List<ShopPO> selectPageList(@Param("tenantId") Long tenantId,
                                @Param("keyword") String keyword,
                                @Param("status") Integer status,
                                @Param("offset") long offset,
                                @Param("size") int size);

    long selectPageCount(@Param("tenantId") Long tenantId,
                         @Param("keyword") String keyword,
                         @Param("status") Integer status);
}
