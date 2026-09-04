package com.kemall.product.domain.vo;

import lombok.Data;

@Data
public class ProductIntro {
    private Long id;

    /**
     * 商品编码
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品主图URL
     */
    private String mainImage;

    /**
     * 商品价格
     */
    private Long price;
}
