package com.kemall.cart.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartUpdateDTO {
    private Long totalPrice;
    private Integer totalQuantity;
    private Integer selected;
}
