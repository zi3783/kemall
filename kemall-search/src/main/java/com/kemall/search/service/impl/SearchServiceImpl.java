package com.kemall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import co.elastic.clients.json.JsonData;
import com.kemall.api.domain.dto.ProductSearchDTO;
import com.kemall.api.result.Result;
import com.kemall.common.core.utils.bean.result.PageResult;
import com.kemall.search.domain.param.SearchParam;
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

    @Override
    public PageResult<ProductSearchDTO> search(SearchParam search) {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        if(search.getProductName() != null && !search.getProductName().isEmpty()){
            builder.must(
                    m -> m.multiMatch( mm ->  mm.fields("product_name^3", "description^1")
                            .query(search.getProductName()))
            );
        }
        if(search.getBrand() != null && !search.getBrand().isEmpty()){
            builder.filter(
                    m -> m.term(t -> t.field("brand").value(search.getBrand()))
            );
        }
        if(search.getCategory() != null && !search.getCategory().isEmpty()){
            builder.filter(
                    m -> m.term(t -> t.field("category_id").value(search.getCategory()))
            );
        }
        if (search.getStartTime() != null || search.getEndTime() != null) {
            builder.filter(m -> m.range(r -> {
                r.field("create_time");
                if (search.getStartTime() != null) r.gte(JsonData.of(search.getStartTime()));
                if (search.getEndTime()   != null) r.lte(JsonData.of(search.getEndTime()));
                return r;
            }));
        }

        BoolQuery query = builder.build();

        int from = (search.getCurrentPage() - 1)  * search.getPageSize();

        SearchRequest request = SearchRequest.of(i -> i.index("product")
                .query(q -> q.bool(query))
                .from(from)
                .size(search.getPageSize()));

        try {
            SearchResponse<ProductSearchDTO> response = esClient.search(
                    request, ProductSearchDTO.class
            );
            HitsMetadata<ProductSearchDTO> metadata = response.hits();
            List<Hit<ProductSearchDTO>> hits = metadata.hits();
            List<ProductSearchDTO> ret = hits.stream().map(Hit::source).toList();
            PageResult<ProductSearchDTO> result = new PageResult<>();
            if(metadata.total() == null) {
                return new PageResult<>();
            }
            long total = metadata.total().value();
            long pages = total / search.getPageSize() + 1;
            long current = search.getCurrentPage();
            long size = search.getPageSize();
            result.setTotal(total);
            result.setPages(pages);
            result.setPageNum(current);
            result.setPageSize(size);
            result.setRecords(ret);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
