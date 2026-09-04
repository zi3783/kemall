package com.kemall.product.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.common.annotation.RedissonLock;
import com.kemall.common.exception.BusinessException;
import com.kemall.common.utils.BeanUtil;
import com.kemall.common.utils.bean.result.PageResult;
import com.kemall.common.utils.bean.result.Result;
import com.kemall.product.constants.RedisConstants;
import com.kemall.product.domain.po.Brand;
import com.kemall.product.domain.po.Category;
import com.kemall.product.domain.po.Product;
import com.kemall.product.domain.po.ProductSku;
import com.kemall.product.domain.query.ProductCreateReq;
import com.kemall.product.domain.query.ProductQuery;
import com.kemall.product.domain.query.ProductUpdateReq;
import com.kemall.product.domain.query.SkuCreateReq;
import com.kemall.product.domain.vo.ProductIntro;
import com.kemall.product.domain.vo.ProductSkuVO;
import com.kemall.product.domain.vo.ProductVO;
import com.kemall.product.enums.ProductSkuStatus;
import com.kemall.product.enums.ProductStatus;
import com.kemall.product.mapper.BrandMapper;
import com.kemall.product.mapper.CategoryMapper;
import com.kemall.product.mapper.ProductMapper;
import com.kemall.product.mapper.ProductSkuMapper;
import com.kemall.product.service.IProductService;
import com.kemall.product.service.VisitedCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    private final BrandMapper brandMapper;

    private final CategoryMapper categoryMapper;

    private final ProductSkuMapper productSkuMapper;

    private final ObjectMapper objectMapper;

    private final StringRedisTemplate redisTemplate;

    private final VisitedCountService visitedCountService;

    private final ProductMapper productMapper;

    @Override
    public Result<ProductVO> getProductDetail(Long productId) {
        if(productId == null){
            throw new IllegalArgumentException("productId is null");
        }
        //代码最准备
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String visitKey = buildHourKey(productId, 0);
        //查询redis缓存
        String key = RedisConstants.PRODUCT_DETAIL_PREFIX + productId;
        String json = ops.get(key);
        //命中 则返回 异步计数器 + 1
        if(json != null){
            try {
                ProductVO productVO = objectMapper.readValue(json, ProductVO.class);
                visitedCountService.visitedCountIncrement(visitKey);
                return Result.success(productVO);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("json转换失败");
            }
        }
        //未命中 判断是否为热点数据 计数器 + 1
        Long newCount = ops.increment(visitKey);
        if(newCount != null && newCount.equals(1L)){
            redisTemplate.expire(visitKey, 48, TimeUnit.HOURS);
        }
        //是热数据则加锁 （ 上个小时内访问量大于阈值）
        if(newCount != null && newCount > 5){
            ProductServiceImpl productService = (ProductServiceImpl) AopContext.currentProxy();
            ProductVO vo = productService.lockProduct(productId, ops, key);
            if(vo != null){
                return Result.success(vo);
            }
        }
        //不是热数据则不加锁
        //直接访问db
        ProductVO productVO = productMapper.queryProductDetailById(productId);
        //写入redis缓存5分钟
        try {
            json = objectMapper.writeValueAsString(productVO);
            ops.set(key, json, 5, TimeUnit.MINUTES);
            return Result.success(productVO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("object转换json失败");
        }
    }

    @Override
    public Result<PageResult<ProductIntro>> queryProductIntroByCondition(ProductQuery query) {
        //查询分类
        Page<ProductIntro> page = productMapper.queryProductIntroByCondition(query.toPage(), query.getCategoryId(), query.getBrandId());
        PageResult<ProductIntro> pageResult = new PageResult<>();
        pageResult.setPageNum(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(page.getRecords());
        pageResult.setPages(page.getPages());
        return Result.success(pageResult);
    }

    private ProductVO queryProductVoOnDb(Long productId) {
        Product product = lambdaQuery().eq(Product::getId, productId)
                .eq(Product::getStatus, ProductStatus.ON_SHELF)
                .one();
        if(product == null){
            throw new BusinessException("商品不存在");
        }
        ProductVO productVO = BeanUtil.copyBean(product, ProductVO.class);

        LambdaQueryWrapper<Brand> brandWrapper = new LambdaQueryWrapper<Brand>().select(Brand::getName)
                .eq(Brand::getId, product.getBrandId());
        List<Brand> brands = brandMapper.selectList(brandWrapper);
        if(brands != null && !brands.isEmpty()){
            productVO.setBrandName(brands.get(0).getName());
        }

        LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<Category>().select(Category::getName)
                .eq(Category::getId, product.getCategoryId());
        List<Category> categories = categoryMapper.selectList(categoryWrapper);
        if(categories != null && !categories.isEmpty()){
            productVO.setCategoryName(categories.get(0).getName());
        }

        LambdaQueryWrapper<ProductSku> productSkuLambdaQueryWrapper = new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, productId)
                .eq(ProductSku::getStatus, ProductSkuStatus.NORMAL);
        List<ProductSku> productSkus = productSkuMapper.selectList(productSkuLambdaQueryWrapper);

        List<ProductSkuVO> productSkuVOS = BeanUtil.copyList(productSkus, ProductSkuVO.class);
        productVO.setProductSkuVOList(productSkuVOS);
        return productVO;
    }

    private String buildHourKey(Long productId, int hourOffset) {
        String hour = LocalDateTime.now()
                .minusHours(hourOffset)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        return RedisConstants.PRODUCT_VISITED_PREFIX + productId + ":" + hour;
    }

    @RedissonLock(key = "#productId", waitTime = 10, prefix = RedisConstants.PRODUCT_LOCK_PREFIX)
    public ProductVO lockProduct(Long productId, ValueOperations<String, String> ops, String key) {
        //再次访问redis防止请求打入db
        String json = ops.get(key);
        //命中则返回
        if(json != null){
            try {
                ProductVO productVO = objectMapper.readValue(json, ProductVO.class);
                return productVO;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("json转object失败");
            }
        }
        //访问db
        ProductVO productVO = productMapper.queryProductDetailById(productId);
        //写入redis缓存1小时
        try {
            json = objectMapper.writeValueAsString(productVO);
            ops.set(key, json, 1, TimeUnit.HOURS);
            return productVO;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("object转换json失败");
        }
    }


    @Transactional
    @Override
    public void createProduct(ProductCreateReq req) {
        // 1. 生成商品编码
        String productCode = "SPU" + System.currentTimeMillis();

        // 2. 插入商品
        Product product = BeanUtil.copyBean(req, Product.class);
        product.setProductCode(productCode);
        product.setStatus(ProductStatus.DRAFT);
        save(product);

        // 3. 批量插入 SKU
        if (CollectionUtils.isEmpty(Collections.singleton(req.getSkuList()))) {
            throw new BusinessException("至少创建一个SKU");
        }
        for (SkuCreateReq skuReq : req.getSkuList()) {
            ProductSku sku = BeanUtil.copyBean(skuReq, ProductSku.class);
            sku.setProductId(product.getId());
            sku.setSkuCode("SKU" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000));
            sku.setStatus(1);
            productSkuMapper.insert(sku);
        }
    }

    @Transactional
    @Override
    public void updateStatus(Long productId, Integer status) {
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查状态流转合法性
        if (!isValidStatusTransition(product.getStatus().getCode(), status)) {
            throw new BusinessException("状态流转不合法");
        }

        product.setStatus(ProductStatus.getProductStatus(status));
        updateById(product);

        // 删除缓存
        redisTemplate.delete(RedisConstants.PRODUCT_DETAIL_PREFIX + productId);
    }

    private boolean isValidStatusTransition(Integer current, Integer target) {
        // 草稿 → 待审核 → 审核通过 → 上架 → 下架
        // 下架 → 上架（重新上架）
        // 下架 → 草稿（撤回）
        // 违规下架 → 下架
        return true;
    }

    @Transactional
    @Override
    public void updateProduct(ProductUpdateReq req) {
        // 1. 检查商品存在
        Product product = getById(req.getId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 2. 更新
        BeanUtils.copyProperties(req, product);
        updateById(product);

        // 3. 删除缓存
        redisTemplate.delete(RedisConstants.PRODUCT_DETAIL_PREFIX + req.getId());
    }

    @Transactional
    @Override
    public void deleteProduct(Long productId) {
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 逻辑删除：status 设为 0
        product.setStatus(ProductStatus.DELETED);
        updateById(product);

        // 删除缓存
        redisTemplate.delete(RedisConstants.PRODUCT_DETAIL_PREFIX + productId);
    }
}
