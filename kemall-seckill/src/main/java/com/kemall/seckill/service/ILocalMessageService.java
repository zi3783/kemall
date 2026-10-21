package com.kemall.seckill.service;

import com.kemall.seckill.domain.po.LocalMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 本地消息表 服务类
 * </p>
 *
 * @author author
 * @since 2026-10-19
 */
public interface ILocalMessageService extends IService<LocalMessage> {

    void sendMessage();
}
