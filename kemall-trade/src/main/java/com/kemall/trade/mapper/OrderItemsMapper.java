package com.kemall.trade.mapper;

import com.kemall.trade.domain.po.OrderItems;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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
}
