package com.kemall.product.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryTreeVO {
    private Long id;
    private String name;
    private String path;
    private Integer level;
    private Integer sortOrder;
    private List<CategoryTreeVO> children = new ArrayList<>();
}
