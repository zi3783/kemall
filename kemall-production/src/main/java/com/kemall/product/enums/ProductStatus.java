package com.kemall.product.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;


public enum ProductStatus {
    DELETED(0,"已删除"),
    DRAFT(1, "草稿"),
    PENDING_REVIEW(2, "待审核"),
    APPROVED(3, "审核通过"),
    ON_SHELF(4, "上架"),
    OFF_SHELF(5, "下架"),
    VIOLATION_OFF_SHELF(6, "违规下架");

    @Getter
    @EnumValue
    private final Integer code;
    
    @JsonValue
    private final String desc;

    ProductStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProductStatus getProductStatus(Integer code) {
        for (ProductStatus productStatus : ProductStatus.values()) {
            if (productStatus.getCode().equals(code)) {
                return productStatus;
            }
        }
        return null;
    }
}