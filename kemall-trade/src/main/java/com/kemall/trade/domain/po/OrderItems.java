package com.kemall.trade.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单项表
 * </p>
 *
 * @author author
 * @since 2026-09-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("order_items")
@Builder
public class OrderItems implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单项id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 商品id
     */
    private Long skuId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品价格
     */
    private Long skuPrice;

    /**
     * 商品数量
     */
    private Integer productQuantity;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
