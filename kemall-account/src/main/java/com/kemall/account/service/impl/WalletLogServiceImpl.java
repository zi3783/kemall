package com.kemall.account.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kemall.account.domain.po.WalletLog;
import com.kemall.account.domain.query.WalletLogPage;
import com.kemall.account.domain.result.PageResult;
import com.kemall.account.domain.vo.WalletLogVO;
import com.kemall.account.mapper.WalletLogMapper;
import com.kemall.account.service.IWalletLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kemall.common.utils.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-07
 */
@Service
public class WalletLogServiceImpl extends ServiceImpl<WalletLogMapper, WalletLog> implements IWalletLogService {

    @Override
    public PageResult<WalletLogVO> queryWalletLogByPage(WalletLogPage page) {
        //查找流水
        Page<WalletLog> p = lambdaQuery().eq(WalletLog::getUserId, UserContext.getUserId())
                .page(page.toPage());
        //封装流水
        //返回流水
        return PageResult.convert(p, WalletLogVO::new);
        //结束
    }
}
