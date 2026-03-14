# Web 后端对接说明（交给 Web 端 AI / 开发者）

本文档说明 Web 后端如何与 **Android 家庭记账 App** 共用同一数据库，并支持「统一配置源」以便隧道或库地址变更时只改一处。

---

## 一、目标

- Web 与 Android 使用**同一数据库、同一批表**，数据一致。
- **数据库连接必须可配置**（环境变量或统一配置 URL），不要写死 IP/端口，便于 cpolar 隧道换地址或迁库时只改配置。

---

## 二、数据库信息（与 Android 一致）

- **类型**：SQL Server  
- **库名**：`AccountBookDB`  
- **表**：`Users`、`Records` 等，表结构与字段名以本仓库 **`docs/ANDROID_DB_REFERENCE.md`** 为准，不要自创表名或列名。

**连接字符串参考**（按你使用驱动调整）：

- 官方 SQL Server 驱动（如 Node 的 `tedious` / `mssql`、Java 的 `sqljdbc`）：  
  `Server=主机,端口;Database=AccountBookDB;User Id=sa;Password=xxx;`
- JDBC：  
  `jdbc:sqlserver://主机:端口;databaseName=AccountBookDB`

---

## 三、连接配置方式（二选一或同时支持）

### 1. 环境变量（推荐生产）

在 Web 部署环境（如 Vercel、自建服务器、Docker）中配置：

- `DB_HOST`：数据库主机（如 cpolar 公网地址或云库地址）
- `DB_PORT`：端口（如 11573）
- `DB_NAME`：`AccountBookDB`
- `DB_USER`：用户名
- `DB_PASSWORD`：密码

启动时从环境变量读取并拼出连接字符串。**不要**把上述敏感信息写死在代码或提交到公开仓库。

### 2. 统一配置 URL（与 Android 一致）

Android 已支持从「配置 URL」拉取 JSON 并保存连接信息；Web 若使用**同一 URL**，则隧道/库地址变更时只需更新该 URL 指向的内容，Android 与 Web 都能拿到新配置。

- **配置 URL**：例如 GitHub Gist 的 **Raw** 地址（如 `https://gist.githubusercontent.com/用户名/xxx/raw/xxx/db_config.json`）。
- **JSON 格式**（与 Android 约定一致）：

```json
{
  "host": "15.tcp.cpolar.top",
  "port": 11573,
  "dbName": "AccountBookDB",
  "user": "sa",
  "password": "123456"
}
```

- **逻辑**：Web 后端在**启动时**请求该 URL，解析 JSON，用其中的 `host`、`port`、`dbName`、`user`、`password` 拼出连接字符串；若请求失败则回退到环境变量或本地默认配置。
- **安全**：生产环境建议仍以环境变量为主；配置 URL 可用于开发或与 Android 共享同一份“开发/测试”配置，注意不要将带真实密码的 JSON 提交到公开仓库。

---

## 四、表与字段（严格按 Android 约定）

请直接阅读本仓库 **`docs/ANDROID_DB_REFERENCE.md`**，其中包含：

- 表名：`Records`、`Users` 等
- 列名：`record_id`、`user_id`、`amount`、`type_name`、`category`、`remark`、`record_date` 等
- INSERT 示例与查询示例

Web 后端的 **RecordMapper / 实体 / SQL** 必须与上述文档一致，否则与 Android 数据不一致。

---

## 五、小结（给 Web 端 AI 的执行清单）

1. **连接**：使用环境变量 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD` 拼连接字符串；可选地在启动时请求「统一配置 URL」的 JSON，成功则覆盖上述配置（与 Android 共用同一配置源）。
2. **表结构**：严格按 **`docs/ANDROID_DB_REFERENCE.md`** 的表名、列名实现；不要自创字段或表名。
3. **安全**：生产环境用环境变量或密钥管理服务存密码；不要提交 `.env` 或带密码的配置文件到公开仓库。

按以上方式实现后，Web 与 Android 将共用同一数据库；隧道或库地址变更时，只需更新配置源（如 Gist）或环境变量，无需在 JDBC/代码里改公网地址。
