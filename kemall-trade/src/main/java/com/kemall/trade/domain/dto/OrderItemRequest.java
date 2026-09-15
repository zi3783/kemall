package com.kemall.trade.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {

    @NotNull
    private Long skuId;
    @NotNull
    private Integer quantity;

}
