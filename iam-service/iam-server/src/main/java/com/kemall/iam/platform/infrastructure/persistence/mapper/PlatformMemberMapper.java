package com.kemall.iam.platform.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kemall.iam.platform.infrastructure.persistence.entity.PlatformMemberPO;
import com.kemall.iam.platform.infrastructure.persistence.entity.PlatformMemberRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 平台成员 Mapper。
 */
@Mapper
public interface PlatformMemberMapper extends BaseMapper<PlatformMemberPO> {

    List<PlatformMemberRow> selectPageList(@Param("keyword") String keyword,
                                           @Param("status") Integer status,
                                           @Param("offset") long offset,
                                           @Param("size") int size);

    long selectPageCount(@Param("keyword") String keyword,
                         @Param("status") Integer status);
}
