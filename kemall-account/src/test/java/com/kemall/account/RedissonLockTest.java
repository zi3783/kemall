package com.kemall.account;

import com.kemall.api.dto.WalletDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
public class RedissonLockTest {

    @Autowired
    private Test test;

    @org.junit.jupiter.api.Test
    public void testAnnotation(){
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setUserId(11111L);
        test.test(walletDTO);
    }
}
