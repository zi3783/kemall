package com.kemall.product.convert;

import com.kemall.api.dto.SkuCache;
import com.kemall.product.domain.vo.SkuVo;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-16T17:19:22+0800",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
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
        skuVo.setProductId( skuCache.getProductId() );
        skuVo.setSkuCode( skuCache.getSkuCode() );
        skuVo.setPrice( skuCache.getPrice() );
        skuVo.setMainImage( skuCache.getMainImage() );
        skuVo.setSpecJson( skuCache.getSpecJson() );
        skuVo.setStatus( skuCache.getStatus() );

        return skuVo;
    }
}
