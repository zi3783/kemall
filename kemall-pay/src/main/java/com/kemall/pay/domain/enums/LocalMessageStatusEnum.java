package com.kemall.pay.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LocalMessageStatusEnum {
    PENDING(0, "待投递"),
    SENT(1, "已投递"),
    FAIL(2, "失败");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String desc;
}
