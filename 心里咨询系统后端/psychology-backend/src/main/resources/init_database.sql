-- 心理咨询系统数据库初始化脚本
-- 数据库: psychology_db
-- 创建时间: 2026-04-28

-- ==================== 创建数据库 ====================
CREATE DATABASE IF NOT EXISTS psychology_db
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE psychology_db;

-- ==================== 用户表 ====================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ==================== 咨询师表 ====================
DROP TABLE IF EXISTS `counselor`;
CREATE TABLE `counselor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '咨询师ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `gender` TINYINT DEFAULT NULL COMMENT '性别 1男 2女',
  `qualification` VARCHAR(100) DEFAULT NULL COMMENT '资质',
  `expertise` VARCHAR(200) DEFAULT NULL COMMENT '专长',
  `profile` TEXT COMMENT '简介',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询师表';

-- ==================== 排班表 ====================
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '排班ID',
  `counselor_id` BIGINT NOT NULL COMMENT '咨询师ID',
  `work_date` DATE NOT NULL COMMENT '工作日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `is_available` TINYINT DEFAULT 1 COMMENT '是否可约 1可约 0不可约',
  PRIMARY KEY (`id`),
  KEY `idx_counselor_id` (`counselor_id`),
  KEY `idx_work_date` (`work_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排班表';

