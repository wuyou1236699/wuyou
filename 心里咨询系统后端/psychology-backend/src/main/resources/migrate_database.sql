-- 心理咨询系统数据库迁移脚本
-- 为咨询师表添加用户名和密码字段
-- 使用前请先备份数据库！

USE psychology_db;

-- 1. 为咨询师表添加字段
ALTER TABLE `counselor` 
ADD COLUMN `username` VARCHAR(50) NOT NULL COMMENT '用户名' AFTER `id`,
ADD COLUMN `password` VARCHAR(100) NOT NULL COMMENT '密码' AFTER `username`,
ADD UNIQUE KEY `uk_username` (`username`);

-- 2. 更新现有咨询师数据
UPDATE `counselor` SET username = 'counselor1', password = '123456' WHERE id = 1;
UPDATE `counselor` SET username = 'counselor2', password = '123456' WHERE id = 2;
UPDATE `counselor` SET username = 'counselor3', password = '123456' WHERE id = 3;
UPDATE `counselor` SET username = 'counselor4', password = '123456' WHERE id = 4;
UPDATE `counselor` SET username = 'counselor5', password = '123456' WHERE id = 5;

-- 验证
SELECT '数据库迁移完成！' AS message;
SELECT id, username, name FROM counselor;
