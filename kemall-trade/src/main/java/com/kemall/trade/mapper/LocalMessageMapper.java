package com.kemall.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kemall.trade.domain.po.LocalMessage;

import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * 本地消息表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-09-30
 */
public interface LocalMessageMapper extends BaseMapper<LocalMessage> {
    void batchUpdateNextRetryTimeByIds(List<LocalMessage> list, LocalDateTime time);

    void batchUpdateStatusBymMsgIds(List<String> list);
}
