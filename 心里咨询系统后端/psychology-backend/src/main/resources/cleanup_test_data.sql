-- 删除测试咨询师数据
-- 测试医生、123、忧医生 为测试时手动添加

USE psychology_db;

-- 先查看这些咨询师及其关联数据
SELECT id, username, name FROM counselor WHERE name IN ('测试医生', '123', '忧医生');

-- 删除关联的排班数据
DELETE FROM schedule WHERE counselor_id IN (SELECT id FROM counselor WHERE name IN ('测试医生', '123', '忧医生'));

-- 删除关联的预约数据
DELETE FROM appointment WHERE counselor_id IN (SELECT id FROM counselor WHERE name IN ('测试医生', '123', '忧医生'));

-- 删除关联的评价数据
DELETE FROM review WHERE counselor_id IN (SELECT id FROM counselor WHERE name IN ('测试医生', '123', '忧医生'));

-- 删除咨询师
DELETE FROM counselor WHERE name IN ('测试医生', '123', '忧医生');

-- 验证
SELECT '清理完成！剩余咨询师：' AS message;
SELECT id, username, name FROM counselor;
