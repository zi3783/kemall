package com.kemall.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserTypeEnum {
    NORMAL(1,"普通用户"),
    ADMIN(2,"管理员");

    @EnumValue
    private final Integer type;
    @JsonValue
    private final String desc;

    UserTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
