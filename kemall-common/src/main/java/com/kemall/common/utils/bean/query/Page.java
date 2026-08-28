package com.kemall.common.utils.bean.query;

import com.esotericsoftware.kryo.serializers.FieldSerializer.NotNull;
import lombok.Data;

@Data
public class Page {
    @NotNull
    private Integer pageNo;
    @NotNull
    private Integer pageSize;
}
