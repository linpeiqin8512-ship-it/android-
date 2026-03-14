# 给 Web 端 AI 的指令（直接复制整段发给 Web 端）

---

请按以下要求实现/调整 Web 后端，与 Android 家庭记账 App 共用同一套数据库和「方案 C：统一配置源」。

## 1. 数据库连接（必须可配置，禁止写死）

- **统一配置源（与 Android 一致）**：后端启动时请求下面这个 URL 获取数据库连接信息，用返回的 JSON 拼连接字符串；若请求失败则回退到环境变量。
  - **配置 URL**：  
    `https://gist.githubusercontent.com/linpeiqin8512-ship-it/ce6d236c694b44d79e108ef85fa54a16/raw/090ae496648aefd1346dda92453a6f8aab747354/db_config.json`
  - **当前返回格式**：`{"host":"3.tcp.cpolar.top","port":12345}`（可能只有 host 和 port）
  - **约定**：若 JSON 中缺少 `dbName`、`user`、`password`，请使用默认值：库名 `AccountBookDB`，用户名 `sa`，密码 `123456`。
- **同时支持环境变量**（生产建议）：`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD`。若已配置环境变量，可优先用环境变量，未配置时再请求上述配置 URL。

## 2. 数据库与表结构（与 Android 完全一致）

- **数据库**：SQL Server，库名 `AccountBookDB`。
- **表与字段**：严格按本仓库文档，不要自创表名或列名。请阅读并遵守：
  - **`docs/ANDROID_DB_REFERENCE.md`**：表名 `Records`、`Users`，列名 `record_id`、`user_id`、`amount`、`type_name`、`category`、`remark`、`record_date` 等，以及 INSERT/查询示例。
  - **`docs/WEB_BACKEND_SETUP.md`**：Web 对接要点与连接方式说明。

## 3. 协同与后续维护

- Android 与 Web 共用同一配置 URL。cpolar 隧道换地址时，**只需修改 Gist 里 JSON 的 `host` 和 `port`**，两端下次启动/拉取都会用新地址，无需各自改代码。
- 生产环境请用环境变量或密钥管理存密码，不要将带密码的 JSON 或 `.env` 提交到公开仓库。

请根据当前 Web 技术栈（如 Node/Java/Python 等）实现：启动时按上述规则解析配置 URL 或环境变量 → 连接 SQL Server → 对 `Records`、`Users` 等表的增删改查与文档一致。
