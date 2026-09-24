package com.kemall.account;

import com.kemall.api.dto.WalletDTO;
import com.kemall.common.core.annotation.RedissonLock;

//@Component
public class Test {
    @RedissonLock(key = "#dto.userId", waitTime = 3, prefix = "pre:")
    public void test(WalletDTO dto) {
        System.out.println("Say hello");
    }
}
