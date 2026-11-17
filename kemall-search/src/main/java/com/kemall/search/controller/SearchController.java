package com.kemall.search.controller;

import com.kemall.api.domain.dto.ProductSearchDTO;
import com.kemall.api.result.Result;
import com.kemall.common.core.annotation.LoginRequire;
import com.kemall.common.core.utils.bean.result.PageResult;
import com.kemall.search.domain.param.SearchParam;
import com.kemall.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "检索相关接口")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/autoPatch")
    @Operation(summary = "搜索栏自动补全")
    @LoginRequire(login = false)
    public Result<List<String>> searchAutoPatch(String prefix) {
        return searchService.searchAutoPatch(prefix);
    }

    @PostMapping
    @Operation(summary = "商品检索")
    @LoginRequire(login = false)
    public PageResult<ProductSearchDTO> search(@RequestBody SearchParam search) {
        return searchService.search(search);
    }

}
