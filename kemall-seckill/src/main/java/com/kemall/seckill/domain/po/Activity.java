package com.kemall.seckill.domain.po;

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
 * 
 * </p>
 *
 * @author author
 * @since 2026-10-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("activity")
public class Activity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 业务活动编号，雪花ID/前缀+ID
     */
    private String activityNo;

    private String activityName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 0待开始 1进行中 2已结束 3已暂停
     */
    private Integer activityStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
