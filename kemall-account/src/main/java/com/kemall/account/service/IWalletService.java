package com.kemall.account.service;

import com.kemall.account.domain.po.Wallet;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kemall.api.dto.WalletDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2026-08-07
 */
public interface IWalletService extends IService<Wallet> {

    void transaction(WalletDTO walletDTO);

    Wallet changeAmount(Long userId, Long amount);

    Long queryBalanceByUserId();

    boolean freezeAmount(Long balance, String bizId, Long userId);

    Wallet getWalletAndUpdate(Long balance, Long userId,String bizId);

    /**
     * TCC全局事务扣款入口：Try 阶段冻结金额，全局提交后由 TC 调用确认扣款
     *
     * @param userId 用户id
     * @param amount 扣款金额（分）
     * @param bizId  业务id（幂等标识）
     * @return true-扣款成功
     */
    boolean deductByTcc(Long userId, Long amount, String bizId);

}
