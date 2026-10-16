-- 检查b_user_coupon表是否存在
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = DATABASE() AND table_name = 'b_user_coupon';

-- 检查字段名
SELECT COLUMN_NAME FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'b_user_coupon';

-- 添加update_time字段（如果不存在）
ALTER TABLE `b_user_coupon` 
ADD COLUMN `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
ON DUPLICATE KEY UPDATE `update_time`=`update_time`;

-- 确保use_time字段的命名正确
-- 如果表中有used_time字段而没有use_time字段，则重命名
-- 注意：此操作需要在确认字段存在后才能执行
-- 请先执行上面的检查字段名查询，然后根据结果决定是否执行下面的语句

-- ALTER TABLE `b_user_coupon` 
-- CHANGE COLUMN `used_time` `use_time` datetime DEFAULT NULL COMMENT '使用时间';

-- 如果表中既没有used_time也没有use_time字段，则添加use_time字段
-- ALTER TABLE `b_user_coupon` 
-- ADD COLUMN `use_time` datetime DEFAULT NULL COMMENT '使用时间'; 