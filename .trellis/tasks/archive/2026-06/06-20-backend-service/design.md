# 增加后端服务：技术设计

## 1. 设计目标

在不接入真实车辆、支付或云厂商的前提下，为 PhoneCar 原型增加可本地完整运行的服务端，并把现有 Android 单机状态改造成“服务端业务状态 + 设备本地 UI 状态 + 最近成功缓存”的明确边界。

关键原则：

- 服务端是登录用户业务数据的唯一权威来源。
- Android 的 DataStore 只保存设备本地 UI 状态、用户隔离的最近成功缓存和会话恢复所需信息。
- 所有外部输入在 API 边界校验，数据库约束作为第二道防线。
- API DTO、JPA Entity、Android UI Model 分离，不能让数据库结构或网络 DTO 直接泄漏到 Compose。
- 第一期只实现演示业务，不把模拟操作包装成真实车控或真实支付。

## 2. 工程与技术基线

### 2.1 仓库结构

在当前 Gradle 多项目中增加独立 Java 模块：

```text
PhoneCar/
├── app/                         # 现有 Android 客户端
├── backend/                     # Spring Boot Java 21 服务
│   ├── src/main/java/com/lautung/phonecar/backend/
│   │   ├── auth/
│   │   ├── user/
│   │   ├── vehicle/
│   │   ├── service/
│   │   ├── content/
│   │   ├── media/
│   │   └── common/
│   ├── src/main/resources/db/migration/
│   └── src/test/
├── docker-compose.yml
└── .env.example
```

`settings.gradle.kts` 增加 `:backend`。后端使用同一 Gradle Wrapper，但保持自己的源码、测试和运行配置；Android 和后端不直接共享源代码，通过 `/api/v1` OpenAPI 契约对齐。

### 2.2 版本与主要依赖

- Java 21。
- Spring Boot 4.1.0；该版本支持 Java 21 和当前 Gradle 9.1。
- Spring Web MVC、Validation、Security、OAuth2 Resource Server/Jose、Data JPA、Flyway、PostgreSQL、Actuator。
- springdoc-openapi 3.x（Spring Boot 4 兼容线）和 Swagger UI。
- MinIO Java SDK 9.x。
- 后端测试使用 JUnit 5、Spring Security Test、Testcontainers PostgreSQL 和 MinIO。
- Android 使用 Retrofit 3、OkHttp 5、Kotlinx Serialization、Coil 3 和 MockWebServer；依赖继续集中在 version catalog。

精确补丁版本均写入 version catalog/后端构建文件并由依赖解析验证，不使用动态版本。

## 3. 端到端数据流

### 3.1 登录与会话恢复

```text
LoginScreen
  -> AuthRepository.login
  -> POST /api/v1/auth/login
  -> access token + opaque refresh token
  -> access token 保存在内存
  -> refresh token 由 Android Keystore 加密后落盘
  -> 清除旧用户同步缓存
  -> 拉取服务端状态并写入当前用户缓存
  -> PhoneCarUiState -> Compose
```

应用重启时使用加密保存的 refresh token 换取新 token；刷新失败则回到登录页。退出调用撤销接口，并清理 access token、refresh token 和该用户的服务端缓存。

### 3.2 业务读取与离线缓存

```text
API JSON -> Android network DTO -> domain snapshot -> user-scoped DataStore cache
                                              -> PhoneCarUiState -> Screen
```

有网络时先显示缓存并刷新服务端数据；无网络或请求失败时保留缓存并标记离线/错误。首次登录没有缓存时显示加载状态，不把匿名 `DemoState` 上传到服务端。

### 3.3 在线写入

```text
UI event -> ViewModel -> Repository -> API
                         | success: 更新 confirmed snapshot + DataStore cache
                         | failure: 恢复 confirmed snapshot + 可重试错误
```

同一资源的重复写入在请求完成前禁用。车辆状态更新携带 `version`；服务端版本不匹配返回 `409 STATE_VERSION_CONFLICT`，Android 重新拉取权威状态后提示用户重试。第一期不排队离线写入。

