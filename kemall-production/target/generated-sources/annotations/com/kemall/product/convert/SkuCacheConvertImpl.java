package com.kemall.product.convert;

import com.kemall.api.domain.dto.SkuCache;
import com.kemall.product.domain.vo.SkuVo;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-24T08:46:40+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.200.v20261008-1115, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class SkuCacheConvertImpl implements SkuCacheConvert {

    @Override
    public SkuVo toSkuVo(SkuCache skuCache) {
        if ( skuCache == null ) {
            return null;
        }

        SkuVo skuVo = new SkuVo();

        skuVo.setId( skuCache.getId() );
        skuVo.setMainImage( skuCache.getMainImage() );
        skuVo.setPrice( skuCache.getPrice() );
        skuVo.setProductId( skuCache.getProductId() );
        skuVo.setSkuCode( skuCache.getSkuCode() );
        skuVo.setSpecJson( skuCache.getSpecJson() );
        skuVo.setStatus( skuCache.getStatus() );

        return skuVo;
    }
}
