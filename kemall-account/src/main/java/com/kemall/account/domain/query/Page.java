package com.kemall.account.domain.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Page {
    @NotNull
    private Integer pageNo;
    @NotNull
    private Integer pageSize;
}
