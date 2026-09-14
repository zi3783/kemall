package com.kemall.cart.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartDto {
    private Long cartId;
    private Long totalPrice;
    private Integer totalQuantity;
    private List<CartItemDto> items;

    @Data
    public static class CartItemDto{
        private Long skuId;
        private Integer quantity;
    }
}
