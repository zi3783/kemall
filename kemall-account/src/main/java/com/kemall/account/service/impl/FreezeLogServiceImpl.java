package com.kemall.account.service.impl;

import com.kemall.account.domain.po.FreezeLog;
import com.kemall.account.mapper.FreezeLogMapper;
import com.kemall.account.service.IFreezeLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 冻结日志表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-21
 */
@Service
public class FreezeLogServiceImpl extends ServiceImpl<FreezeLogMapper, FreezeLog> implements IFreezeLogService {

}
