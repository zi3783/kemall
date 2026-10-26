package com.kemall.api.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserOrderDto {

    private Long userId;

    private Long skuId;

    private String messageId;
}
