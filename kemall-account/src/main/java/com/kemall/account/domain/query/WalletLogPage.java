package com.kemall.account.domain.query;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kemall.account.domain.po.WalletLog;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class WalletLogPage{
    private String orderBy;
    @NotNull
    private Integer pageNo;
    @NotNull
    private Integer pageSize;
    private Boolean isAsc = false;
    public Page<WalletLog> toPage(){
        Page<WalletLog> page = new Page<>(pageNo,pageSize);
        if(orderBy != null && !orderBy.equals("")){
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(orderBy);
            orderItem.setAsc(isAsc);
            page.setOrders(List.of(orderItem));
        }
        return page;
    }
}
