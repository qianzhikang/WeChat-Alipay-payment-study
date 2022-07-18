/*
 Navicat Premium Data Transfer

 Source Server         : 此电脑的数据库
 Source Server Type    : MySQL
 Source Server Version : 80029
 Source Host           : localhost:3306
 Source Schema         : payment_study

 Target Server Type    : MySQL
 Target Server Version : 80029
 File Encoding         : 65001

 Date: 18/07/2022 12:41:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_order_info
-- ----------------------------
DROP TABLE IF EXISTS `t_order_info`;
CREATE TABLE `t_order_info` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '订单id',
  `title` varchar(256) DEFAULT NULL COMMENT '订单标题',
  `order_no` varchar(50) DEFAULT NULL COMMENT '商户订单编号',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `product_id` bigint DEFAULT NULL COMMENT '支付产品id',
  `total_fee` int DEFAULT NULL COMMENT '订单金额(分)',
  `code_url` varchar(50) DEFAULT NULL COMMENT '订单二维码连接',
  `order_status` varchar(10) DEFAULT NULL COMMENT '订单状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of t_order_info
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_payment_info
-- ----------------------------
DROP TABLE IF EXISTS `t_payment_info`;
CREATE TABLE `t_payment_info` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '支付记录id',
  `order_no` varchar(50) DEFAULT NULL COMMENT '商户订单编号',
  `transaction_id` varchar(50) DEFAULT NULL COMMENT '支付系统交易编号',
  `payment_type` varchar(20) DEFAULT NULL COMMENT '支付类型',
  `trade_type` varchar(20) DEFAULT NULL COMMENT '交易类型',
  `trade_state` varchar(50) DEFAULT NULL COMMENT '交易状态',
  `payer_total` int DEFAULT NULL COMMENT '支付金额(分)',
  `content` text COMMENT '通知参数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of t_payment_info
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_product
-- ----------------------------
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `title` varchar(20) DEFAULT NULL COMMENT '商品名称',
  `price` int DEFAULT NULL COMMENT '价格（分）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of t_product
-- ----------------------------
BEGIN;
INSERT INTO `t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (1, 'Java课程', 1, '2022-07-15 13:12:46', '2022-07-15 13:12:46');
INSERT INTO `t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (2, '大数据课程', 1, '2022-07-15 13:12:46', '2022-07-15 13:12:46');
INSERT INTO `t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (3, '前端课程', 1, '2022-07-15 13:12:46', '2022-07-15 13:12:46');
INSERT INTO `t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (4, 'UI课程', 1, '2022-07-15 13:12:46', '2022-07-15 13:12:46');
COMMIT;

-- ----------------------------
-- Table structure for t_refund_info
-- ----------------------------
DROP TABLE IF EXISTS `t_refund_info`;
CREATE TABLE `t_refund_info` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '退款单id',
  `order_no` varchar(50) DEFAULT NULL COMMENT '商户订单编号',
  `refund_no` varchar(50) DEFAULT NULL COMMENT '商户退款单编号',
  `refund_id` varchar(50) DEFAULT NULL COMMENT '支付系统退款单号',
  `total_fee` int DEFAULT NULL COMMENT '原订单金额(分)',
  `refund` int DEFAULT NULL COMMENT '退款金额(分)',
  `reason` varchar(50) DEFAULT NULL COMMENT '退款原因',
  `refund_status` varchar(10) DEFAULT NULL COMMENT '退款状态',
  `content_return` text COMMENT '申请退款返回参数',
  `content_notify` text COMMENT '退款结果通知参数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of t_refund_info
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
