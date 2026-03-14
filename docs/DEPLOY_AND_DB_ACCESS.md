# 数据库访问方案说明（替代/优化 cpolar 隧道）

当前：用 **cpolar** 打通隧道，Android / Web 通过公网地址（如 `15.tcp.cpolar.top:11573`）访问本机 SQL Server。问题：免费隧道重启后地址可能变，每次都要改 Android 里 JDBC 和 Web 里配置，不方便。

下面给出**可替代方案**和**在保留 cpolar 前提下的自动化手段**，以及 **Web 端对接说明**（可交给 Web 端 AI）。

---

## 一、推荐方案对比

| 方案 | 优点 | 缺点 | 适用 |
|------|------|------|------|
| **A. 云数据库** | 固定地址、稳定、不用开本机隧道 | 需迁库、可能有费用 | 正式用、多端+多人 |
| **B. cpolar 固定域名（付费）** | 继续用本机 DB，地址不再变 | 需 cpolar 付费 | 想保留本机 DB 又不想改代码 |
| **C. 统一配置源（见下）** | 隧道地址只改一处，Android/Web 都从这一处读 | 需维护一个“配置源” | 短期、免费隧道 |

---

## 二、方案 A：迁到云数据库（最省心）

把 SQL Server 迁到云上，或新建一个云上的库，**连接地址固定**，Android 和 Web 都填这个地址，以后不用再改。

- **Azure SQL**、**阿里云 RDS（SQL Server）**、**腾讯云 CDB** 等：创建实例后得到固定主机名，如 `xxx.database.windows.net`。
- 本机用 SSMS 把现有库导出/导入到云库；或云上新建空库，用 `docs/seed_data.sql` 初始化。
- **Android**：在 `JDBCUtils`（或你后来的“配置源”）里把 IP/端口 改成云库的 **主机:端口**。
- **Web**：后端用环境变量配置同一主机、库名、账号密码，与 Android 共用同一库。

这样不再依赖 cpolar，也不用每次改地址。

---

## 三、方案 B：cpolar 固定域名（继续用本机 DB）

- cpolar **付费**一般会提供**固定二级域名**（或固定隧道），重启后地址不变。
- 在 cpolar 后台把「sqlserver」隧道设为固定域名后，把该地址写进 Android 和 Web 的配置里，之后就不用再改。

---

## 四、方案 C：统一配置源（免费隧道 + 少改代码）

思路：**隧道地址只维护在一个地方**，Android 和 Web 都从这一处读取，cpolar 换地址后只改这一处。

### 4.1 配置源选型（任选其一）

1. **GitHub Gist（推荐，免费）**  
   - 建一个 **private Gist**，里面放一个 JSON，例如：  
     `db_config.json`:  
     `{"host":"15.tcp.cpolar.top","port":11573,"dbName":"AccountBookDB","user":"sa","password":"123456"}`  
   - 用 Gist 的 **Raw** 地址（如 `https://gist.githubusercontent.com/你的用户名/xxx/raw/xxx/db_config.json`）作为“配置 URL”。
   - 每次 cpolar 重启、地址变了，只改这个 Gist 里 `host`/`port` 并保存。
   - **Android**：启动时请求该 URL，解析 JSON，把连接信息存到 SharedPreferences；`JDBCUtils` 优先用这里的 host/port，没有再用默认值（见下节）。
   - **Web**：后端启动时请求同一个 URL，用返回的 host/port 做数据库连接；或你手动把 Gist 里内容同步到 Web 的 `.env`。

2. **本机小脚本 + 任意“可被访问的”配置存储**  
   - 写一个脚本（PowerShell/Python），在 **cpolar 启动后** 从 cpolar 的 API 或本地状态里拿到当前「sqlserver」隧道的公网地址。
   - 脚本把 `host`、`port` 写入：  
     - 一个你部署在公网上的**极简接口**（例如只返回 JSON 的一行 PHP/Node 页面），或  
     - 一个 **Gist**（通过 GitHub API 更新 Gist 内容）。  
   - Android / Web 都从这个“配置接口”或 Gist 的 Raw 地址读，逻辑同 1。

