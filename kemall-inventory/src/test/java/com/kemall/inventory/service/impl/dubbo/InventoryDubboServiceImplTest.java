package com.kemall.inventory.service.impl.dubbo;

import com.kemall.api.dubbo.InventoryDubboService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InventoryDubboServiceImplTest {

    @Autowired
    private InventoryDubboService inventoryDubboService;

    @Test
    public void test(){
        inventoryDubboService.prepareDeductByTcc(3L, 1, "tcctransaction");
    }

}