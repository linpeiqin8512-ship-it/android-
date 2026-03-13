# 种子数据导入说明

本目录下的 **`seed_data.sql`** 用于向 **AccountBookDB** 一次性插入：

- **8 个测试账户**（1 管理员 + 2 家长 + 5 子女，含绑定关系）
- **约 300 条模拟账单**（支出/收入，分布在 2025-01～2025-03，归属上述 8 个用户）

Android 与 Web 只要连接同一库 **AccountBookDB**，即可看到同一批用户和账单。

---

## 方式一：在 SQL Server 里执行（推荐）

1. 用 **SQL Server Management Studio** 或 **Azure Data Studio** 连接你的数据库。
2. 确保当前库为 **AccountBookDB**（与 Android 里 `JDBCUtils` 配置一致）。
3. 打开 `seed_data.sql`，全选后执行（F5）。
4. 若无报错，可执行脚本末尾的验证查询：
   ```sql
   SELECT COUNT(*) AS user_count FROM Users WHERE username LIKE 'demo_%';
   SELECT COUNT(*) AS record_count FROM Records WHERE user_id IN (SELECT user_id FROM Users WHERE username LIKE 'demo_%');
   ```
   - `user_count` 应为 8，`record_count` 约 300。

**注意：**

- 若 **Users** 表没有 **monthly_budget** 列，脚本已按“只插 5 列”编写，无需改。
- 若已有 **monthly_budget** 列，可在执行完用户插入后单独执行：  
  `UPDATE Users SET monthly_budget = 10000 WHERE username LIKE 'demo_%';`
- 种子账户统一使用 **demo_** 前缀（如 `demo_baba`、`demo_mama`），避免与现有账号（如 `mama`、`papa`）重复。若仍报唯一约束，说明库里已有 `demo_xxx`，请先删除或改名后再执行。

---

## 方式二：用 Android 端 JDBC 执行（可选）

若你更希望用当前项目里的 JDBC 连接执行同一套逻辑，可以：

1. 在工程里新增一个“开发者/测试”入口（例如在 **我的** 页长按某处，或通过 Build 类型判断）。
2. 在该入口里读取 `docs/seed_data.sql` 文件内容，按 `;` 拆成多条 SQL，逐条用 `JDBCUtils.getConn()` 得到的 `Connection` 执行。
3. 注意：脚本里用了 `DECLARE @uid1 ...` 等变量，在 JDBC 里不能直接执行整段，需要拆成“先插用户 → 查 user_id → 再插 Records”的逻辑，或直接复用 **方式一** 在 SSMS 里跑一遍。

推荐优先用 **方式一**，简单且与数据库类型、驱动一致。

---

## 种子账户一览（执行脚本后）

所有登录名带 **demo_** 前缀，避免与现有用户（如 mama、papa）冲突；昵称仍为中文，界面显示不变。

| 用户名        | 密码   | 昵称   | 角色     | 说明           |
|---------------|--------|--------|----------|----------------|
| demo_admin    | 123456 | 管理员 | 管理员(0) | 可做成员重置等 |
| demo_baba     | 123456 | 爸爸   | 家长(1)   | 小明、小红的家长 |
| demo_mama     | 123456 | 妈妈   | 家长(1)   | 小刚、小芳、小华的家长 |
| demo_xiaoming | 123456 | 小明   | 子女(2)   | parent_id → 爸爸 |
| demo_xiaohong | 123456 | 小红   | 子女(2)   | parent_id → 爸爸 |
| demo_xiaogang | 123456 | 小刚   | 子女(2)   | parent_id → 妈妈 |
| demo_xiaofang | 123456 | 小芳   | 子女(2)   | parent_id → 妈妈 |
| demo_xiaohua  | 123456 | 小华   | 子女(2)   | parent_id → 妈妈 |

账单分布在 **2025-01** 至 **2025-03**，类型为 **支出**（少量 **收入** 如工资），分类含餐饮、交通、购物、娱乐等，已随机分配到上述 8 个账户。
