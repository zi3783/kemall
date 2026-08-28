package com.kemall.product.domain.dto;

import com.esotericsoftware.kryo.serializers.FieldSerializer;
import lombok.Builder;
import lombok.Data;

@Data
public class CategoryDTO {
    /**
     * 分类名称
     */
    @FieldSerializer.NotNull
    private String name;

    /**
     * 父级分类id
     */
    @FieldSerializer.NotNull
    private Long parentId;



    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 分类状态 1:正常 2:禁用
     */
    private Integer status;
}
