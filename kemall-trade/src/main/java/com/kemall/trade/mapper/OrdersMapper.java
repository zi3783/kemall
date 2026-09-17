package com.kemall.trade.mapper;

import com.kemall.api.dto.OrderDto;
import com.kemall.trade.domain.po.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kemall.trade.enums.OrderStatusEnum;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-09-14
 */
public interface OrdersMapper extends BaseMapper<Orders> {

    int updateStatusByOrderNo(String orderNo,@Param("status") OrderStatusEnum orderStatusEnum);

    void deleteByOrderNo(String orderNo);

    @Select("select id, order_no, user_id, idempotency_key, total_amount, actual_amount, status, expire_time from orders where order_no = #{orderNo}")
    OrderDto selectByOrderNo(String orderNo);
}
