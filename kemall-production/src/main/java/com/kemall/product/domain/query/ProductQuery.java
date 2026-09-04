package com.kemall.product.domain.query;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kemall.product.domain.po.Product;
import lombok.Data;

@Data
public class ProductQuery extends com.kemall.common.utils.bean.query.Page {
    private Long categoryId;
    private Long brandId;

    public Page<Product> toPage() {
        return new Page<>(super.getPageNo(),super.getPageSize());
    }
}
