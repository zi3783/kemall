package com.kemall.product.domain.query;

import lombok.Data;

import java.util.List;

@Data
public class ProductCreateReq {
    private String name;
    private String mainImage;
    private String description;
    private Long price;
    private Long categoryId;
    private Long brandId;
    private List<SkuCreateReq> skuList;
}