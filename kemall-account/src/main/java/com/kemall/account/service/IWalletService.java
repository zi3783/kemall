package com.kemall.account.service;

import com.kemall.account.domain.po.Wallet;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kemall.account.enums.WalletLogTypeEnum;
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

    void updateAccount(WalletDTO dto, WalletLogTypeEnum type);

}
