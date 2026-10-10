package com.kemall.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kemall.trade.domain.po.LocalMessage;

/**
 * <p>
 * 本地消息表 服务类
 * </p>
 *
 * @author author
 * @since 2026-09-30
 */
public interface ILocalMessageService extends IService<LocalMessage> {

    void sendMessage();
}
