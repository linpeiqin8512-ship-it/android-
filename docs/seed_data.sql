-- ============================================================
-- 种子数据脚本：AccountBookDB
-- 1) 创建 8 个测试账户  2) 插入约 300 条模拟账单到这些账户
-- 在 SQL Server Management Studio 或 Azure Data Studio 中执行
-- 连接数据库：AccountBookDB
-- ============================================================

-- 若表中有 monthly_budget 列则使用下面这句；若没有请去掉 , monthly_budget 及对应的值
-- 先插入 8 个用户（role: 0=管理员 1=家长 2=子女；parent_id: 子女绑定家长）
INSERT INTO Users (username, password, nickname, role, parent_id)
VALUES
  ('demo_admin', '123456', '管理员', 0, 0),
  ('demo_baba', '123456', '爸爸', 1, 0),
  ('demo_mama', '123456', '妈妈', 1, 0),
  ('demo_xiaoming', '123456', '小明', 2, 0),
  ('demo_xiaohong', '123456', '小红', 2, 0),
  ('demo_xiaogang', '123456', '小刚', 2, 0),
  ('demo_xiaofang', '123456', '小芳', 2, 0),
  ('demo_xiaohua', '123456', '小华', 2, 0);

-- 若有 monthly_budget 列，可执行下面更新（需先插入用户后再执行）
-- UPDATE Users SET monthly_budget = 10000 WHERE username LIKE 'demo_%';

-- 取刚插入的 8 个用户的 user_id（用于插入 Records）
DECLARE @uid1 INT = (SELECT user_id FROM Users WHERE username = 'demo_admin');
DECLARE @uid2 INT = (SELECT user_id FROM Users WHERE username = 'demo_baba');
DECLARE @uid3 INT = (SELECT user_id FROM Users WHERE username = 'demo_mama');
DECLARE @uid4 INT = (SELECT user_id FROM Users WHERE username = 'demo_xiaoming');
DECLARE @uid5 INT = (SELECT user_id FROM Users WHERE username = 'demo_xiaohong');
DECLARE @uid6 INT = (SELECT user_id FROM Users WHERE username = 'demo_xiaogang');
DECLARE @uid7 INT = (SELECT user_id FROM Users WHERE username = 'demo_xiaofang');
DECLARE @uid8 INT = (SELECT user_id FROM Users WHERE username = 'demo_xiaohua');

-- 可选：为子女绑定家长（小明、小红 -> 爸爸；小刚、小芳、小华 -> 妈妈）
UPDATE Users SET parent_id = @uid2 WHERE username IN ('demo_xiaoming','demo_xiaohong');
UPDATE Users SET parent_id = @uid3 WHERE username IN ('demo_xiaogang','demo_xiaofang','demo_xiaohua');

-- ========== 插入约 300 条账单（支出/收入，分布在 8 个用户、多个月份）==========
-- 格式：user_id, amount, type_name, category, remark, record_date

