package com.kemall.pay.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 退款表
 * </p>
 *
 * @author author
 * @since 2026-09-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("refund")
public class Refund implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 业务退款单号（幂等）
     */
    private String bizNo;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 关联支付单号
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
     * 退款金额（分）
     */
    private Long amount;

    /**
     * 原始支付金额（分）
     */
    private Long paymentAmount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 0-待退款 1-退款成功 2-退款失败
     */
    private Integer status;

    /**
     * 退款完成时间
     */
    private LocalDateTime refundTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
