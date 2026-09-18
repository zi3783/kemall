package com.kemall.pay.mapper;

import com.kemall.pay.domain.po.Payment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 支付单表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-09-17
 */
public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("select * from payment where order_no = #{orderNo}")
    Payment selectByOrderNo(String orderNo);


    @Update("update payment set status = #{status} where status = 0 and id = #{id}")
    int updateStatus(Long id, int status);
}
