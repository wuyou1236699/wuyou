-- 更新咨询师密码为BCrypt加密格式
-- 默认密码：123456 的BCrypt加密值
-- $2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq

USE psychology_db;

-- 更新咨询师密码
UPDATE counselor SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq';

-- 更新管理员密码
UPDATE admin SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq';

-- 更新用户密码（如果需要）
UPDATE user SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq' WHERE password IS NOT NULL AND password != '';

SELECT '密码更新完成！' AS message;