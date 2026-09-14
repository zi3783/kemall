package com.kemall.cart.mapper;

import com.kemall.cart.domain.dto.CartDto;
import com.kemall.cart.domain.po.Cart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 购物车主表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-09-04
 */
public interface CartMapper extends BaseMapper<Cart> {

    void insertByUserId(@Param("cart") Cart cart);

    Long deleteFromCartAndItemByUserId(Long userId);

    List<CartDto> selectCartByUserId(Long userId);
}
