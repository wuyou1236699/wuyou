USE psychology_db;

DROP TABLE IF EXISTS `counselor_online_status`;
CREATE TABLE `counselor_online_status` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `counselor_id` BIGINT NOT NULL COMMENT '咨询师ID',
  `is_online` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否在线',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_counselor_id` (`counselor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询师在线状态表';

SELECT '在线状态表创建完成！' AS message;
