package com.kemall.pay.mapper;

import com.kemall.pay.domain.po.LocalMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

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

    int batchUpdateStatusBymMsgIds(List<String> list);
}
