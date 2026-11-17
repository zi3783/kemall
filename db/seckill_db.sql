/*
 Navicat Premium Dump SQL

 Source Server         : wsl
 Source Server Type    : MySQL
 Source Server Version : 260700 (26.7.0)
 Source Host           : 172.24.223.134:3306
 Source Schema         : seckill_db

 Target Server Type    : MySQL
 Target Server Version : 260700 (26.7.0)
 File Encoding         : 65001

 Date: 17/11/2026 15:03:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务活动编号，雪花ID/前缀+ID',
  `activity_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `activity_status` tinyint NOT NULL DEFAULT 0 COMMENT '0待开始 1进行中 2已结束 3已暂停',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_activity_no`(`activity_no` ASC) USING BTREE,
  INDEX `idx_status_start`(`activity_status` ASC, `start_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for local_message
-- ----------------------------
DROP TABLE IF EXISTS `local_message`;
CREATE TABLE `local_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息id',
  `exchange` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交换机',
  `routing_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '路由键',
  `status` tinyint NOT NULL COMMENT '0-未投递 1-已投递 2-失败',
  `payload` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息体json',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '已重试次数',
  `max_retry` int NOT NULL DEFAULT 5 COMMENT '最大重试次数',
  `next_retry_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下次重试时间',
  `error_msg` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一次失败原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_status_next_retry`(`status` ASC, `next_retry_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7018 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '本地消息表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
