package com.kemall.inventory.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 库存流水表
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("inventory_log")
public class InventoryLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 库存日志id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品SKU id
     */
    @TableField("sku_id")
    private Long skuId;

    /**
     * 订单编号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 1-锁定 2-扣减 3-释放 4-入库
     */
    @TableField("change_type")
    private Integer changeType;

    @TableField("change_amount")
    private Integer changeAmount;

    @TableField("before_available")
    private Integer beforeAvailable;

    @TableField("after_available")
    private Integer afterAvailable;

    @TableField("before_locked")
    private Integer beforeLocked;

    @TableField("after_locked")
    private Integer afterLocked;

    @TableField("create_time")
    private LocalDateTime createTime;


}
