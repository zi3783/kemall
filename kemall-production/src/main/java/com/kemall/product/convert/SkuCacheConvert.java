package com.kemall.product.convert;

import com.kemall.api.domain.dto.SkuCache;
import com.kemall.product.domain.vo.SkuVo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkuCacheConvert {
    SkuVo toSkuVo(SkuCache skuCache);
}
