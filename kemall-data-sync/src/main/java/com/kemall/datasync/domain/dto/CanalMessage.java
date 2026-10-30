package com.kemall.datasync.domain.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CanalMessage {
    private String database;
    private String table;
    private String type; // INSERT, UPDATE, DELETE
    private List<Map<String, String>> data;
    private List<Map<String, String>> old;
}