package com.kemall.cart.service.impl;

import com.kemall.cart.domain.po.CartItem;
import com.kemall.cart.mapper.CartItemMapper;
import com.kemall.cart.service.ICartItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 购物车详情表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-09-04
 */
@Service
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements ICartItemService {

}
