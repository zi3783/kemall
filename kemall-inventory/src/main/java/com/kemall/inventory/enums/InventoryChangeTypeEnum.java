package com.kemall.inventory.enums;

/**
 * <p>
 * 库存流水变更类型枚举
 * </p>
 * <p>
 * 同时作为 TCC 状态机：LOCK（Try 锁定） -> DEDUCT（Confirm 扣减）/ RELEASE（Cancel 释放）
 * </p>
 *
 * @author author
 * @since 2026-08-31
 */
public enum InventoryChangeTypeEnum {

    /**
     * 锁定（TCC Try）
     */
    LOCK(1, "锁定"),

    /**
     * 扣减（TCC Confirm）
     */
    DEDUCT(2, "扣减"),

    /**
     * 释放（TCC Cancel）
     */
    RELEASE(3, "释放"),

    /**
     * 入库
     */
    STOCK_IN(4, "入库");

    private final Integer code;

    private final String msg;

    InventoryChangeTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}