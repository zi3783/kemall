package com.kemall.account.mapper;

import com.kemall.account.domain.po.Wallet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-08-07
 */
public interface WalletMapper extends BaseMapper<Wallet> {

    Integer updateBalance(Long userId, Long balance, Integer version);
}
