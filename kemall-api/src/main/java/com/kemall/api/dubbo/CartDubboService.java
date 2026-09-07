package com.kemall.api.dubbo;

import com.kemall.api.result.Result;

public interface CartDubboService {
    Result<String> clearCart();
}
