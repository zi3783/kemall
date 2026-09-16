package com.kemall.product.convert;

import com.kemall.api.dto.SkuCache;
import com.kemall.product.domain.po.ProductSku;
import com.kemall.product.domain.vo.SkuVo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkuConvert {
    SkuCache toSkuCache(ProductSku productSku);
    SkuVo toSkuVo(ProductSku productSku);
}