### 3.4 图片上传与读取

```text
ADMIN multipart upload
  -> 文件大小/MIME/魔数校验
  -> UUID 对象键
  -> MinIO private bucket
  -> media_assets 元数据

Published content query
  -> content + media metadata
  -> 针对公共 MinIO endpoint 生成短期预签名 GET URL
  -> Android Coil（mediaId 作为 memory/disk cache key）
```

上传使用后端内部 MinIO endpoint；签名使用独立 `MINIO_PUBLIC_ENDPOINT` 和固定 region，避免 Docker 服务名出现在 Android 无法访问的 URL 中。本地模拟器默认公共 endpoint 为 `http://10.0.2.2:9000`，外部部署时由环境变量替换。

## 4. 后端领域与数据库

全部主键使用 UUID；时间使用 UTC `Instant`，API 输出 ISO-8601；有限值使用稳定的大写枚举字符串。

### 4.1 表与职责

| 表 | 主要职责 |
| --- | --- |
| `users` | 用户名、BCrypt 密码哈希、`USER/ADMIN` 角色、状态与审计时间 |
| `refresh_tokens` | refresh token 的 SHA-256 哈希、用户、到期和撤销时间；不保存原文 |
| `vehicle_states` | 每用户一份模拟车辆业务状态、JPA 乐观锁 `version` |
| `user_preferences` | 定位分享、车内摄像头等个人设置 |
| `content_follows` | 用户与发现内容的关注关系 |
| `vehicle_reservations` | 车辆预订记录及车型配置快照、状态和创建时间 |
| `maintenance_bookings` | 维保服务、预约日期、状态和创建时间 |
| `subscriptions` | 用户、计划、启停状态和生效时间；同一计划保持单一当前记录 |
| `media_assets` | MinIO 对象键、原文件名、MIME、大小、SHA-256、上传者和状态 |
| `discovery_contents` | 分类、标题、摘要、正文、媒体引用、发布状态和发布时间 |

注册用户时在同一数据库事务内创建默认 `vehicle_states` 与 `user_preferences`。Flyway 是唯一 schema 变更入口，JPA 使用 validate，不自动建表。

### 4.2 服务端与本地字段边界

服务端车辆状态包括锁车、空调、净化、温度、风量、座椅、灯光、车窗、后视镜、后备箱、遮阳帘、儿童锁和哨兵开关。

服务端个人/内容状态包括定位分享、车内摄像头和内容关注。车辆预订、维保预约、订阅使用独立记录。

Android 本地状态包括发现标签、哨兵查看角度、尚未提交的车漆/轮毂/日期/服务选择和弹窗显示。道路救援取消、清理缓存等未纳入第一期后端的原型操作保持本地。

## 5. REST API 契约

统一前缀 `/api/v1`。普通业务接口要求 Bearer access token；管理接口还要求 `ADMIN`。

### 5.1 认证

| Method | Path | 行为 |
| --- | --- | --- |
| `POST` | `/auth/register` | 用户名密码注册，只能创建 `USER`，返回 token pair |
| `POST` | `/auth/login` | 登录并返回 token pair |
| `POST` | `/auth/refresh` | 轮换 refresh token 并返回新 token pair |
| `POST` | `/auth/logout` | 撤销当前 refresh token |
| `GET` | `/me` | 返回当前用户 ID、用户名和角色 |

access token 使用 HMAC JWT，默认 15 分钟；refresh token 使用 256-bit 随机不透明值，默认 30 天，每次刷新都轮换。JWT secret、有效期和管理员初始化凭证只来自环境变量。用户名唯一且规范化，密码长度和字符规则在 API 边界校验。

### 5.2 用户业务接口

