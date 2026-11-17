/*
 Navicat Premium Dump SQL

 Source Server         : wsl
 Source Server Type    : MySQL
 Source Server Version : 260700 (26.7.0)
 Source Host           : 172.24.223.134:3306
 Source Schema         : cart_db

 Target Server Type    : MySQL
 Target Server Version : 260700 (26.7.0)
 File Encoding         : 65001

 Date: 17/11/2026 15:02:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_quantity` int NOT NULL DEFAULT 0 COMMENT '购物车总商品数量',
  `selected_count` int NOT NULL DEFAULT 0 COMMENT '选中商品数量',
  `total_price` bigint NOT NULL DEFAULT 0 COMMENT '购物车总价（单位：分）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cart_item
-- ----------------------------
DROP TABLE IF EXISTS `cart_item`;
CREATE TABLE `cart_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cart_id` bigint NOT NULL COMMENT '购物车主表ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `product_id` bigint NOT NULL COMMENT '商品SPU ID（冗余，方便展示商品名）',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `selected` tinyint NOT NULL DEFAULT 1 COMMENT '1-勾选 0-未勾选',
  `is_valid` tinyint NOT NULL DEFAULT 1 COMMENT '1-有效 0-失效（商品下架/删除）',
  `add_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_cart_sku`(`cart_id` ASC, `sku_id` ASC) USING BTREE,
  INDEX `idx_cart_id`(`cart_id` ASC) USING BTREE,
  INDEX `idx_sku_id`(`sku_id` ASC) USING BTREE,
  INDEX `idx_selected`(`selected` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log`  (
  `branch_id` bigint NOT NULL COMMENT 'branch transaction id',
  `xid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'global transaction id',
  `context` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` longblob NOT NULL COMMENT 'rollback info',
  `log_status` int NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` datetime(6) NOT NULL COMMENT 'create datetime',
  `log_modified` datetime(6) NOT NULL COMMENT 'modify datetime',
  UNIQUE INDEX `ux_undo_log`(`xid` ASC, `branch_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
