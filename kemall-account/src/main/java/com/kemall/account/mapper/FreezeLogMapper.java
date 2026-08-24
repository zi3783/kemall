package com.kemall.account.mapper;

import com.kemall.account.domain.po.FreezeLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kemall.account.enums.FreezeLogStatusEnum;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 冻结日志表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-08-21
 */
@Mapper
public interface FreezeLogMapper extends BaseMapper<FreezeLog> {

    Integer updateOnVersion(FreezeLogStatusEnum status, Integer version, Long id);
}
