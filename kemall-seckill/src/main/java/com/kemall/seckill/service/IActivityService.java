package com.kemall.seckill.service;

import com.kemall.seckill.domain.po.Activity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2026-10-19
 */
public interface IActivityService extends IService<Activity> {

    void seckill(Long skuId);
}
