package com.kemall.datasync.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CanalMessage {

    /**
     * 消息的全局唯一ID
     */
    private Long id;

    /**
     * 数据库名 (如: kemall_product)
     */
    @JsonProperty("database")
    private String databaseName;

    /**
     * 表名 (如: product, sku) - 注意这里用 @JsonProperty 映射
     */
    @JsonProperty("table")
    private String tableName;

    /**
     * 操作类型：INSERT / UPDATE / DELETE / CREATE TABLE / ALTER TABLE 等
     */
    private String type;

    /**
     * 是否属于 DDL 语句
     */
    private Boolean isDdl;

    /**
     * 主键字段名列表，例如 ["id"]
     */
    private List<String> pkNames;

    /**
     * 变更后的最新数据 (INSERT / UPDATE 时有值)
     * 使用 Object 而不是 String，防止 JSON 反序列化时类型异常
     */
    private List<Map<String, Object>> data;

    /**
     * 变更前的旧数据 (UPDATE 时可能有值，DELETE 时一般为空)
     */
    private List<Map<String, Object>> old;

    /**
     * 发生变更的时间戳 (毫秒)
     */
    private Long ts;

    /**
     * 执行的原始 SQL 语句 (主要用于 DDL)
     */
    private String sql;
}