package com.kemall.trade.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kemall.trade.domain.enums.LocalMessageStatusEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 本地消息表
 * </p>
 *
 * @author author
 * @since 2026-09-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("local_message")
@Builder
public class LocalMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息id
     */
    private String messageId;

    /**
     * 交换机
     */
    private String exchange;

    /**
     * 路由键
     */
    private String routingKey;

    /**
     * 0-未投递 1-已投递 2-失败
     */
    private LocalMessageStatusEnum status;

    /**
     * 消息体json
     */
    private String payload;

    /**
     * 已重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetry;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 最后一次失败原因
     */
    private String errorMsg;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
