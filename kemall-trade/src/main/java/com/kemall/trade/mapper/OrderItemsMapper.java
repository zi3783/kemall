package com.kemall.trade.mapper;

import com.kemall.trade.domain.po.OrderItems;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单项表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-09-14
 */
public interface OrderItemsMapper extends BaseMapper<OrderItems> {

    int insertBatch(List<OrderItems> items);

    void deleteByOrderNo(String orderNo);

    List<Map<String, Object>> selectByOrderNo(String orderNo);
}
