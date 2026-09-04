package com.kemall.product.domain.query;

import lombok.Data;

@Data
public class SkuCreateReq {
    private String skuCode;
    private Long price;
    private String mainImage;
    private String specJson;  // {"颜色":"黑色","容量":"128G"}
}