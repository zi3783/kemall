package com.kemall.iam.tenancy.domain.exception;

import java.util.Map;

/**
 * 店铺不存在。
 */
public class ShopNotFoundException extends TenancyDomainException {

    private static final long serialVersionUID = 1L;

    public ShopNotFoundException(Long shopId) {
        super(TenancyErrorCode.SHOP_NOT_FOUND, Map.of("shopId", String.valueOf(shopId)));
    }
}
