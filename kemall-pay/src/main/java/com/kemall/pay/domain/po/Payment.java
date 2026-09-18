package com.kemall.pay.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.kemall.pay.domain.enums.PaymentStatusEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 支付单表
 * </p>
 *
 * @author author
 * @since 2026-09-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@TableName("payment")
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付金额（单位：分）
     */
    private Long amount;

    /**
     * 支付渠道 BALANCE-余额支付
     */
    private String channel;

    /**
     * 0-待支付 1-支付成功 2-支付失败 3-已关闭
     */
    private PaymentStatusEnum status;

    /**
     * 已退款金额（分）
     */
    private Long refundedAmount;

    /**
     * 0-未退款 1-部分退款 2-全额退款
     */
    private Integer refundStatus;

    /**
     * 支付完成时间
     */
    private LocalDateTime payTime;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
