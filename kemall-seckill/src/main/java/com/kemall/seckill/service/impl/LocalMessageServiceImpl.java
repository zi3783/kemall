package com.kemall.seckill.service.impl;

import com.kemall.seckill.domain.po.LocalMessage;
import com.kemall.seckill.mapper.LocalMessageMapper;
import com.kemall.seckill.service.ILocalMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 本地消息表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-10-19
 */
@Service
public class LocalMessageServiceImpl extends ServiceImpl<LocalMessageMapper, LocalMessage> implements ILocalMessageService {

}
