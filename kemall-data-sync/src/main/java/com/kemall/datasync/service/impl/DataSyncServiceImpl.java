package com.kemall.datasync.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.datasync.domain.dto.CanalMessage;
import com.kemall.datasync.service.DataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        } catch (JsonProcessingException e) {
            throw new RuntimeException("string to object is fail",e);
        }

    }
}
