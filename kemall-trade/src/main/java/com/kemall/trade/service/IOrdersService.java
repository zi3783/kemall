package com.kemall.trade.service;

import com.kemall.common.utils.bean.result.Result;
import com.kemall.trade.domain.dto.OrderRequest;
import com.kemall.trade.domain.po.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author author
 * @since 2026-09-14
 */
public interface IOrdersService extends IService<Orders> {

    Result<Object> placeOrder(OrderRequest request);
}
