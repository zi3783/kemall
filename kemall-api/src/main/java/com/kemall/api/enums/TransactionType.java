package com.kemall.api.enums;

public enum TransactionType {
    RECHARGE(1, "充值"),
    CONSUME(2, "扣款");
    final Integer value;
    final String desc;
    TransactionType( Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
