package com.kemall.cart.domain.po;

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
 * 购物车主表
 * </p>
 *
 * @author author
 * @since 2026-09-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cart")
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 购物车总商品数量
     */
    private Integer totalQuantity;

    /**
     * 选中商品数量
     */
    private Integer selectedCount;

    /**
     * 购物车总价（单位：分）
     */
    private Long totalPrice;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
