package com.kemall.product.domain.cache;

import com.kemall.product.enums.ProductStatus;
import lombok.Data;

@Data
public class SpuCache {
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

    /**
     * 商品品牌id
     */
    private Long brandId;

    /**
     * 商品状态 1:草稿 2:待审核 3:审核通过 4:上架 5:下架 6:违规下架
     */
    private ProductStatus status;
}
