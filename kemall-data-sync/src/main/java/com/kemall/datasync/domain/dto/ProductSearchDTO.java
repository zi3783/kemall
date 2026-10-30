package com.kemall.datasync.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kemall.datasync.domain.interfaces.SyncVersion;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSearchDTO implements SyncVersion {
    /**
     * 主键
     */
    private String id;

    /**
     * 商品编码
     */
    @JsonProperty("product_code")
    private String productCode;

    /**
     * 商品名称
     */
    private String name;


    /**
     * 商品价格
     */
    private String price;

    /**
     * 商品末级分类id
     */
    @JsonProperty("category_id")
    private String categoryId;

    /**
     * 商品品牌id
     */
    @JsonProperty("brand_id")
    private String brandId;

    /**
     * 商品主图URL
     */
    @JsonProperty("main_image")
    private String mainImage;

    /**
     * 商品状态 1:草稿 2:待审核 3:审核通过 4:上架 5:下架 6:违规下架
     */
//    private ProductStatus status;

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

    private String version;



}
