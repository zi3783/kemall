package com.kemall.product.convert;

import com.kemall.api.domain.dto.SkuCache;
import com.kemall.product.domain.po.ProductSku;
import com.kemall.product.domain.vo.SkuVo;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-24T08:46:40+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.200.v20261008-1115, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class SkuConvertImpl implements SkuConvert {

    @Override
    public SkuCache toSkuCache(ProductSku productSku) {
        if ( productSku == null ) {
            return null;
        }

        SkuCache skuCache = new SkuCache();

        skuCache.setId( productSku.getId() );
        skuCache.setProductId( productSku.getProductId() );
        skuCache.setSkuCode( productSku.getSkuCode() );
        skuCache.setPrice( productSku.getPrice() );
        skuCache.setMainImage( productSku.getMainImage() );
        skuCache.setSpecJson( productSku.getSpecJson() );
        skuCache.setStatus( productSku.getStatus() );

        return skuCache;
    }

    @Override
    public SkuVo toSkuVo(ProductSku productSku) {
        if ( productSku == null ) {
            return null;
        }

        SkuVo skuVo = new SkuVo();

        skuVo.setId( productSku.getId() );
        skuVo.setMainImage( productSku.getMainImage() );
        skuVo.setPrice( productSku.getPrice() );
        skuVo.setProductId( productSku.getProductId() );
        skuVo.setSkuCode( productSku.getSkuCode() );
        skuVo.setSpecJson( productSku.getSpecJson() );
        skuVo.setStatus( productSku.getStatus() );

        return skuVo;
    }
}
