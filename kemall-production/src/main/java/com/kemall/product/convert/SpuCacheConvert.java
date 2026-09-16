package com.kemall.product.convert;

import com.kemall.api.dto.SpuCache;
import com.kemall.common.utils.BaseConvert;
import com.kemall.product.domain.vo.SpuVo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SpuCacheConvert extends BaseConvert<SpuCache, SpuVo> {
}
