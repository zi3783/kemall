package com.kemall.account;

import com.kemall.account.annotation.RedissonLock;
import com.kemall.api.dto.WalletDTO;
import org.springframework.stereotype.Component;

//@Component
public class Test {
    @RedissonLock(key = "#dto.userId", waitTime = 3, prefix = "pre:")
    public void test(WalletDTO dto) {
        System.out.println("Say hello");
    }
}
