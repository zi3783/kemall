package com.kemall.product.convert;

import com.kemall.api.dto.SkuCache;
import com.kemall.product.domain.po.ProductSku;
import com.kemall.product.domain.vo.SkuVo;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-16T17:19:22+0800",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
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
        skuVo.setProductId( productSku.getProductId() );
        skuVo.setSkuCode( productSku.getSkuCode() );
        skuVo.setPrice( productSku.getPrice() );
        skuVo.setMainImage( productSku.getMainImage() );
        skuVo.setSpecJson( productSku.getSpecJson() );
        skuVo.setStatus( productSku.getStatus() );

        return skuVo;
    }
}
