-- ----------------------------
-- Table structure for b_coupon
-- ----------------------------
DROP TABLE IF EXISTS `b_coupon`;
CREATE TABLE `b_coupon`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '优惠券名称',
  `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '优惠券类型:1-满减券,2-折扣券',
  `discount` decimal(10,2) DEFAULT NULL COMMENT '折扣率(0-1)',
  `threshold` decimal(10,2) DEFAULT '0.00' COMMENT '使用门槛金额',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '优惠金额',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态:0-禁用,1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- ----------------------------
-- Records of b_coupon
-- ----------------------------
INSERT INTO `b_coupon` VALUES (1, '满100减10元券', 1, NULL, 100.00, 10.00, '2023-01-01 00:00:00', '2024-12-31 23:59:59', 1, '2023-01-01 00:00:00', NULL);
INSERT INTO `b_coupon` VALUES (2, '满200减30元券', 1, NULL, 200.00, 30.00, '2023-01-01 00:00:00', '2024-12-31 23:59:59', 1, '2023-01-01 00:00:00', NULL);
INSERT INTO `b_coupon` VALUES (3, '9折优惠券', 2, 0.90, 0.00, NULL, '2023-01-01 00:00:00', '2024-12-31 23:59:59', 1, '2023-01-01 00:00:00', NULL);

-- ----------------------------
-- Table structure for b_user_coupon
-- ----------------------------
DROP TABLE IF EXISTS `b_user_coupon`;
CREATE TABLE `b_user_coupon`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `coupon_id` bigint(20) NOT NULL COMMENT '优惠券ID',
  `order_id` bigint(20) DEFAULT NULL COMMENT '使用的订单ID',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态:0-已使用,1-未使用,2-已过期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- ----------------------------
-- Records of b_user_coupon
-- ----------------------------
INSERT INTO `b_user_coupon` VALUES (1, 2, 1, NULL, 1, '2023-01-01 00:00:00', NULL);
INSERT INTO `b_user_coupon` VALUES (2, 2, 3, NULL, 1, '2023-01-01 00:00:00', NULL);

-- 插入示例数据
INSERT INTO `b_coupon` (`name`, `type`, `discount`, `threshold`, `amount`, `start_time`, `end_time`, `status`) VALUES
('新用户优惠券', 1, NULL, 100.00, 10.00, '2023-01-01 00:00:00', '2023-12-31 23:59:59', 1),
('全场折扣券', 2, 0.8, 0.00, NULL, '2023-01-01 00:00:00', '2023-12-31 23:59:59', 1); 