USE psychology_db;

-- 抑郁自评量表(SDS) test_id=1
INSERT INTO test_question (id, test_id, question_text, sort_order) VALUES
(1,1,'我感到情绪低落',1),(2,1,'我对未来感到悲观',2),(3,1,'我觉得自己没有价值',3),
(4,1,'我感到疲倦乏力',4),(5,1,'我的食欲减退',5),(6,1,'我难以入睡',6),
(7,1,'我觉得生活没有意义',7),(8,1,'我注意力难以集中',8),(9,1,'我感到自责',9),
(10,1,'我有自杀念头',10);

-- 焦虑自评量表(SAS) test_id=2
INSERT INTO test_question (id, test_id, question_text, sort_order) VALUES
(11,2,'我感到紧张不安',1),(12,2,'我容易感到疲劳',2),(13,2,'我难以放松',3),
(14,2,'我感到心慌',4),(15,2,'我有头晕症状',5),(16,2,'我感到胸闷',6),
(17,2,'我容易受惊',7),(18,2,'我入睡困难',8),(19,2,'我感到害怕',9),
(20,2,'我有强迫行为',10);

-- 压力测试 test_id=3
INSERT INTO test_question (id, test_id, question_text, sort_order) VALUES
(21,3,'我感到工作压力大',1),(22,3,'我经常加班',2),(23,3,'我感到时间不够用',3),
(24,3,'我难以平衡工作与生活',4),(25,3,'我感到责任重大',5),(26,3,'我担心失业',6),
(27,3,'我感到竞争压力',7),(28,3,'我经常感到焦虑',8),(29,3,'我睡眠质量下降',9),
(30,3,'我感到身心疲惫',10);

-- 情绪管理测试 test_id=4
INSERT INTO test_question (id, test_id, question_text, sort_order) VALUES
(31,4,'我能控制自己的情绪',1),(32,4,'我能表达自己的感受',2),(33,4,'我能应对负面情绪',3),
(34,4,'我善于倾听他人',4),(35,4,'我能调节自己的情绪',5),(36,4,'我能理解他人的感受',6),
(37,4,'我能保持冷静',7),(38,4,'我能处理冲突',8),(39,4,'我能自我激励',9),
(40,4,'我能感恩生活',10);

-- 睡眠质量测试 test_id=5
INSERT INTO test_question (id, test_id, question_text, sort_order) VALUES
(41,5,'我能快速入睡',1),(42,5,'我睡眠深沉',2),(43,5,'我不会半夜醒来',3),
(44,5,'我早晨醒来感到精力充沛',4),(45,5,'我睡眠规律',5),(46,5,'我没有失眠困扰',6),
(47,5,'我睡前能放松',7),(48,5,'我的睡眠环境舒适',8),(49,5,'我不会做噩梦',9),
(50,5,'我每天睡眠充足',10);

-- 人际关系测试 test_id=6
INSERT INTO test_question (id, test_id, question_text, sort_order) VALUES
(51,6,'我善于与人沟通',1),(52,6,'我有良好的人际关系',2),(53,6,'我能建立信任',3),
(54,6,'我能处理人际冲突',4),(55,6,'我乐于助人',5),(56,6,'我善于团队合作',6),
(57,6,'我能表达感谢',7),(58,6,'我能接受批评',8),(59,6,'我能尊重他人',9),
(60,6,'我有良好的社交圈',10);

-- 所有题目统一5个选项(完全不符合/不太符合/一般/比较符合/完全符合)，分数1-5
INSERT INTO test_option (question_id, option_order, option_text, score)
SELECT q.id, o.n, o.txt, o.n
FROM test_question q
CROSS JOIN (
  SELECT 1 as n, '完全不符合' as txt UNION ALL
  SELECT 2, '不太符合' UNION ALL
  SELECT 3, '一般' UNION ALL
  SELECT 4, '比较符合' UNION ALL
  SELECT 5, '完全符合'
) o;

SELECT CONCAT('测评数据初始化完成！共 ', COUNT(*), ' 题，', (SELECT COUNT(*) FROM test_option), ' 个选项') AS result FROM test_question;