| Method | Path | 行为 |
| --- | --- | --- |
| `GET/PATCH` | `/vehicle-state` | 读取或按字段更新模拟车辆状态；PATCH 必须携带 version |
| `GET/PATCH` | `/me/preferences` | 读取或更新个人设置 |
| `GET` | `/discovery/contents` | 分页读取已发布内容，可按分类筛选 |
| `GET` | `/discovery/contents/{id}` | 读取已发布内容详情 |
| `PUT/DELETE` | `/discovery/contents/{id}/follow` | 幂等关注/取消关注 |
| `POST/GET` | `/vehicle-reservations` | 创建和分页读取自己的车辆预订 |
| `POST/GET` | `/maintenance-bookings` | 创建和分页读取自己的维保预约 |
| `GET` | `/subscriptions` | 读取自己的订阅 |
| `PUT/DELETE` | `/subscriptions/{plan}` | 幂等启用/停用指定订阅计划 |

列表统一返回 `items/page/size/totalElements/totalPages`。创建接口返回 `201` 和资源表示；幂等更新返回最新资源；删除/退出成功返回 `204`。

### 5.3 管理接口

| Method | Path | 行为 |
| --- | --- | --- |
| `POST` | `/admin/media` | multipart 图片上传 |
| `GET` | `/admin/media` | 分页查询媒体元数据 |
| `DELETE` | `/admin/media/{id}` | 删除未被内容引用的媒体 |
| `POST` | `/admin/discovery/contents` | 创建草稿 |
| `GET` | `/admin/discovery/contents` | 查询全部状态内容 |
| `GET/PUT/DELETE` | `/admin/discovery/contents/{id}` | 查看、完整修改、删除内容 |
| `POST` | `/admin/discovery/contents/{id}/publish` | 发布内容 |
| `POST` | `/admin/discovery/contents/{id}/unpublish` | 下架内容 |

图片第一期允许 JPEG、PNG、WebP，默认最大 10 MiB。服务端不信任文件名和声明 MIME，验证实际内容后用 UUID 生成对象键。被内容引用的媒体删除返回 `409 MEDIA_IN_USE`。

### 5.4 错误结构

使用 RFC 9457/Spring `ProblemDetail`：

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Vehicle state has changed",
  "instance": "/api/v1/vehicle-state",
  "code": "STATE_VERSION_CONFLICT",
  "fieldErrors": []
}
```

稳定 `code` 由 Android 映射为用户提示；未知错误显示通用可重试信息。主要状态码为 `400/401/403/404/409/413/415/500/503`。响应不暴露堆栈、SQL、对象存储凭证或内部路径。

## 6. Android 架构调整

### 6.1 模型与状态

保留现有页面使用的 `DemoState` 作为 UI 聚合模型，新增：

- `AuthUiState`：未登录、加载、已登录、认证错误。
- `PhoneCarUiState`：`DemoState`、首次加载、离线、待处理操作和一次性错误。
- network DTO/domain snapshot/DataStore cache mapping，三个边界各自负责序列化和默认值。

服务端字段与本地字段合并为 `DemoState` 后再交给页面，Compose 不读取 Retrofit DTO。用户级缓存必须带 `userId`，防止退出后向下一账号展示上一账号数据。

### 6.2 网络与令牌

- `ApiClient` 的基础地址由 `BuildConfig` 生成，Debug 默认 `http://10.0.2.2:8080/api/v1/`。
- access token 仅存内存；refresh token 使用 Android Keystore 生成的 AES/GCM key 加密后保存。
- OkHttp interceptor 添加 access token；authenticator 对并发 `401` 做单飞刷新，只重试一次，刷新失败清除会话。
- Debug manifest/network security config 只允许本地明文 HTTP；非 Debug 配置默认要求 HTTPS。
- 添加 `INTERNET` 权限。

### 6.3 Repository 和缓存

现有按域 Repository 接口保留业务意图，但实现改为 remote + cache；表单草稿继续只写本地。ViewModel 不捕获底层 HTTP 异常，而只消费结构化结果。

车辆写入采用“最近确认快照 + pending 状态”；请求失败或冲突时恢复确认快照。预约、订阅等提交按钮在请求期间禁用，成功后用服务端返回记录更新 UI。

