package com.kemall.product.domain.po;

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
 * 
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("product_sku")
public class ProductSku implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品SKU id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品id
     */
    private Long productId;

    /**
     * 商品SKU编码
     */
    private String skuCode;

    /**
     * 商品价格 单位：分
     */
    private Long price;

    /**
     * SKU主图
     */
    private String mainImage;

    /**
     * 规格值（如{"颜色":"黑色","容量":"128G"}）
     */
    private String specJson;

    /**
     * 商品SKU状态 1:正常 2:禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
