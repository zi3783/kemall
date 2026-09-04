package com.kemall.product.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class ProductVO {
    /**
     * 商品id
     */
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
     * 商品描述
     */
    private String description;

    /**
     * 商品价格
     */
    private Long price;

    /**
     * 商品末级分类id
     */
    private Long categoryId;

    private String categoryName;

    /**
     * 商品品牌id
     */
    private Long brandId;

    private String brandName;

    private List<ProductSkuVO> productSkuVOList;

}
