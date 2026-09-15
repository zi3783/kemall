package com.kemall.trade.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotNull
    private String idempotencyKey;

    @NotEmpty
    private List<OrderItemRequest> orderItemList;

}
