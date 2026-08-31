package com.kemall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.common.exception.BusinessException;
import com.kemall.common.utils.BeanUtil;
import com.kemall.common.utils.bean.result.Result;
import com.kemall.product.constants.RedisConstants;
import com.kemall.product.domain.dto.CategoryDTO;
import com.kemall.product.domain.po.Category;
import com.kemall.product.domain.po.Product;
import com.kemall.product.domain.vo.CategoryTreeVO;
import com.kemall.product.enums.CategoryStatus;
import com.kemall.product.enums.ProductStatus;
import com.kemall.product.mapper.CategoryMapper;
import com.kemall.product.mapper.ProductMapper;
import com.kemall.product.service.ICategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品分类表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final DefaultRedisScript redisScript;

    private final ProductMapper productMapper;

    @Override
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        //从redis读缓存
        String json = redisTemplate.opsForValue().get(RedisConstants.CATEGORY_TREE);
        //读到返回
        if(json != null){
            try {
                List<CategoryTreeVO> vos = objectMapper.readValue(json, new TypeReference<List<CategoryTreeVO>>() {});
                return Result.success(vos);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("jsonToObject fail",e);
            }
        }
        //没读到则构建
        List<CategoryTreeVO> vos = getCategoryTreeVOS();
        //写入redis
        try {
            json = objectMapper.writeValueAsString(vos);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("objectToJson fail",e);
        }
        redisTemplate.opsForValue().set(RedisConstants.CATEGORY_TREE, json);
        //返回
        return Result.success(vos);
    }

    private List<CategoryTreeVO> getCategoryTreeVOS() {
        List<Category> list = lambdaQuery()
                .eq(Category::getStatus, CategoryStatus.NORMAL)
                .list();

        Map<Long, CategoryTreeVO> map = list.stream().map(c -> BeanUtil.copyBean(c, CategoryTreeVO.class))
                .collect(Collectors.toMap(CategoryTreeVO::getId, c -> c));

        List<CategoryTreeVO> vos = new ArrayList<>();
        for (Category category : list) {
            Long pid = category.getParentId();
            Long id = category.getId();

            if(pid == null || pid == 0L ){
                vos.add(map.get(id));
            }else{
                CategoryTreeVO parent = map.get(pid);
                if(parent == null){
                    vos.add(map.get(id));
                    continue;
                }
                parent.getChildren().add(map.get(id));
            }
        }
        return vos;
    }

    @Override
    public void saveCategory(CategoryDTO dto) {
        Category category = BeanUtil.copyBean(dto, Category.class);
        category.setLevel(-1);
        category.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        category.setPath("#");

        Long parentId = dto.getParentId();
        if(parentId == null || parentId == 0L){
            category.setLevel(1);
            save(category);
            Long id = category.getId();
            category.setPath("/" + id);
            updateById(category);
            redisTemplate.delete(RedisConstants.CATEGORY_TREE);
            return ;
        }

        Category parent = getById(parentId);
        save(category);
        Long id = category.getId();
        category.setPath(parent.getPath() + "/" + id);
        category.setLevel(parent.getLevel() + 1);
        updateById(category);
        redisTemplate.delete(RedisConstants.CATEGORY_TREE);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        // 1. 检查分类是否存在
        Category category = getById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 2. 检查是否有子分类
        long childCount = lambdaQuery()
                .eq(Category::getParentId, categoryId)
                .eq(Category::getStatus, CategoryStatus.NORMAL)
                .count();
        if (childCount > 0) {
            throw new BusinessException("该分类下存在子分类，请先删除子分类");
        }

        // 3. 检查是否有商品关联
        long productCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCategoryId, categoryId)
                        .eq(Product::getStatus, ProductStatus.ON_SHELF)
        );
        if (productCount > 0) {
            throw new BusinessException("该分类下存在上架商品，请先下架商品");
        }

        // 4. 逻辑删除（改状态）
        Category update = new Category();
        update.setId(categoryId);
        update.setStatus(CategoryStatus.DISABLED.getCode());
        updateById(update);

        // 5. 清除分类树缓存
        redisTemplate.delete(RedisConstants.CATEGORY_TREE);
    }
}
