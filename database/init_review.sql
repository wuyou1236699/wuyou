USE psychology_db;

-- 创建评价表
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `counselor_id` BIGINT NOT NULL COMMENT '咨询师ID',
  `appointment_id` BIGINT DEFAULT NULL COMMENT '预约ID',
  `rating` INT NOT NULL DEFAULT 5 COMMENT '评分(1-5)',
  `content` TEXT NOT NULL COMMENT '评价内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_counselor` (`counselor_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- 插入一些测试评价数据
-- 为李医生添加评价 (counselor_id=1)
INSERT INTO `review` (`user_id`, `counselor_id`, `rating`, `content`, `create_time`) VALUES
(1, 1, 5, '李医生非常专业，帮助我走出了困境。', '2026-04-20 10:00:00'),
(2, 1, 4, '很有耐心，咨询效果很好。', '2026-04-18 15:00:00'),
(3, 1, 5, '态度很好，给了很多有用的建议。', '2026-04-15 09:00:00');

-- 为王医生添加评价 (counselor_id=2)
INSERT INTO `review` (`user_id`, `counselor_id`, `rating`, `content`, `create_time`) VALUES
(4, 2, 5, '王医生的情绪管理方法很有效！', '2026-04-22 11:00:00'),
(5, 2, 4, '体验不错，推荐。', '2026-04-20 14:00:00');

-- 为张医生添加评价 (counselor_id=3)
INSERT INTO `review` (`user_id`, `counselor_id`, `rating`, `content`, `create_time`) VALUES
(1, 3, 5, '张医生的家庭咨询方法很实用，帮助我和家人关系改善了很多。', '2026-04-19 16:00:00'),
(6, 3, 4, '很好的咨询师，很理解人。', '2026-04-17 10:00:00');

-- 为刘医生添加评价 (counselor_id=4)
INSERT INTO `review` (`user_id`, `counselor_id`, `rating`, `content`, `create_time`) VALUES
(7, 4, 5, '刘医生的青少年心理辅导非常专业，孩子很喜欢她。', '2026-04-21 13:00:00'),
(8, 4, 5, '非常感谢刘医生的帮助，效果显著。', '2026-04-20 09:00:00');

-- 为陈医生添加评价 (counselor_id=5)
INSERT INTO `review` (`user_id`, `counselor_id`, `rating`, `content`, `create_time`) VALUES
(9, 5, 4, '陈医生的职场压力管理方法很实用。', '2026-04-22 14:00:00'),
(10, 5, 5, '很专业，咨询后心情好多了。', '2026-04-20 11:00:00');

SELECT '评价表创建完成，测试数据已添加！' AS message;
