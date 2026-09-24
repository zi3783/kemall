package com.kemall.common.dubbo.filter;

import com.kemall.common.core.exception.BusinessException;
import com.kemall.common.core.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Slf4j
@Activate(group = CommonConstants.CONSUMER)
public class ConsumerContextFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Long userId = UserContext.getUserId();
        if(userId==null){
            throw new BusinessException("消费端userId为null");
        }
        RpcContext.getClientAttachment().setAttachment("userId", String.valueOf(userId));
        return invoker.invoke(invocation);
    }
}
