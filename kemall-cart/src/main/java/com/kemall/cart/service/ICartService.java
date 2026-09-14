package com.kemall.cart.service;

import com.kemall.cart.domain.po.Cart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 购物车主表 服务类
 * </p>
 *
 * @author author
 * @since 2026-09-04
 */
public interface ICartService extends IService<Cart> {

    void addToCart(Long pId, Long skuId, Integer quantity);

    Map<String, Object> listCart(Long userId);
}
