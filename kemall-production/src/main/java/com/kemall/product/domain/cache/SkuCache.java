package com.kemall.product.domain.cache;

import lombok.Data;

@Data
public class SkuCache {

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
}