3. **仅 Web 用环境变量，Android 用“拉取配置”**  
   - Web 部署时（如 Vercel / 自建服务器）在 **环境变量** 里配置：  
     `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD`。  
   - 你每次 cpolar 换地址后，只改服务器上的环境变量并重启/重新部署 Web。  
   - Android 仍用“从 URL 拉取配置”的方式，URL 可以指向你 Web 后端提供的一个 **/api/config**（只返回 DB 连接信息，且可做权限控制），这样仍是“只改一处”（Web 环境变量或该接口的数据源）。

### 4.2 Android 端已做的支持（本仓库）

- 已增加**从“配置源”读取连接信息**的逻辑：  
  - 若存在“已保存的 DB 配置”（来自配置 URL 拉取或手动设置），则 `JDBCUtils` 使用该配置；  
  - 否则使用代码里的默认值（当前 cpolar 地址），保证不配也能用。
- 在**用户菜单**（点头像弹出的 BottomSheet）中点击 **「配置数据库(开发)」**，可填写 **配置 URL**（如 GitHub Gist 的 Raw 地址），点「拉取并保存」后 App 会请求该 URL、解析 JSON 并保存，之后连接都走这份配置。
- cpolar 换地址后：**只更新 Gist 里 JSON 的 host/port**，Android 下次启动会自动拉取（若已设过配置 URL），无需改代码或重新发版。

---

## 五、Web 端对接说明（交给 Web 端 AI 用）

下面这段可直接复制给 **Web 端开发 / Web 端 AI**，保证和 Android 用同一库、同一套配置思路。

---

### 《Web 后端数据库对接说明》

- **目标**：Web 后端与 Android 使用**同一数据库、同一批表**，数据一致；且**数据库连接可配置**，便于隧道地址变更时只改一处。
- **数据库类型**：SQL Server；库名：**AccountBookDB**。
- **表与字段**：与 Android 完全一致，请严格按本仓库 **`docs/ANDROID_DB_REFERENCE.md`** 中的表名、列名、INSERT/查询示例实现（表 `Users`、`Records` 等）。不要自创表名或列名。
- **连接配置方式（二选一或同时支持）**：  
  1. **环境变量**（推荐生产）：  
     `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD`。  
     连接字符串示例（以 Node/Java 常见写法）：  
     - 若用官方 SQL Server 驱动：  
       `Server=${DB_HOST},${DB_PORT};Database=${DB_NAME};User Id=${DB_USER};Password=${DB_PASSWORD};`  
     - 若用 jdbc：  
       `jdbc:sqlserver://${DB_HOST}:${DB_PORT};databaseName=${DB_NAME}`，用户名密码用上述变量。  
  2. **统一配置 URL**（与 Android 一致）：  
     - 后端启动时请求一个“配置 URL”（如 GitHub Gist 的 Raw 地址），返回 JSON：  
       `{"host":"...","port":1234,"dbName":"AccountBookDB","user":"...","password":"..."}`  
     - 用该 JSON 拼出连接字符串；若请求失败则回退到环境变量或本地默认配置。
- **与 Android 的协同**：  
  - Android 已支持从“配置 URL”拉取并保存 DB 连接信息；若 Web 也使用同一配置 URL，则 cpolar 隧道地址变更时，**只需更新该 URL 指向的 JSON（如 Gist）**，Android 与 Web 都会拿到新地址，无需各自改代码。
- **安全**：  
  - 生产环境务必用环境变量或密钥管理服务存密码，不要将 `db_config.json` 或 `.env` 提交到公开仓库。

---

## 六、小结

- **长期/正式环境**：优先考虑 **云数据库（方案 A）** 或 **cpolar 固定域名（方案 B）**，一劳永逸。  
- **短期/继续用免费隧道**：用 **方案 C（统一配置源）**，把隧道地址放在一个 Gist 或自建配置接口里，Android 与 Web 都从这里读；本仓库已支持 Android 从 URL 拉取并保存 DB 配置，你只需在 cpolar 换地址后更新该配置源即可，无需在 JDBC 里改公网地址。

如有需要，我可以再根据你选的方案（A/B/C）写出具体的“本机脚本 + cpolar API”示例或 Gist 的 JSON 格式说明。
