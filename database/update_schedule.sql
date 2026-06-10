USE psychology_db;

-- 删除现有的排班数据
DELETE FROM schedule;

-- 重新插入2026年5月份的排班数据

-- 李医生的排班（2026年5月）
INSERT INTO schedule (counselor_id, date, start_time, end_time, max_appointments, current_appointments, status, is_available) VALUES
(1, '2026-05-03', '09:00:00', '12:00:00', 5, 0, 'available', 1),
(1, '2026-05-03', '14:00:00', '18:00:00', 5, 0, 'available', 1),
(1, '2026-05-04', '09:00:00', '12:00:00', 5, 0, 'available', 1),
(1, '2026-05-04', '14:00:00', '18:00:00', 5, 0, 'available', 1),
(1, '2026-05-05', '09:00:00', '12:00:00', 5, 0, 'available', 1),
(1, '2026-05-06', '14:00:00', '18:00:00', 5, 0, 'available', 1),
(1, '2026-05-07', '09:00:00', '12:00:00', 5, 0, 'available', 1);

-- 王医生的排班（2026年5月）
INSERT INTO schedule (counselor_id, date, start_time, end_time, max_appointments, current_appointments, status, is_available) VALUES
(2, '2026-05-03', '10:00:00', '13:00:00', 5, 0, 'available', 1),
(2, '2026-05-03', '15:00:00', '19:00:00', 5, 0, 'available', 1),
(2, '2026-05-04', '09:00:00', '12:00:00', 5, 0, 'available', 1),
(2, '2026-05-05', '14:00:00', '18:00:00', 5, 0, 'available', 1),
(2, '2026-05-06', '09:00:00', '12:00:00', 5, 0, 'available', 1),
(2, '2026-05-07', '14:00:00', '18:00:00', 5, 0, 'available', 1);

-- 张医生的排班（2026年5月）
INSERT INTO schedule (counselor_id, date, start_time, end_time, max_appointments, current_appointments, status, is_available) VALUES
(3, '2026-05-03', '09:00:00', '12:00:00', 5, 0, 'available', 1),
(3, '2026-05-03', '14:00:00', '17:00:00', 5, 0, 'available', 1),
(3, '2026-05-04', '09:00:00', '12:00:00', 5, 0, 'available', 1),
(3, '2026-05-05', '14:00:00', '18:00:00', 5, 0, 'available', 1),
(3, '2026-05-06', '10:00:00', '13:00:00', 5, 0, 'available', 1);

-- 刘医生的排班（2026年5月）
INSERT INTO schedule (counselor_id, date, start_time, end_time, max_appointments, current_appointments, status, is_available) VALUES
(4, '2026-05-03', '14:00:00', '18:00:00', 5, 0, 'available', 1),
(4, '2026-05-04', '09:00:00', '12:00:00', 5, 0, 'available', 1),
(4, '2026-05-04', '14:00:00', '17:00:00', 5, 0, 'available', 1),
(4, '2026-05-05', '10:00:00', '14:00:00', 5, 0, 'available', 1),
(4, '2026-05-06', '09:00:00', '12:00:00', 5, 0, 'available', 1);

-- 陈医生的排班（2026年5月）
INSERT INTO schedule (counselor_id, date, start_time, end_time, max_appointments, current_appointments, status, is_available) VALUES
(5, '2026-05-03', '09:00:00', '12:00:00', 5, 0, 'available', 1),
(5, '2026-05-03', '14:00:00', '18:00:00', 5, 0, 'available', 1),
(5, '2026-05-04', '10:00:00', '13:00:00', 5, 0, 'available', 1),
(5, '2026-05-05', '09:00:00', '12:00:00', 5, 0, 'available', 1),
(5, '2026-05-06', '14:00:00', '18:00:00', 5, 0, 'available', 1),
(5, '2026-05-07', '09:00:00', '12:00:00', 5, 0, 'available', 1);

-- 查看更新后的排班数据
SELECT s.id, c.name as counselor_name, s.date, s.start_time, s.end_time, s.is_available
FROM schedule s
LEFT JOIN counselor c ON s.counselor_id = c.id
ORDER BY s.date, c.id;

SELECT '排班数据已更新为2026年5月份！' as message;
