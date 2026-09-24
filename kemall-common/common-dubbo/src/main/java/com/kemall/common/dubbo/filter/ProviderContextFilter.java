package com.kemall.common.dubbo.filter;

import com.kemall.common.core.exception.BusinessException;
import com.kemall.common.core.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Slf4j
@Activate(group = CommonConstants.PROVIDER)
public class ProviderContextFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String userId = RpcContext.getServiceContext().getAttachment("userId");
        if(userId==null){
            throw new BusinessException("生产端userId为null");
        }
        try {
            UserContext.setUserId(Long.valueOf(userId));
            return invoker.invoke(invocation);
        }finally {
            UserContext.removeUserId();
        }
    }
}
