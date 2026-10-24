-- 追加测试数据脚本
-- 让用户数量达到50，咨询师数量达到10

USE psychology_db;

-- 插入更多咨询师（当前5条，再加5条）
INSERT INTO `counselor` (id, username, password, name, gender, qualification, expertise, profile, avatar, status) VALUES
(6, 'counselor6', '123456', '赵医生', 1, '国家二级心理咨询师', '情绪管理、压力疏导', '专注于职场压力和情绪管理咨询，帮助众多职场人士缓解焦虑和压力。', '/images/default-avatar.png', 1),
(7, 'counselor7', '123456', '孙医生', 2, '资深心理咨询师', '人际关系、社交恐惧', '擅长人际关系改善和社交恐惧治疗，帮助来访者建立健康的社交模式。', '/images/default-avatar.png', 1),
(8, 'counselor8', '123456', '周医生', 1, '高级心理咨询师', '婚姻情感、离婚咨询', '专注婚姻家庭问题咨询，帮助夫妻改善关系，处理离婚后的心理重建。', '/images/default-avatar.png', 1),
(9, 'counselor9', '123456', '吴医生', 2, '国家三级心理咨询师', '学业压力、厌学问题', '专注于青少年学业压力和厌学问题的心理咨询与辅导。', '/images/default-avatar.png', 1),
(10, 'counselor10', '123456', '郑医生', 1, '资深心理咨询师', '睡眠障碍、噩梦处理', '专业治疗失眠和噩梦困扰，采用多种疗法结合的方式，效果显著。', '/images/default-avatar.png', 1);

-- 插入更多用户（当前3条，再加47条）
INSERT INTO `user` (id, openid, nickname, avatar, phone, create_time) VALUES
(4, 'mock_openid_004', '王芳', '/images/default-avatar.png', '13800138004', NOW()),
(5, 'mock_openid_005', '刘洋', '/images/default-avatar.png', '13800138005', NOW()),
(6, 'mock_openid_006', '陈静', '/images/default-avatar.png', '13800138006', NOW()),
(7, 'mock_openid_007', '黄磊', '/images/default-avatar.png', '13800138007', NOW()),
(8, 'mock_openid_008', '赵敏', '/images/default-avatar.png', '13800138008', NOW()),
(9, 'mock_openid_009', '周强', '/images/default-avatar.png', '13800138009', NOW()),
(10, 'mock_openid_010', '吴娟', '/images/default-avatar.png', '13800138010', NOW()),
(11, 'mock_openid_011', '徐涛', '/images/default-avatar.png', '13800138011', NOW()),
(12, 'mock_openid_012', '孙丽', '/images/default-avatar.png', '13800138012', NOW()),
(13, 'mock_openid_013', '马超', '/images/default-avatar.png', '13800138013', NOW()),
(14, 'mock_openid_014', '朱琳', '/images/default-avatar.png', '13800138014', NOW()),
(15, 'mock_openid_015', '胡建', '/images/default-avatar.png', '13800138015', NOW()),
(16, 'mock_openid_016', '林萍', '/images/default-avatar.png', '13800138016', NOW()),
(17, 'mock_openid_017', '郭峰', '/images/default-avatar.png', '13800138017', NOW()),
(18, 'mock_openid_018', '何燕', '/images/default-avatar.png', '13800138018', NOW()),
(19, 'mock_openid_019', '高峰', '/images/default-avatar.png', '13800138019', NOW()),
(20, 'mock_openid_020', '肖娜', '/images/default-avatar.png', '13800138020', NOW()),
(21, 'mock_openid_021', '许刚', '/images/default-avatar.png', '13800138021', NOW()),
(22, 'mock_openid_022', '蒋雪', '/images/default-avatar.png', '13800138022', NOW()),
(23, 'mock_openid_023', '韩磊', '/images/default-avatar.png', '13800138023', NOW()),
(24, 'mock_openid_024', '冯莉', '/images/default-avatar.png', '13800138024', NOW()),
(25, 'mock_openid_025', '于勇', '/images/default-avatar.png', '13800138025', NOW()),
(26, 'mock_openid_026', '董芳', '/images/default-avatar.png', '13800138026', NOW()),
(27, 'mock_openid_027', '梁志', '/images/default-avatar.png', '13800138027', NOW()),
(28, 'mock_openid_028', '沈娟', '/images/default-avatar.png', '13800138028', NOW()),
(29, 'mock_openid_029', '张伟', '/images/default-avatar.png', '13800138029', NOW()),
(30, 'mock_openid_030', '李娜', '/images/default-avatar.png', '13800138030', NOW()),
(31, 'mock_openid_031', '王刚', '/images/default-avatar.png', '13800138031', NOW()),
(32, 'mock_openid_032', '李梅', '/images/default-avatar.png', '13800138032', NOW()),
(33, 'mock_openid_033', '刘强', '/images/default-avatar.png', '13800138033', NOW()),
(34, 'mock_openid_034', '陈霞', '/images/default-avatar.png', '13800138034', NOW()),
(35, 'mock_openid_035', '杨帆', '/images/default-avatar.png', '13800138035', NOW()),
(36, 'mock_openid_036', '周莉', '/images/default-avatar.png', '13800138036', NOW()),
(37, 'mock_openid_037', '吴昊', '/images/default-avatar.png', '13800138037', NOW()),
(38, 'mock_openid_038', '郑敏', '/images/default-avatar.png', '13800138038', NOW()),
(39, 'mock_openid_039', '徐鹏', '/images/default-avatar.png', '13800138039', NOW()),
(40, 'mock_openid_040', '孙丹', '/images/default-avatar.png', '13800138040', NOW()),
(41, 'mock_openid_041', '马超', '/images/default-avatar.png', '13800138041', NOW()),
(42, 'mock_openid_042', '朱琳', '/images/default-avatar.png', '13800138042', NOW()),
(43, 'mock_openid_043', '胡勇', '/images/default-avatar.png', '13800138043', NOW()),
(44, 'mock_openid_044', '林梅', '/images/default-avatar.png', '13800138044', NOW()),
(45, 'mock_openid_045', '郭涛', '/images/default-avatar.png', '13800138045', NOW()),
(46, 'mock_openid_046', '何丽', '/images/default-avatar.png', '13800138046', NOW()),
(47, 'mock_openid_047', '高峰', '/images/default-avatar.png', '13800138047', NOW()),
(48, 'mock_openid_048', '肖莉', '/images/default-avatar.png', '13800138048', NOW()),
(49, 'mock_openid_049', '许刚', '/images/default-avatar.png', '13800138049', NOW()),
(50, 'mock_openid_050', '蒋华', '/images/default-avatar.png', '13800138050', NOW());

-- 验证数据
SELECT CONCAT('用户数量: ', COUNT(*)) AS info FROM `user`;
SELECT CONCAT('咨询师数量: ', COUNT(*)) AS info FROM counselor;
SELECT CONCAT('预约数量: ', COUNT(*)) AS info FROM appointment;
SELECT CONCAT('评价数量: ', COUNT(*)) AS info FROM review;
