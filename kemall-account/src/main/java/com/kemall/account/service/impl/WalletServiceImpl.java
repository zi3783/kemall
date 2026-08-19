package com.kemall.account.service.impl;

import com.kemall.account.domain.po.Wallet;
import com.kemall.account.domain.po.WalletLog;
import com.kemall.account.enums.AccountStatusEnum;
import com.kemall.account.enums.WalletLogTypeEnum;
import com.kemall.account.mapper.WalletLogMapper;
import com.kemall.account.mapper.WalletMapper;
import com.kemall.account.service.IWalletService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kemall.api.dto.WalletDTO;
import com.kemall.common.exception.BusinessException;
import com.kemall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-07
 */
@Service
@RequiredArgsConstructor
public class WalletServiceImpl extends ServiceImpl<WalletMapper, Wallet> implements IWalletService {

    private final WalletMapper walletMapper;

    private final WalletLogMapper walletLogMapper;

    @Override
    @Transactional
    public void updateAccount(WalletDTO dto, WalletLogTypeEnum type) {
        if(dto == null) {
            throw new IllegalArgumentException("walletDTO is null");
        }
        //确认更改账户
        Long userId = dto.getUserId() != null ? dto.getUserId() : UserContext.getUserId();
        //查询金额
        Wallet one = lambdaQuery()
                .eq(Wallet::getUserId, userId)
                .one();
        if(one == null) {
            throw new BusinessException("未找到账户");
        }
        if(one.getStatus() == AccountStatusEnum.FROZEN){
            throw new BusinessException("账户已经冻结无法操作");
        }

        //修改数据库
        Long balance = one.getBalance() + dto.getBalance();
        if(balance < 0) {
            throw new BusinessException("金额不足");
        }
        Integer version = one.getVersion();

        Integer success = walletMapper.updateBalance(userId ,balance, version);

        WalletLog walletLog = new WalletLog();
        walletLog.setAmount(dto.getBalance());
        walletLog.setWalletId(one.getId());
        walletLog.setType(type);
        walletLog.setUserId(userId);
        //添加记录
        if(success > 0) {
            //成功
            walletLog.setStatus(1);
            walletLogMapper.insert(walletLog);
        }else{
            //失败
            walletLog.setStatus(2);
            walletLogMapper.insert(walletLog);
            throw new BusinessException("操作频繁");
        }
    }
}
