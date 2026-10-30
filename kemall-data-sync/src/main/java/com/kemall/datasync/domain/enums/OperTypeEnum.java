package com.kemall.datasync.domain.enums;

import lombok.Getter;

@Getter
public enum OperTypeEnum {
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),;
    private final String value;
    OperTypeEnum(String value) {
        this.value = value;
    }

    public boolean equal(String data){
        return this.value.equals(data);
    }

}
