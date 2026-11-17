/*
 Navicat Premium Dump SQL

 Source Server         : wsl
 Source Server Type    : MySQL
 Source Server Version : 260700 (26.7.0)
 Source Host           : 172.24.223.134:3306
 Source Schema         : inventory_db

 Target Server Type    : MySQL
 Target Server Version : 260700 (26.7.0)
 File Encoding         : 65001

 Date: 17/11/2026 15:03:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for inventory
-- ----------------------------
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory`  (
  `sku_id` bigint NOT NULL COMMENT '商品SKU id',
  `available_quantity` int NOT NULL COMMENT '可用库存数量',
  `locked_quantity` int NOT NULL COMMENT '锁定库存数量',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `stock_type` tinyint NOT NULL DEFAULT 1 COMMENT '1普通库存 2秒杀库存',
  `activity_id` bigint NULL DEFAULT NULL COMMENT '秒杀活动id',
  PRIMARY KEY (`sku_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for inventory_log
-- ----------------------------
DROP TABLE IF EXISTS `inventory_log`;
CREATE TABLE `inventory_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存日志id',
  `sku_id` bigint NOT NULL COMMENT '商品SKU id',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `change_type` tinyint NOT NULL COMMENT '1-锁定 2-扣减 3-释放 4-入库',
  `change_amount` int NOT NULL,
  `before_available` int NULL DEFAULT NULL,
  `after_available` int NULL DEFAULT NULL,
  `before_locked` int NULL DEFAULT NULL,
  `after_locked` int NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_change`(`order_no` ASC, `sku_id` ASC, `change_type` ASC) USING BTREE,
  INDEX `idx_sku_id`(`sku_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 107 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存流水表' ROW_FORMAT = Dynamic;

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
  `gmt_modified` datetime(3) NOT NULL COMMENT 'update time',
  PRIMARY KEY (`xid`, `branch_id`) USING BTREE,
  INDEX `idx_gmt_modified`(`gmt_modified` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` bigint NOT NULL,
  `xid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `context` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  `ext` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_undo_log`(`xid` ASC, `branch_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
