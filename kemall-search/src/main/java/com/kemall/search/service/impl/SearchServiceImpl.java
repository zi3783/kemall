package com.kemall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import com.kemall.api.result.Result;
import com.kemall.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchClient esClient;

    @Override
    public Result<List<String>> searchAutoPatch(String prefix) {
        try {
            SearchResponse<Void> response = esClient.search(
                    s -> s.index("product")
                            .suggest(su -> su.suggesters(
                                    "product_suggester",
                                    sg -> sg.prefix(prefix)
                                            .completion(c -> c
                                                    .field("suggest")
                                                    .size(10)
                                                    .skipDuplicates(true)
                                            )
                            )),
                    Void.class
            );

            List<Suggestion<Void>> suggestions = response.suggest().get("product_suggester");
            if (suggestions == null || suggestions.isEmpty()) {
                return Result.success(List.of());
            }

            return Result.success(suggestions.get(0).completion().options().stream()
                    .map(CompletionSuggestOption::text) // text() 即你写入的 input 字符串
                    .toList());
        } catch (IOException e) {
            throw new RuntimeException("请求失败",e);
        }
    }
}
