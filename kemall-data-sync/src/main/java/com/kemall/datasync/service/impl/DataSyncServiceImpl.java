package com.kemall.datasync.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.VersionType;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.datasync.domain.dto.CanalMessage;
import com.kemall.api.domain.dto.ProductSearchDTO;
import com.kemall.datasync.exception.DataException;
import com.kemall.datasync.service.DataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncServiceImpl implements DataSyncService {

    private final ObjectMapper objectMapper;

    private final ElasticsearchClient esClient;

    @Override
    public void syncElastic(String message) {

        try {
            CanalMessage canalMessage = objectMapper.readValue(message, CanalMessage.class);
            //先校验是否为ddl
            if(canalMessage.getIsDdl()) {
                //是直接抛异常
                throw new DataException(DataException.DDL_DO_NOT_SYNC);
            }
            String type = canalMessage.getType();
            switch (type) {
                case "INSERT":
                case "UPDATE":
                    insertAndUpdate(canalMessage);
                    break;
                case "DELETE":
                    deleteSync(canalMessage);
                    break;
                default:
                    throw new RuntimeException("未知操作类型，无法同步");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("string to object is fail",e);
        }

    }

    private void deleteSync(CanalMessage canalMessage) {
        if(canalMessage == null) {
            throw new NullPointerException("canalMessage is null");
        }
        List<Map<String, Object>> data = canalMessage.getData();
        String tableName = canalMessage.getTableName();

        List<BulkOperation> syncList = new ArrayList<>();
        //情况一
        if("product".equals(tableName) && data != null) {
            for (Map<String, Object> datum : data) {
                BulkOperation bulkOperation = BulkOperation.of(i -> i.delete(
                        k -> k.index(tableName)
                                .id(datum.get("id").toString())
                ));
                syncList.add(bulkOperation);
            }
        }

        if(syncList.isEmpty()) {
            return;
        }

        BulkRequest bulkRequest = BulkRequest.of(b -> b.operations(syncList));
        try {
            BulkResponse bulk = esClient.bulk(bulkRequest);
            if (bulk.errors()) {
                List<String> reasons = new ArrayList<>();
                boolean isRepeat = false;
                for (BulkResponseItem item : bulk.items()) {
                    if(item.error() == null) {
                        continue;
                    }
                    String reason = item.error().reason();
                    String type = item.error().type();

                    if(!"version_conflict_engine_exception".equals(type)) {
                        isRepeat = true;
                    }else {
                        log.error("同步失败，原因：{}", reason);
                        reasons.add(reason);
                    }
                }
                if(isRepeat) {
                    throw new DataException("同步失败，原因如下：" + reasons);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("同步失败，请检查网络");
        }
    }

    private void insertAndUpdate(CanalMessage canalMessage) {
        if(canalMessage == null) {
            throw new NullPointerException("canalMessage is null");
        }
        List<Map<String, Object>> data = canalMessage.getData();
        String tableName = canalMessage.getTableName();

        List<BulkOperation> syncList = new ArrayList<>();
        //情况一
        if("product".equals(tableName) && data != null) {
            for (Map<String, Object> datum : data) {
                ProductSearchDTO productSearchDTO = objectMapper.convertValue(datum, ProductSearchDTO.class);
                productSearchDTO.setSuggest(productSearchDTO.getName());
                BulkOperation bulkOperation = BulkOperation.of(i -> i.index(
                        k -> k.index(tableName)
                                .document(productSearchDTO)
                                .id(productSearchDTO.getId())
                                .version(Long.parseLong(productSearchDTO.getVersion()))
                                .versionType(VersionType.External)
                ));
                syncList.add(bulkOperation);
            }
        }

        if(syncList.isEmpty()) {
            return;
        }

        BulkRequest bulkRequest = BulkRequest.of(b -> b.operations(syncList));
        try {
            BulkResponse bulk = esClient.bulk(bulkRequest);
            if (bulk.errors()) {
                List<String> reasons = new ArrayList<>();
                boolean isRepeat = false;
                for (BulkResponseItem item : bulk.items()) {
                    if(item.error() == null) {
                        continue;
                    }
                    String reason = item.error().reason();
                    String type = item.error().type();

                    if(!"version_conflict_engine_exception".equals(type)) {
                        isRepeat = true;
                    }else {
                        log.error("同步失败，原因：{}", reason);
                        reasons.add(reason);
                    }
                }
                if(isRepeat) {
                    throw new DataException("同步失败，原因如下：" + reasons);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("同步失败，请检查网络");
        }
    }
}
