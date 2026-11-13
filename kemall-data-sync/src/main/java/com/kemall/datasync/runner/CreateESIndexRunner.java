package com.kemall.datasync.runner;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class CreateESIndexRunner implements CommandLineRunner {

    private final ElasticsearchClient esClient;

    private final Map<String, CreateIndexRequest> indexMap = new HashMap<>();

    @Override
    public void run(String... args) throws Exception {
        initCreateIndexRequest();

        Set<String> indexNames = indexMap.keySet();

        for (String indexName : indexNames) {
            BooleanResponse exists = esClient.indices().exists(i -> i.index(indexName));
            if (exists.value()) {
                log.info("index {} exists", indexName);
                continue;
            }
            try {
                CreateIndexResponse createIndexResponse = esClient.indices().create(indexMap.get(indexName));
                if(createIndexResponse.acknowledged()){
                    log.info("index {} created", indexName);
                }else {
                    log.info("index {} not created", indexName);
                    throw new RuntimeException("index " + indexName + " not created");
                }
            }catch (ElasticsearchException e) {
                // 3. 兜底：并发场景下 exists 检查后仍可能被其他实例抢先创建
                if ("resource_already_exists_exception".equals(e.error().type())) {
                    log.info("Index already exists (race), skip: {}", indexName);
                    continue;
                }
                log.error("Create Index Failed: {}, type={}, reason={}",
                        indexName, e.error().type(), e.error().reason());
                throw e;   // fail-fast，让应用启动失败
            }
        }
    }

    private void initCreateIndexRequest() {
        CreateIndexRequest productIndexRequest = CreateIndexRequest.of(
                i -> i.index("product")
                        .mappings(
                                m -> m.properties("id", p -> p.keyword(k -> k))
                                        .properties("product_code", p -> p.keyword(k -> k))
                                        .properties("description", p -> p
                                                .text(t -> t)
                                        )
                                        .properties("name", p -> p
                                                .text(t -> t)
                                        )
                                        .properties("suggest", p -> p.completion(t -> t.analyzer("ik_max_word")))
                                        .properties("price", p -> p.integer(k -> k))
                                        .properties("categoryId", p -> p.keyword(k -> k))
                                        .properties("main_image", p -> p.keyword(k -> k))
                                        .properties("create_time", p -> p.date(k -> k))
                                        .properties("update_time", p -> p.date(k -> k))
                                        .properties("version", p -> p.long_(k -> k))
                        )
        );
        indexMap.put("product",  productIndexRequest);
    }
}
