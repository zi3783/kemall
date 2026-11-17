package com.kemall.search.service;

import com.kemall.api.domain.dto.ProductSearchDTO;
import com.kemall.api.result.Result;
import com.kemall.common.core.utils.bean.result.PageResult;
import com.kemall.search.domain.param.SearchParam;

import java.util.List;

public interface SearchService {
    Result<List<String>> searchAutoPatch(String prefix);

    PageResult<ProductSearchDTO> search(SearchParam search);
}
