# Android 端账单表与连接配置（供 Web 端对照）

请确保 Web 后端访问**同一数据库、同一张表、同一套字段**。以下为 Android 端实际使用的配置与代码。

---

## 一、数据库连接配置

| 项 | 值 |
|----|-----|
| 驱动 | jTDS: `net.sourceforge.jtds.jdbc.Driver` |
| JDBC URL | `jdbc:jtds:sqlserver://15.tcp.cpolar.top:11573/AccountBookDB` |
| 库名 | **AccountBookDB** |
| 主机 | 15.tcp.cpolar.top |
| 端口 | 11573 |
| 用户 | sa |
| 密码 | 123456 |

**说明**：Android 用 jTDS 连 SQL Server。Web 若用 ODBC/官方 SQL Server 驱动，URL 可能为：  
`jdbc:sqlserver://15.tcp.cpolar.top:11573;databaseName=AccountBookDB`  
只要连到同一个库 **AccountBookDB** 即可。

---

## 二、表名与列名（必须一致）

| 表名 | **Records**（注意首字母大写） |

| 列名（数据库） | 类型 | 说明 |
|----------------|------|------|
| record_id | int (PK, 自增) | 主键，插入时可不传 |
| user_id | int | 用户 ID |
| amount | double/float | 金额（支出为负数，如 -10.00） |
| type_name | varchar/nvarchar | 固定为 **"支出"** 或 **"收入"** |
| category | varchar/nvarchar | 分类，如 餐饮、交通 |
| remark | varchar/nvarchar | 备注 |
| record_date | date/datetime | 记账日期，Android 传字符串如 "2026-03-13" |

---

## 三、Record 实体（Java）与数据库列对应

```java
// 表: Records
// Java 属性 -> 数据库列
private int id;             // record_id  (主键)
private int userId;         // user_id
private double amount;      // amount
private String typeName;    // type_name  ("支出" | "收入")
private String category;    // category
private String remark;      // remark
private String date;        // record_date  (String 存，如 "2026-03-13")
```

---

## 四、插入账单的代码（记一笔支出/收入）

**INSERT 语句（与 addRecord 使用的一致）：**

```sql
INSERT INTO Records (user_id, amount, type_name, category, remark, record_date)
VALUES (?, ?, ?, ?, ?, ?)
```

**参数顺序与类型：**

| 序号 | 参数 | Java 类型 | 说明 |
|------|------|-----------|------|
| 1 | user_id | int | 当前用户 ID |
| 2 | amount | double | 支出为负数，如 -10.00；收入为正数 |
| 3 | type_name | String | "支出" 或 "收入" |
| 4 | category | String | 分类名 |
| 5 | remark | String | 备注 |
| 6 | record_date | String | 日期 "yyyy-MM-dd" |

**Android addRecord 方法（核心逻辑）：**

```java
public boolean addRecord(Record record) {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
        conn = JDBCUtils.getConn();  // 使用上面同一套连接配置
        if (conn == null) return false;
        String sql = "INSERT INTO Records (user_id, amount, type_name, category, remark, record_date) VALUES (?, ?, ?, ?, ?, ?)";
        ps = conn.prepareStatement(sql);
        ps.setInt(1, record.getUserId());
        ps.setDouble(2, record.getAmount());
        ps.setString(3, record.getTypeName());
        ps.setString(4, record.getCategory());
        ps.setString(5, record.getRemark());
        ps.setString(6, record.getDate());
        return ps.executeUpdate() > 0;
    } catch (Exception e) { e.printStackTrace(); return false; }
    finally { JDBCUtils.close(conn); }
}
```

---

## 五、查询时使用的列名（Web 列表/统计可参考）

Android 查询单条记录时使用的列名：

- `record_id`, `user_id`, `amount`, `type_name`, `category`, `remark`, `record_date`

例如「按月份查」的 SQL：

```sql
SELECT * FROM Records
WHERE user_id = ? AND YEAR(record_date) = ? AND MONTH(record_date) = ?
ORDER BY record_date DESC
```

Web 端「全站最新 10 条」可类似：

```sql
SELECT TOP 10 record_id, user_id, amount, type_name, category, remark, record_date
FROM Records
ORDER BY record_date DESC
```

---

## 六、对照检查清单（给 Web 端）

- [ ] 连接的是同一个库 **AccountBookDB**
- [ ] 表名是 **Records**（不是 record、tb_record 等）
- [ ] 列名：**record_id, user_id, amount, type_name, category, remark, record_date**
- [ ] type_name 取值是中文 **"支出"** / **"收入"**
- [ ] amount：支出为负数，收入为正数
- [ ] record_date 为日期类型，与 Android 传入的 "yyyy-MM-dd" 兼容

若 Web 的 RecordMapper / 实体 / 表名或列名与上述任一项不一致，请改为与本文档一致后再查数据。
