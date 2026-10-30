package com.kemall.datasync.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kemall.datasync.domain.interfaces.SyncVersion;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 商品品牌表
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrandSearchDTO implements SyncVersion {

    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    private String id;

    /**
     * 品牌名称
     */
    private String name;

    /**
     * 品牌描述
     */
    private String description;

    /**
     * 品牌状态 1:正常 2:禁用
     */
//    private Integer status;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private String createTime;

    /**
     * 更新时间
     */
    @JsonProperty("update_time")
    private String updateTime;


    @Override
    public String getVersion() {
        return getId(); //todo 暂时先写这个，数据库表还没有这个字段
    }
}
