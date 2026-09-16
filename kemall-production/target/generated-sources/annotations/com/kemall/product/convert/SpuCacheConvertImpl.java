package com.kemall.product.convert;

import com.kemall.api.dto.SpuCache;
import com.kemall.product.domain.vo.SpuVo;
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
public class SpuCacheConvertImpl implements SpuCacheConvert {

    @Override
    public SpuVo toTarget(SpuCache source) {
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
        spuVo.setStatus( source.getStatus() );

        return spuVo;
    }

    @Override
    public SpuCache toSource(SpuVo target) {
        if ( target == null ) {
            return null;
        }

        SpuCache spuCache = new SpuCache();

        spuCache.setId( target.getId() );
        spuCache.setProductCode( target.getProductCode() );
        spuCache.setName( target.getName() );
        spuCache.setMainImage( target.getMainImage() );
        spuCache.setDescription( target.getDescription() );
        spuCache.setPrice( target.getPrice() );
        spuCache.setCategoryId( target.getCategoryId() );
        spuCache.setBrandId( target.getBrandId() );
        spuCache.setStatus( target.getStatus() );

        return spuCache;
    }

    @Override
    public List<SpuVo> toTargetList(List<SpuCache> sources) {
        if ( sources == null ) {
            return null;
        }

        List<SpuVo> list = new ArrayList<SpuVo>( sources.size() );
        for ( SpuCache spuCache : sources ) {
            list.add( toTarget( spuCache ) );
        }

        return list;
    }

    @Override
    public List<SpuCache> toSourceList(List<SpuVo> targets) {
        if ( targets == null ) {
            return null;
        }

        List<SpuCache> list = new ArrayList<SpuCache>( targets.size() );
        for ( SpuVo spuVo : targets ) {
            list.add( toSource( spuVo ) );
        }

        return list;
    }
}
