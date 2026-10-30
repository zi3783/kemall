package com.kemall.datasync.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.VersionType;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.datasync.domain.dto.*;
import com.kemall.datasync.domain.enums.OperTypeEnum;
import com.kemall.datasync.domain.interfaces.SyncVersion;
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
            CanalMessage po = objectMapper.readValue(message, CanalMessage.class);
            if(po.getIsDdl()) {
                throw new DataException(DataException.DDL_DO_NOT_SYNC);
            }
            //先根据表转换成对象
            String tableName = po.getTableName();
            String type = po.getType();

            List<BulkOperation> bulkOperations = new ArrayList<>();

            if(OperTypeEnum.DELETE.equal(type)) {
                for (Map<String, Object> oldData : po.getOld()) {
                    // 提取主键，假设你的主键字段叫 id
                    String id = oldData.get("id").toString();

                    if (id != null) {
                        // 构建删除操作
                        BulkOperation deleteOp = BulkOperation.of(b -> b.delete(d -> d
                                .index(tableName)
                                .id(id)
                                .version(po.getTs())
                                .versionType(VersionType.External)
                        ));
                        bulkOperations.add(deleteOp);
                    }
                }
            }else if(OperTypeEnum.INSERT.equal(type) || OperTypeEnum.UPDATE.equal(type)) {
                List<? extends SyncVersion> row;
                List<Map<String, Object>> data = po.getData();
                switch (tableName) {
                    case "product":{
                        row = data.stream().map(
                                m -> objectMapper.convertValue(m, ProductSearchDTO.class)
                        ).toList();
                        break;
                    } case "brand":{
                        row = data.stream().map(
                                m -> objectMapper.convertValue(m, BrandSearchDTO.class)
                        ).toList();
                        break;
                    } case "category":{
                        row = data.stream().map(
                                m -> objectMapper.convertValue(m, CategorySearchDTO.class)
                        ).toList();
                        break;
                    } case "product_sku":{
                        row = data.stream().map(
                                m -> objectMapper.convertValue(m, ProductSkuSearchDTO.class)
                        ).toList();
                        break;
                    } default:
                        return;
                }
                row.forEach(m -> {
                    BulkOperation bulkOperation = BulkOperation.of(
                            fn -> fn.index(
                                    i -> i.index(tableName)
                                            .id(m.getId())
                                            .document(m)
                                            .version(Long.parseLong(m.getVersion()))
                                            .versionType(VersionType.External)
                            )
                    );
                    bulkOperations.add(bulkOperation);
                });
            }

            BulkRequest bulkRequest = BulkRequest.of(k -> k.operations(bulkOperations));
            BulkResponse response = esClient.bulk(bulkRequest);
            if (response.errors()) {
                boolean hasCriticalError = false;
                for (BulkResponseItem item : response.items()) {
                    if (item.error() != null) {
                        // 如果是版本冲突，直接忽略（属于正常现象，说明有更新的数据已经写入了）
                        if ("version_conflict_engine_exception".equals(item.error().type())) {
                            log.debug("忽略版本冲突, 表: {}, id: {}", tableName, item.id());
                        } else {
                            // 其他错误（如 ES 磁盘满、字段类型不匹配等），记录日志并标记需要重试
                            log.error("ES 写入失败, 表: {}, id: {}, 原因: {}", tableName, item.id(), item.error().reason());
                            hasCriticalError = true;
                        }
                    }
                }
                if (hasCriticalError) {
                    throw new DataException("部分 ES 写入失败，需要重试");
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Canal 消息解析失败: {}", message, e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("ES 网络异常", e);
            throw new RuntimeException(e);
        }
    }
}
