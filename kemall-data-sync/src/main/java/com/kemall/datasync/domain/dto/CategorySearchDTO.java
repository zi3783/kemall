package com.kemall.datasync.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kemall.datasync.domain.interfaces.SyncVersion;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 商品分类表
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategorySearchDTO implements Serializable, SyncVersion {

    private static final long serialVersionUID = 1L;

    /**
     * 分类id
     */
    private String id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父级分类id
     */
    @JsonProperty("parent_id")
    private String parentId;

    /**
     * 分类层级
     */
    private String level;

    /**
     * 分类路径 如："/1/2/3/4"
     */
    private String path;

    /**
     * 排序序号
     */
    @JsonProperty("sort_order")
    private String sortOrder;

    /**
     * 分类状态 1:正常 2:禁用
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
