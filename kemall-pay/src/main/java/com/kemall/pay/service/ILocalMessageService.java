package com.kemall.pay.service;

import com.kemall.pay.domain.po.LocalMessage;
import com.baomidou.mybatisplus.extension.service.IService;

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
