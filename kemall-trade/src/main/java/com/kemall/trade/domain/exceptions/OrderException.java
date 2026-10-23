package com.kemall.trade.domain.exceptions;

public class OrderException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public static final String INVENTORY_DEDUCT_FAIL = "库存扣减失败，skuId:";

    private OrderException(String message) {
        super(message);
    }
    private OrderException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class InventoryDeductException extends OrderException {
        public InventoryDeductException(Long skuId) {
            super(INVENTORY_DEDUCT_FAIL + skuId);
        }
        public InventoryDeductException(Long skuId, Throwable cause) {
            super(INVENTORY_DEDUCT_FAIL + skuId, cause);
        }
    }
}
