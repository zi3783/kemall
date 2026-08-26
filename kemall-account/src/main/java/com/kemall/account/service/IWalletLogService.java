package com.kemall.account.service;

import com.kemall.account.domain.po.WalletLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kemall.account.domain.query.WalletLogPage;
import com.kemall.account.domain.result.PageResult;
import com.kemall.account.domain.vo.WalletLogVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2026-08-07
 */
public interface IWalletLogService extends IService<WalletLog> {

    PageResult<WalletLogVO> queryWalletLogByPage(WalletLogPage page);
}
