package com.kemall.trade.service.impl;

import com.kemall.trade.domain.po.OrderItems;
import com.kemall.trade.mapper.OrderItemsMapper;
import com.kemall.trade.service.IOrderItemsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单项表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Service
public class OrderItemsServiceImpl extends ServiceImpl<OrderItemsMapper, OrderItems> implements IOrderItemsService {

}
