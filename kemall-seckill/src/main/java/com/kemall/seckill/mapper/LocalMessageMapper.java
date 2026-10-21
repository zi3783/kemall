package com.kemall.seckill.mapper;

import com.kemall.seckill.domain.po.LocalMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 本地消息表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-10-19
 */
public interface LocalMessageMapper extends BaseMapper<LocalMessage> {

    void batchUpdateNextRetryTimeByIds(List<LocalMessage> list, LocalDateTime time);

    void batchUpdateStatusBymMsgIds(List<String> list);
}
