package com.kemall.pay.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.kemall.pay.domain.enums.PaymentLogChangeTypeEnum;
import com.kemall.pay.domain.enums.PaymentStatusEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 支付流水表
 * </p>
 *
 * @author author
 * @since 2026-09-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("payment_log")
@Builder
public class PaymentLog implements Serializable {

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
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 金额（分）
     */
    private Long amount;

    /**
     * 1-发起支付 2-支付成功 3-支付失败 4-退款
     */
    private PaymentLogChangeTypeEnum changeType;

    /**
     * 变更前状态
     */
    private PaymentStatusEnum beforeStatus;

    /**
     * 变更后状态
     */
    private PaymentStatusEnum afterStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
