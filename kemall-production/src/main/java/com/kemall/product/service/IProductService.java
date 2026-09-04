package com.kemall.product.service;

import com.kemall.common.utils.bean.result.PageResult;
import com.kemall.common.utils.bean.result.Result;
import com.kemall.product.domain.po.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kemall.product.domain.query.ProductCreateReq;
import com.kemall.product.domain.query.ProductQuery;
import com.kemall.product.domain.query.ProductUpdateReq;
import com.kemall.product.domain.vo.ProductIntro;
import com.kemall.product.domain.vo.ProductVO;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
public interface IProductService extends IService<Product> {

    Result<ProductVO> getProductDetail(Long productId);

    Result<PageResult<ProductIntro>> queryProductIntroByCondition(ProductQuery query);

    @Transactional
    void createProduct(ProductCreateReq req);

    @Transactional
    void updateStatus(Long productId, Integer status);

    @Transactional
    void updateProduct(ProductUpdateReq req);

    @Transactional
    void deleteProduct(Long productId);
}