### 6.4 图片

发现页/详情页使用 Coil 3 加载远程图片。请求的 memory/disk cache key 使用 `mediaId`，不使用会变化的预签名 URL。URL 失效或 `403` 时刷新内容元数据后只重试一次。

## 7. 配置、容器与运行

`docker-compose.yml` 包含：

- `postgres`：持久卷和健康检查。
- `minio`：私有存储桶服务、持久卷和健康检查。
- 后端启动器：等待 MinIO 后幂等创建私有 bucket，减少额外管理镜像依赖。
- `backend`：等待 PostgreSQL/MinIO 健康后启动，暴露 8080。

`.env.example` 只包含示例值；真实 `.env` 被 Git 忽略。必需配置包含数据库、JWT、管理员和 MinIO 内部/公共 endpoint。后端提供 `/actuator/health`，但不公开敏感 actuator 端点。

后端使用多阶段 Dockerfile 生成精简运行镜像，并以非 root 用户运行。第一期不配置生产 TLS；README 明确公网部署前必须改用 HTTPS、强 secret、反向代理和正式运维策略。

## 8. 一致性与失败处理

- 数据库事务只覆盖数据库；MinIO 操作使用补偿逻辑。上传后 DB 写入失败则尝试删除对象；删除操作保持幂等，并允许再次清理缺失对象。
- 内容发布前必须引用处于 ACTIVE 状态的媒体。
- 删除内容不自动删除媒体，避免复用资源误删；媒体由显式管理接口删除。
- 服务端写成功、Android 缓存写失败时仍以服务端为准，下次刷新修复缓存。
- MinIO 不可用时媒体管理返回 `503 MEDIA_STORAGE_UNAVAILABLE`；不影响认证、车辆状态等非媒体 API。
- PostgreSQL 不可用时健康检查失败且服务不宣告 ready。

## 9. 测试策略

### 后端

- 纯单元测试：校验、映射、状态边界、token 轮换、权限判断。
- Testcontainers 集成测试：Flyway、JPA 约束、用户隔离、乐观锁、认证/刷新/退出、管理权限、MinIO 上传/签名/删除补偿。
- MockMvc/真实容器接口测试：状态码、ProblemDetail、分页、枚举、时间格式和 OpenAPI 可用性。

### Android

- MockWebServer：DTO 兼容、token 刷新单飞、401/409/503、缓存回退、写失败回滚、账号缓存隔离。
- DataStore/Keystore 抽象测试：本地 UI 状态与用户缓存分离、退出清理。
- Compose 设备测试：注册/登录/退出、加载/离线/错误提示、远程图片语义，以及现有 16 页面导航回归。
- 端到端冒烟：先检查现有 Android 模拟器；启动 Compose 服务后，在模拟器用 `10.0.2.2` 完成登录、状态修改、预约和内容图片加载。

## 10. 兼容与回滚

- 数据库只通过向前 Flyway migration 演进；第一期开发阶段若需破坏性修正，新增 migration，不修改已执行 migration。
- Android 首次升级不上传旧业务状态；登录成功后用服务端值覆盖同步字段。
- 若网络集成需要临时回滚，可保留本地 Repository 实现并通过 Debug 构建开关切换，但生产代码路径默认 remote；该开关不得造成两套同时写入。
- 容器数据通过命名卷保存。回滚应用镜像前必须确认对应 Flyway schema 兼容；本任务不承诺自动数据库降级。

## 11. 已接受的取舍

- 选择 Spring Boot + Java 21，而不是 Ktor。
- 选择 PostgreSQL + Flyway，而不是内存数据库作为生产运行依赖。
- 选择私有 MinIO + 预签名 URL，而不是公开 bucket 或把图片打包进 APP。
- 选择 Swagger 管理 API，而不是独立 Web 管理后台。
- 选择缓存只读、在线写入，而不是离线队列和自动冲突合并。
- 选择服务端业务状态与本地 UI 状态分离，而不是同步整个 `DemoState`。
