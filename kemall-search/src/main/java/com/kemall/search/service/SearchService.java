package com.kemall.search.service;

import com.kemall.api.result.Result;

import java.util.List;

public interface SearchService {
    Result<List<String>> searchAutoPatch(String prefix);
}
