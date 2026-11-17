package com.kemall.search.domain.param;

import jakarta.annotation.Nullable;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchParam {
    private String categoryId;
    private String brand;
    private String productName;
    @Nullable
    private Long minPrice;
    @Nullable
    private Long maxPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer currentPage = 1;
    private Integer pageSize = 10;
}
