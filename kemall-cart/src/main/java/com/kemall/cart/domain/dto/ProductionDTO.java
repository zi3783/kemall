package com.kemall.cart.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

@Builder
@Jacksonized
@Getter
public class ProductionDTO implements Serializable {
    private Long skuId;
    private Long productionId;
    private Integer quantity;
    private Long price;
    private Long userId;
    private Integer selected;
}
