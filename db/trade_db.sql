/*
 Navicat Premium Dump SQL

 Source Server         : wsl
 Source Server Type    : MySQL
 Source Server Version : 260700 (26.7.0)
 Source Host           : 172.24.223.134:3306
 Source Schema         : trade_db

 Target Server Type    : MySQL
 Target Server Version : 260700 (26.7.0)
 File Encoding         : 65001

 Date: 17/11/2026 15:03:53
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
) ENGINE = InnoDB AUTO_INCREMENT = 315 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '本地消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_items
-- ----------------------------
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单项id',
  `order_id` bigint NOT NULL COMMENT '订单id',
  `sku_id` bigint NOT NULL COMMENT '商品id',
  `product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品名称',
  `sku_price` bigint NOT NULL COMMENT '商品价格',
  `product_quantity` int NOT NULL COMMENT '商品数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 322 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单id',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `idempotency_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '幂等键',
  `total_amount` bigint NOT NULL COMMENT '订单总金额',
  `actual_amount` bigint NOT NULL COMMENT '实际支付金额',
  `status` tinyint NOT NULL COMMENT '订单状态 1:待付款 2:待发货 3:待收货 4:待评价 5:已完成 6:已取消',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `expire_time` datetime NOT NULL COMMENT '订单过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `order_type` tinyint NULL DEFAULT 1 COMMENT '1普通 2秒杀',
  `activity_id` bigint NULL DEFAULT NULL COMMENT '秒杀活动ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_idempotency_key`(`idempotency_key` ASC) USING BTREE,
  INDEX `idx_user_status_time`(`user_id` ASC, `status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7009 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tcc_fence_log
-- ----------------------------
DROP TABLE IF EXISTS `tcc_fence_log`;
CREATE TABLE `tcc_fence_log`  (
  `xid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'global id',
  `branch_id` bigint NOT NULL COMMENT 'branch id',
  `action_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'action name',
  `status` tinyint NOT NULL COMMENT 'status(tried:1;committed:2;rollbacked:3;suspended:4)',
  `gmt_create` datetime(3) NOT NULL COMMENT 'create time',
  `gmt_modified` datetime(3) NOT NULL COMMENT 'modify time',
  PRIMARY KEY (`xid`, `branch_id`) USING BTREE,
  INDEX `idx_gmt_modified`(`gmt_modified` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
