package com.kemall.product.convert;

import com.kemall.api.domain.dto.SpuCache;
import com.kemall.product.domain.po.Product;
import com.kemall.product.domain.vo.SpuVo;
import com.kemall.product.enums.ProductStatus;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-24T08:46:40+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.200.v20261008-1115, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class SpuConvertImpl implements SpuConvert {

    @Override
    public SpuVo toTarget(Product source) {
        if ( source == null ) {
            return null;
        }

        SpuVo spuVo = new SpuVo();

        spuVo.setBrandId( source.getBrandId() );
        spuVo.setCategoryId( source.getCategoryId() );
        spuVo.setDescription( source.getDescription() );
        spuVo.setId( source.getId() );
        spuVo.setMainImage( source.getMainImage() );
        spuVo.setName( source.getName() );
        spuVo.setPrice( source.getPrice() );
        spuVo.setProductCode( source.getProductCode() );
        if ( source.getStatus() != null ) {
            spuVo.setStatus( source.getStatus().ordinal() );
        }

        return spuVo;
    }

    @Override
    public Product toSource(SpuVo target) {
        if ( target == null ) {
            return null;
        }

        Product product = new Product();

        product.setBrandId( target.getBrandId() );
        product.setCategoryId( target.getCategoryId() );
        product.setDescription( target.getDescription() );
        product.setId( target.getId() );
        product.setMainImage( target.getMainImage() );
        product.setName( target.getName() );
        product.setPrice( target.getPrice() );
        product.setProductCode( target.getProductCode() );
        if ( target.getStatus() != null ) {
            product.setStatus( ProductStatus.values()[ target.getStatus() ] );
        }

        return product;
    }

    @Override
    public List<SpuVo> toTargetList(List<Product> sources) {
        if ( sources == null ) {
            return null;
        }

        List<SpuVo> list = new ArrayList<SpuVo>( sources.size() );
        for ( Product product : sources ) {
            list.add( toTarget( product ) );
        }

        return list;
    }

    @Override
    public List<Product> toSourceList(List<SpuVo> targets) {
        if ( targets == null ) {
            return null;
        }

        List<Product> list = new ArrayList<Product>( targets.size() );
        for ( SpuVo spuVo : targets ) {
            list.add( toSource( spuVo ) );
        }

        return list;
    }

    @Override
    public SpuCache toSpuCache(Product spu) {
        if ( spu == null ) {
            return null;
        }

        SpuCache spuCache = new SpuCache();

        spuCache.setId( spu.getId() );
        spuCache.setProductCode( spu.getProductCode() );
        spuCache.setName( spu.getName() );
        spuCache.setMainImage( spu.getMainImage() );
        spuCache.setDescription( spu.getDescription() );
        spuCache.setPrice( spu.getPrice() );
        spuCache.setCategoryId( spu.getCategoryId() );
        spuCache.setBrandId( spu.getBrandId() );
        if ( spu.getStatus() != null ) {
            spuCache.setStatus( spu.getStatus().ordinal() );
        }

        return spuCache;
    }
}
