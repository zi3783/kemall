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
 * 购物车详情表
 * </p>
 *
 * @author author
 * @since 2026-09-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cart_item")
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 购物车主表ID
     */
    private Long cartId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 商品SPU ID（冗余，方便展示商品名）
     */
    private Long productId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 1-勾选 0-未勾选
     */
    private Integer selected;

    /**
     * 1-有效 0-失效（商品下架/删除）
     */
    private Integer isValid;

    /**
     * 加入时间
     */
    private LocalDateTime addTime;

    private LocalDateTime updateTime;


}