INSERT INTO Records (user_id, amount, type_name, category, remark, record_date) VALUES
(@uid1, -28.50, '支出', '餐饮', '早餐', '2025-01-02'), (@uid1, 8500, '收入', '工资', '月薪', '2025-01-05'),
(@uid2, -45, '支出', '餐饮', '午餐', '2025-01-03'), (@uid2, 12000, '收入', '工资', '月薪', '2025-01-06'),
(@uid3, -15, '支出', '交通', '地铁', '2025-01-04'), (@uid3, 9800, '收入', '工资', '月薪', '2025-01-07'),
(@uid4, -10, '支出', '餐饮', '奶茶', '2025-01-08'), (@uid5, -66, '支出', '餐饮', '聚餐', '2025-01-09'),
(@uid6, -199, '支出', '购物', '日用品', '2025-01-10'), (@uid7, -32, '支出', '交通', '打车', '2025-01-11'),
(@uid8, -88, '支出', '娱乐', '电影', '2025-01-12'), (@uid1, -120, '支出', '购物', '超市', '2025-01-13'),
(@uid2, -55, '支出', '餐饮', '晚餐', '2025-01-14'), (@uid3, -22, '支出', '交通', '公交', '2025-01-15'),
(@uid4, -8, '支出', '餐饮', '零食', '2025-01-16'), (@uid5, -300, '支出', '购物', '衣服', '2025-01-17'),
(@uid6, -18, '支出', '餐饮', '咖啡', '2025-01-18'), (@uid7, -42, '支出', '餐饮', '外卖', '2025-01-19'),
(@uid8, -25, '支出', '交通', '共享单车', '2025-01-20'), (@uid1, -76, '支出', '餐饮', '聚餐', '2025-01-21'),
(@uid2, -200, '支出', '购物', '家电', '2025-01-22'), (@uid3, -35, '支出', '餐饮', '下午茶', '2025-01-23'),
(@uid4, -12, '支出', '娱乐', '游戏', '2025-01-24'), (@uid5, -90, '支出', '餐饮', '火锅', '2025-01-25'),
(@uid6, -50, '支出', '交通', '加油', '2025-01-26'), (@uid7, -28, '支出', '餐饮', '早餐', '2025-01-27'),
(@uid8, -158, '支出', '购物', '书籍', '2025-01-28'), (@uid1, -38, '支出', '餐饮', '午餐', '2025-01-29'),
(@uid2, -15, '支出', '交通', '停车', '2025-01-30'), (@uid3, -68, '支出', '餐饮', '家庭餐', '2025-01-31'),
(@uid4, -22, '支出', '餐饮', '奶茶', '2025-02-01'), (@uid5, -45, '支出', '交通', '地铁月卡', '2025-02-02'),
(@uid6, -99, '支出', '娱乐', 'K歌', '2025-02-03'), (@uid7, -33, '支出', '餐饮', '外卖', '2025-02-04'),
(@uid8, -11, '支出', '餐饮', '零食', '2025-02-05'), (@uid1, 8500, '收入', '工资', '月薪', '2025-02-06'),
(@uid2, 12000, '收入', '工资', '月薪', '2025-02-07'), (@uid3, 9800, '收入', '工资', '月薪', '2025-02-08'),
(@uid4, -19, '支出', '餐饮', '咖啡', '2025-02-09'), (@uid5, -77, '支出', '餐饮', '聚餐', '2025-02-10'),
(@uid6, -210, '支出', '购物', '数码', '2025-02-11'), (@uid7, -40, '支出', '交通', '打车', '2025-02-12'),
(@uid8, -56, '支出', '餐饮', '晚餐', '2025-02-13'), (@uid1, -95, '支出', '购物', '超市', '2025-02-14'),
(@uid2, -62, '支出', '餐饮', '情人节餐', '2025-02-14'), (@uid3, -28, '支出', '交通', '地铁', '2025-02-15'),
(@uid4, -14, '支出', '娱乐', '电影', '2025-02-16'), (@uid5, -180, '支出', '购物', '鞋', '2025-02-17'),
(@uid6, -23, '支出', '餐饮', '早餐', '2025-02-18'), (@uid7, -48, '支出', '餐饮', '午餐', '2025-02-19'),
(@uid8, -31, '支出', '交通', '公交', '2025-02-20'), (@uid1, -72, '支出', '餐饮', '外卖', '2025-02-21'),
(@uid2, -150, '支出', '购物', '日用品', '2025-02-22'), (@uid3, -36, '支出', '餐饮', '下午茶', '2025-02-23'),
(@uid4, -9, '支出', '餐饮', '奶茶', '2025-02-24'), (@uid5, -52, '支出', '交通', '加油', '2025-02-25'),
(@uid6, -44, '支出', '餐饮', '聚餐', '2025-02-26'), (@uid7, -105, '支出', '娱乐', '健身', '2025-02-27'),
(@uid8, -27, '支出', '餐饮', '零食', '2025-02-28'), (@uid1, -58, '支出', '餐饮', '午餐', '2025-03-01'),
(@uid2, -38, '支出', '交通', '打车', '2025-03-02'), (@uid3, -82, '支出', '餐饮', '家庭餐', '2025-03-03'),
(@uid4, -16, '支出', '娱乐', '游戏', '2025-03-04'), (@uid5, -126, '支出', '购物', '衣服', '2025-03-05'),
(@uid6, -21, '支出', '餐饮', '咖啡', '2025-03-06'), (@uid7, -64, '支出', '餐饮', '火锅', '2025-03-07'),
(@uid8, -43, '支出', '交通', '地铁', '2025-03-08'), (@uid1, 8500, '收入', '工资', '月薪', '2025-03-10'),
(@uid2, 12000, '收入', '工资', '月薪', '2025-03-11'), (@uid3, 9800, '收入', '工资', '月薪', '2025-03-12'),
(@uid4, -10, '支出', '餐饮', '奶茶', '2025-03-13'), (@uid5, -29, '支出', '餐饮', '早餐', '2025-03-13'),
(@uid6, -55, '支出', '交通', '公交', '2025-03-13'), (@uid7, -19, '支出', '餐饮', '零食', '2025-03-13'),
(@uid8, -70, '支出', '购物', '日用品', '2025-03-13');

-- 继续追加约 200 条（用数字表 × 8 用户，总账单约 300 条）
;WITH n(n) AS (SELECT 1 UNION ALL SELECT n+1 FROM n WHERE n < 26)
INSERT INTO Records (user_id, amount, type_name, category, remark, record_date)
SELECT u.user_id,
  -ABS(CHECKSUM(NEWID()) % 140 + 10),
  '支出',
  CASE (ABS(CHECKSUM(NEWID())) % 5) WHEN 0 THEN N'餐饮' WHEN 1 THEN N'交通' WHEN 2 THEN N'购物' WHEN 3 THEN N'娱乐' ELSE N'其他' END,
  N'模拟',
  CONVERT(VARCHAR(10), DATEADD(day, -n*3, '2025-03-13'), 23)
FROM (SELECT user_id FROM Users WHERE username LIKE 'demo_%') u
CROSS JOIN n;

-- 验证
-- SELECT COUNT(*) AS user_count FROM Users WHERE username LIKE 'demo_%';
-- SELECT COUNT(*) AS record_count FROM Records WHERE user_id IN (SELECT user_id FROM Users WHERE username LIKE 'demo_%');
