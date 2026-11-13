package com.kemall.product.convert;

import com.kemall.api.domain.dto.SpuCache;
import com.kemall.common.core.utils.BaseConvert;
import com.kemall.product.domain.vo.SpuVo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SpuCacheConvert extends BaseConvert<SpuCache, SpuVo> {
}
