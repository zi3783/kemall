package com.kemall.product.convert;

import com.kemall.api.dto.SpuCache;
import com.kemall.product.domain.po.Product;
import com.kemall.product.domain.vo.SpuVo;
import com.kemall.product.enums.ProductStatus;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-16T17:17:10+0800",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class SpuConvertImpl implements SpuConvert {

    @Override
    public SpuVo toTarget(Product source) {
        if ( source == null ) {
            return null;
        }

        SpuVo spuVo = new SpuVo();

        spuVo.setId( source.getId() );
        spuVo.setProductCode( source.getProductCode() );
        spuVo.setName( source.getName() );
        spuVo.setMainImage( source.getMainImage() );
        spuVo.setDescription( source.getDescription() );
        spuVo.setPrice( source.getPrice() );
        spuVo.setCategoryId( source.getCategoryId() );
        spuVo.setBrandId( source.getBrandId() );
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

        product.setId( target.getId() );
        product.setProductCode( target.getProductCode() );
        product.setName( target.getName() );
        product.setMainImage( target.getMainImage() );
        product.setDescription( target.getDescription() );
        product.setPrice( target.getPrice() );
        product.setCategoryId( target.getCategoryId() );
        product.setBrandId( target.getBrandId() );
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
