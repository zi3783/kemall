package com.kemall.common.core.utils.bean.query;

import com.esotericsoftware.kryo.serializers.FieldSerializer;
import lombok.Data;

@Data
public class Page {
    @FieldSerializer.NotNull
    private Integer pageNo;
    @FieldSerializer.NotNull
    private Integer pageSize;
}
