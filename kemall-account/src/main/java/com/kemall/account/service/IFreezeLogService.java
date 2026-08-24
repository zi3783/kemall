package com.kemall.account.service;

import com.kemall.account.annotation.RedissonLock;
import com.kemall.account.domain.po.FreezeLog;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 冻结日志表 服务类
 * </p>
 *
 * @author author
 * @since 2026-08-21
 */
public interface IFreezeLogService extends IService<FreezeLog> {

    boolean confirmAccount(Long id);


    boolean confirmLogic(Long freezeLogId, Long userId);

    boolean cancelAccount(Long freezeLogId);
}