-- ==================== 预约表 ====================
DROP TABLE IF EXISTS `appointment`;
CREATE TABLE `appointment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `counselor_id` BIGINT NOT NULL COMMENT '咨询师ID',
  `service_type` TINYINT NOT NULL COMMENT '服务类型 1电话 2网络 3门诊',
  `appointment_time` DATETIME NOT NULL COMMENT '预约时间',
  `problem` TEXT COMMENT '问题描述',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0待确认 1已确认 2已完成 3已取消',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_counselor_id` (`counselor_id`),
  KEY `idx_appointment_time` (`appointment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

-- ==================== 评价表 ====================
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `counselor_id` BIGINT NOT NULL COMMENT '咨询师ID',
  `appointment_id` BIGINT DEFAULT NULL COMMENT '预约ID',
  `rating` TINYINT NOT NULL COMMENT '评分 1-5',
  `content` TEXT COMMENT '评价内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_counselor_id` (`counselor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- ==================== 管理员表 ====================
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
  `role` VARCHAR(20) DEFAULT 'admin' COMMENT '角色',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ==================== 咨询记录表 ====================
DROP TABLE IF EXISTS `consultation_record`;
CREATE TABLE `consultation_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `appointment_id` BIGINT DEFAULT NULL COMMENT '预约ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `counselor_id` BIGINT NOT NULL COMMENT '咨询师ID',
  `problem` TEXT COMMENT '问题描述',
  `diagnosis` TEXT COMMENT '诊断结果',
  `suggestions` TEXT COMMENT '咨询建议',
  `duration` INT DEFAULT NULL COMMENT '咨询时长(分钟)',
  `fee` DECIMAL(10,2) DEFAULT NULL COMMENT '咨询费用',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0草稿 1已完成',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_counselor_id` (`counselor_id`),
  KEY `idx_appointment_id` (`appointment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询记录表';

-- ==================== AI聊天日志表 ====================
DROP TABLE IF EXISTS `ai_chat_log`;
CREATE TABLE `ai_chat_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role` VARCHAR(20) NOT NULL COMMENT '角色 user/ai',
  `content` TEXT NOT NULL COMMENT '内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天日志表';

-- ==================== AI任务表 ====================
DROP TABLE IF EXISTS `ai_task`;
CREATE TABLE `ai_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态',
  `result` TEXT COMMENT '结果',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI任务表';

-- ==================== 插入初始化数据 ====================

-- 咨询师数据
INSERT INTO `counselor` (id, username, password, name, gender, qualification, expertise, profile, avatar, status) VALUES
(1, 'counselor1', '123456', '李医生', 1, '国家二级心理咨询师', '情绪压力、抑郁、焦虑', '专注于情绪压力和抑郁问题的心理咨询，拥有5年临床经验，帮助过数百位来访者走出情绪低谷。', '/images/default-avatar.png', 1),
(2, 'counselor2', '123456', '王医生', 1, '资深心理咨询师', '婚姻家庭、亲子关系', '擅长婚姻家庭咨询和亲子关系调解，拥有10年咨询经验，帮助过上千个家庭重建和谐关系。', '/images/default-avatar.png', 1),
(3, 'counselor3', '123456', '张医生', 2, '心理咨询师', '职业规划、人际关系', '专注于职业规划和人际关系咨询，帮助来访者明确职业方向，改善人际关系。', '/images/default-avatar.png', 1),
(4, 'counselor4', '123456', '刘医生', 2, '国家三级心理咨询师', '青少年心理、学习压力', '擅长青少年心理辅导和学习压力管理，帮助学生建立健康的心理状态。', '/images/default-avatar.png', 1),
(5, 'counselor5', '123456', '陈医生', 1, '高级心理咨询师', '失眠、焦虑症、抑郁症', '专业治疗失眠、焦虑症和抑郁症，采用认知行为疗法，效果显著。', '/images/default-avatar.png', 1);

-- 排班数据
INSERT INTO `schedule` (counselor_id, work_date, start_time, end_time, is_available) VALUES
(1, '2026-04-28', '09:00:00', '12:00:00', 1),
(1, '2026-04-28', '14:00:00', '18:00:00', 1),
(1, '2026-04-29', '09:00:00', '12:00:00', 1),
(1, '2026-04-29', '14:00:00', '18:00:00', 1),
(1, '2026-04-30', '09:00:00', '12:00:00', 1),
(2, '2026-04-28', '10:00:00', '12:00:00', 1),
(2, '2026-04-28', '14:00:00', '17:00:00', 1),
(2, '2026-04-29', '09:00:00', '12:00:00', 1),
(2, '2026-04-30', '14:00:00', '18:00:00', 1),
(3, '2026-04-28', '09:00:00', '12:00:00', 1),
(3, '2026-04-29', '14:00:00', '18:00:00', 1),
(3, '2026-04-30', '10:00:00', '12:00:00', 1),
(4, '2026-04-28', '14:00:00', '18:00:00', 1),
(4, '2026-04-29', '09:00:00', '12:00:00', 1),
(4, '2026-04-30', '14:00:00', '17:00:00', 1),
(5, '2026-04-28', '09:00:00', '12:00:00', 1),
(5, '2026-04-29', '14:00:00', '18:00:00', 1),
(5, '2026-04-30', '10:00:00', '12:00:00', 1);

-- 用户数据
INSERT INTO `user` (id, openid, nickname, avatar, phone, create_time) VALUES
(1, 'mock_openid_001', '张三', '/images/default-avatar.png', '13800138001', NOW()),
(2, 'mock_openid_002', '李四', '/images/default-avatar.png', '13800138002', NOW()),
(3, 'mock_openid_003', '王五', '/images/default-avatar.png', '13800138003', NOW());

-- 预约数据
INSERT INTO `appointment` (id, user_id, counselor_id, service_type, appointment_time, problem, status, create_time) VALUES
(1, 1, 1, 2, '2026-04-28 14:00:00', '失眠问题咨询', 1, NOW()),
(2, 2, 2, 1, '2026-04-29 10:00:00', '焦虑情绪困扰', 0, NOW()),
(3, 3, 3, 3, '2026-04-25 15:00:00', '职业发展咨询', 2, NOW()),
(4, 1, 1, 2, '2026-05-01 09:00:00', '情绪压力管理', 0, NOW()),
(5, 2, 2, 1, '2026-05-02 14:00:00', '亲子关系问题', 0, NOW());

-- 评价数据
INSERT INTO `review` (id, user_id, counselor_id, appointment_id, rating, content, create_time) VALUES
(1, 1, 1, 1, 5, '李医生非常专业，帮助我走出了困境。', '2026-04-20 10:00:00'),
(2, 2, 1, NULL, 5, '很有耐心，咨询效果很好。', '2026-04-18 15:00:00'),
(3, 3, 1, NULL, 4, '态度很好，给了很多有用的建议。', '2026-04-15 09:00:00'),
(4, 1, 2, NULL, 5, '王医生经验丰富，解决了我的家庭矛盾。', '2026-04-22 14:00:00'),
(5, 2, 3, NULL, 4, '张医生很专业，职业规划建议很实用。', '2026-04-19 11:00:00'),
(6, 3, 2, NULL, 5, '非常感谢王医生，家庭关系改善了。', '2026-04-21 16:00:00');

-- 管理员数据
INSERT INTO `admin` (id, username, password, name, role, status, create_time) VALUES
(1, 'admin', 'admin123', '系统管理员', 'admin', 1, NOW());

-- AI聊天日志数据
INSERT INTO `ai_chat_log` (id, user_id, role, content, create_time) VALUES
(1, 1, 'ai', '您好！我是您的AI心理助手。有什么心理方面的困扰可以和我聊聊吗？', NOW()),
(2, 1, 'user', '最近总是失眠，很焦虑。', NOW()),
(3, 1, 'ai', '我理解您的感受。请问这种情况持续多久了？', NOW());

-- ==================== 验证数据 ====================
SELECT '数据库初始化完成！' AS message;
SELECT CONCAT('咨询师数量: ', COUNT(*)) AS info FROM counselor;
SELECT CONCAT('排班数量: ', COUNT(*)) AS info FROM schedule;
SELECT CONCAT('用户数量: ', COUNT(*)) AS info FROM user;
SELECT CONCAT('预约数量: ', COUNT(*)) AS info FROM appointment;
SELECT CONCAT('评价数量: ', COUNT(*)) AS info FROM review;
SELECT CONCAT('管理员数量: ', COUNT(*)) AS info FROM admin;
SELECT CONCAT('咨询记录数量: ', COUNT(*)) AS info FROM consultation_record;
