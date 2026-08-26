package com.kemall.account.domain.vo;

import com.kemall.account.enums.WalletLogTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WalletLogVO {

    private Long id;


    private Long amount;

    /**
     * 1-充值 2-扣款 3-确认扣款 4-退款/取消扣款
     */
    private WalletLogTypeEnum type;

    /**
     * 1-成功 2-失败
     */
    private Integer status;

    private LocalDateTime createTime;
}
