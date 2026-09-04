package com.kemall.cart.domain.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class ProductionDTO {
    private Long skuId;
    private Integer quantity;
    private Long userId;
}
