package com.kemall.trade.service.impl;

import com.kemall.trade.domain.po.Orders;
import com.kemall.trade.mapper.OrdersMapper;
import com.kemall.trade.service.IOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

}
