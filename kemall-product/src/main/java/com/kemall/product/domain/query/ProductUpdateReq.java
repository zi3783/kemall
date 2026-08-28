package com.kemall.product.domain.query;

import lombok.Data;

@Data
public class ProductUpdateReq {
    private Long id;
    private String name;
    private String mainImage;
    private String description;
    private Long price;
    private Long categoryId;
    private Long brandId;
}