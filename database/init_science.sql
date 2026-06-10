USE psychology_db;

-- 科普分类表
DROP TABLE IF EXISTS `science_category`;
CREATE TABLE `science_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '图标',
  `sort` INT DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科普分类表';

-- 科普文章表
DROP TABLE IF EXISTS `science_article`;
CREATE TABLE `science_article` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '摘要',
  `content` TEXT COMMENT '内容',
  `cover` VARCHAR(255) DEFAULT NULL COMMENT '封面',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科普文章表';

-- 插入分类数据
INSERT INTO `science_category` (`name`, `icon`, `sort`) VALUES
('心理健康', '💖', 1),
('情绪管理', '😊', 2),
('压力释放', '🧘', 3),
('睡眠改善', '😴', 4),
('人际关系', '👥', 5);

-- 插入文章数据
INSERT INTO `science_article` (`category_id`, `title`, `summary`, `content`, `view_count`, `like_count`) VALUES
(1, '如何保持积极心态', '在日常生活中，保持积极心态对心理健康至关重要。本文将介绍一些实用的方法...', '保持积极心态的方法：\n1. 每天记录三件开心的事\n2. 与积极向上的人为伍\n3. 学会感恩\n4. 保持规律的生活作息', 120, 35),
(1, '认识抑郁症的早期信号', '了解抑郁症的早期症状，有助于及时发现和干预...', '抑郁症的早期信号：\n1. 持续的情绪低落\n2. 对事物失去兴趣\n3. 睡眠和食欲改变\n4. 疲劳和精力不足', 89, 28),
(2, '如何有效管理愤怒情绪', '愤怒是一种正常的情绪，但需要适当管理。本文将介绍几种控制愤怒的方法...', '管理愤怒的技巧：\n1. 深呼吸\n2. 暂时离开现场\n3. 表达自己的感受\n4. 寻求专业帮助', 156, 42),
(2, '认识焦虑症并学会应对', '焦虑症是常见的心理障碍，但可以通过正确的方法应对...', '应对焦虑的方法：\n1. 规律运动\n2. 学习放松技巧\n3. 保持积极的思维方式\n4. 必要时寻求专业帮助', 234, 67),
(3, '工作压力太大怎么办', '现代社会工作压力大，学会释放压力对身心健康很重要...', '释放压力的方法：\n1. 运动健身\n2. 冥想放松\n3. 培养兴趣爱好\n4. 与朋友家人交流', 345, 89),
(3, '5分钟快速减压法', '短时间内快速缓解压力的实用技巧...', '快速减压技巧：\n1. 478呼吸法\n2. 渐进式肌肉放松\n3. 正念冥想\n4. 听轻音乐', 189, 56),
(4, '改善睡眠质量的10个建议', '好的睡眠对身心健康至关重要。本文将分享改善睡眠的实用建议...', '改善睡眠的建议：\n1. 保持规律的作息\n2. 创造舒适的睡眠环境\n3. 避免晚上摄入咖啡因\n4. 睡前做放松活动', 456, 123),
(4, '认识失眠及应对策略', '失眠是常见的睡眠问题，但可以通过科学的方法改善...', '应对失眠的策略：\n1. 建立规律的睡眠习惯\n2. 避免白天过度睡眠\n3. 保持适当的运动\n4. 必要时就医', 234, 78),
(5, '如何建立良好的人际关系', '良好的人际关系对心理健康很重要。本文将分享一些实用建议...', '建立良好人际关系的建议：\n1. 学会倾听\n2. 真诚待人\n3. 保持适当距离\n4. 尊重他人的边界', 312, 94),
(5, '如何有效沟通', '有效的沟通是人际关系的关键。本文将介绍一些沟通技巧...', '有效沟通技巧：\n1. 积极倾听\n2. 表达清晰\n3. 保持尊重\n4. 学会妥协', 278, 67);

-- 验证
SELECT '科普数据初始化完成！' AS message;
SELECT * FROM science_category;
SELECT COUNT(*) AS article_count FROM science_article;
