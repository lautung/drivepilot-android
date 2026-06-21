# 增加后端服务：执行计划

## 执行结果（2026-06-21）

- 状态：核心功能已实施，Spring Boot/PostgreSQL/MinIO Compose 环境和 Android 客户端联调通过。
- 后端：认证与权限、refresh token 哈希/轮换/撤销及并发锁、用户隔离、模拟车辆乐观锁、个人设置、预约、订阅、发现内容和媒体管理已完成。
- Android：注册/登录/退出、Keystore 会话、OkHttp 单飞刷新、远端业务仓储、缓存读取、失败保留确认状态和 Coil 远程图片已完成。
- 验证：`:backend:test :backend:bootJar`、`:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`、5 项设备测试、Docker 镜像构建和三个容器健康检查均通过。
- 真实联调：复用 `emulator-5554` 注册新用户并加载 MinIO 发现图片；容器 API 验证旧 refresh token 与退出后 token 均返回 `401`，普通用户管理接口返回 `403`。
- 证据：`evidence/phonecar-discovery.png`。非阻塞后续增强为扩大 MinIO 故障注入和全部业务 API 的自动化 HTTP 契约覆盖。

## 0. 开始前门禁

- [ ] 用户审阅并批准 `prd.md`、`design.md` 和本执行计划。
- [ ] 运行 `task.py start`，进入实施阶段。
- [ ] 加载 `trellis-before-dev`，读取 Android 相关规范和本任务设计。
- [ ] 记录当前工作树，确认仅在任务范围内修改；保留现有 `.idea`、Trellis 与用户改动。
- [ ] 核对 Java 21、Docker、Docker Compose、Android SDK 和 Gradle Wrapper 可用。

## 1. 工程骨架与依赖

- [ ] 在 `settings.gradle.kts` 增加 `:backend`，在 version catalog/构建脚本固定 Spring Boot 4.1.0、springdoc 3.x、MinIO 9.x、Retrofit/OkHttp/Serialization/Coil/Testcontainers 版本。
- [ ] 创建 `backend` Java 21 Spring Boot 模块和基础包结构，配置 JPA validate、Flyway、Actuator、统一 Jackson 时间/枚举策略。
- [ ] 添加 `.env.example`、`.gitignore` secret 规则、多阶段非 root `backend/Dockerfile`。
- [x] 新增 `docker-compose.yml`：PostgreSQL、MinIO、后端幂等 bucket 初始化、backend、健康检查、命名卷和可覆盖环境变量。
- [ ] 验证：`./gradlew :backend:test :backend:bootJar`；`docker compose config`。

回滚点：此步骤只提供可启动空服务和容器，不改 Android 数据流。

## 2. 数据库与通用 API 边界

- [ ] 编写首个 Flyway migration：users、refresh_tokens、vehicle_states、user_preferences、content_follows、reservations、maintenance、subscriptions、media_assets、discovery_contents 及索引/外键/唯一约束。
- [ ] 实现 Entity/Repository 与 domain/DTO 映射；禁止 Controller 直接返回 Entity。
- [ ] 实现统一 `ProblemDetail`、稳定错误码、字段校验错误、分页 DTO、UUID/Instant/enum 契约。
- [ ] 添加 Testcontainers PostgreSQL 测试，验证迁移、默认用户状态、约束、分页和序列化。
- [ ] 验证：`./gradlew :backend:test`。

## 3. 认证、权限和管理员初始化

- [ ] 实现用户名规范化、密码策略、BCrypt、注册和登录。
- [ ] 实现 15 分钟 HMAC JWT access token、30 天 opaque refresh token 哈希保存、轮换、撤销和并发安全。
- [ ] 配置 Spring Security：公共 auth/health/docs、认证业务 API、`ADMIN` 管理 API。
- [ ] 实现环境变量管理员初始化；只在管理员不存在时创建，不允许普通注册指定角色。
- [ ] 测试重复用户名、错误密码、token 过期/轮换/撤销、USER/ADMIN 权限、两个用户数据隔离。
- [ ] 验证：`./gradlew :backend:test`，并通过 Swagger 完成一次管理员登录授权。

## 4. 模拟车辆、个人设置和服务业务

- [ ] 实现 `/vehicle-state` GET/PATCH、范围校验、乐观锁 version 和 `409 STATE_VERSION_CONFLICT`。
- [ ] 实现 `/me/preferences` GET/PATCH。
- [ ] 实现车辆预订、维保预约、软件订阅的创建/查询/幂等启停；业务记录始终按当前用户过滤。
- [ ] 为越权 ID、非法日期/枚举、重复订阅、并发更新和分页边界添加测试。
- [ ] 验证：`./gradlew :backend:test`。

## 5. MinIO、媒体和发现内容

- [ ] 实现内部 MinIO client 与公共签名 client，读取 endpoint/region/bucket/credential 配置。
- [ ] 实现 JPEG/PNG/WebP、10 MiB、魔数与 MIME 校验、SHA-256、UUID 对象键、上传补偿和错误映射。
- [ ] 实现媒体列表和安全删除；被内容引用时返回 `409 MEDIA_IN_USE`。
- [ ] 实现管理员发现内容 CRUD、发布/下架，以及普通用户已发布内容分页/详情/关注接口。
- [ ] 响应包含 mediaId、短期预签名 URL 和 expiresAt；草稿/下架内容不出现在普通接口。
- [ ] 使用 Testcontainers/Compose MinIO 测试上传、读取签名、非法文件、对象存储故障、引用删除和权限。
- [ ] 验证：`./gradlew :backend:test`；`docker compose up --build -d` 后检查 health、Swagger、PostgreSQL 和 MinIO bucket。

## 6. Android 认证与网络基础设施

- [ ] 在 version catalog 和 app 模块增加 Serialization、Retrofit、OkHttp、Coil、MockWebServer；增加 Kotlin serialization plugin。
- [ ] 增加 `INTERNET` 权限、Debug-only 本地 HTTP 配置、可覆盖 `BuildConfig.API_BASE_URL`。
- [ ] 实现 API DTO、Retrofit service、ProblemDetail 解码和 network result 映射。
- [ ] 实现 Keystore-backed refresh token store、内存 access token、header interceptor 和单飞 authenticator。
- [ ] 新增 AuthRepository、AuthUiState、注册/登录/退出页面和根导航鉴权门。
- [ ] 添加 MockWebServer 测试：成功认证、刷新轮换、并发 401 只刷新一次、失败清会话、错误显示。
- [ ] 验证：`./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`。

回滚点：认证导航门通过 Debug 配置开关隔离；若尚未接业务 API，可保持原页面使用本地仓储。

## 7. Android 业务同步、缓存和图片

- [ ] 拆分 local UI state、remote confirmed snapshot 和 user-scoped cache，提供到 `DemoState` 的单一映射入口。
- [ ] 登录后清理旧用户缓存并拉取服务端默认状态；退出清 token 和用户缓存，不上传匿名旧状态。
- [ ] 将 Vehicle/Profile/Content/Service Repository 改为 remote + cache，实现加载、离线、pending、错误和重试状态。
- [ ] 车辆状态写入携带 version，失败回滚确认快照，409 后刷新；预约/订阅请求期间禁止重复提交。
- [ ] 接入发现内容分页和关注；使用 Coil 3 加载预签名 URL，以 mediaId 作为稳定缓存键。
- [ ] 更新页面，使服务端记录代替 `reservationConfirmed`、`maintenanceBooked` 和 `subscriptions` 等本地布尔/集合表达；道路救援等范围外行为保持本地。
- [ ] 更新 DataStore 和 Repository 测试，覆盖缓存隔离、首次同步、本地字段保留、离线读、在线写失败回滚和 URL 过期重试。
- [ ] 验证：`./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`。

## 8. 全链路验证与文档

- [ ] 更新 README/README.zh-CN：架构、环境变量、Docker Compose、管理员初始化、Swagger、模拟器 API 地址和安全边界。
- [ ] 导出或保存 OpenAPI 契约快照，增加 Android JSON fixture/后端 schema 契约测试，检查 UUID、Instant、enum、分页和 ProblemDetail。
- [ ] 运行完整后端测试和容器健康检查。
- [ ] 按项目规则先检查已有 Android 模拟器；有则直接使用，没有再创建。
- [ ] 在模拟器完成注册/登录、重启会话恢复、车控更新、冲突/断网提示、预约、订阅、发现图片和退出冒烟测试。
- [ ] 运行现有全部设备导航测试，确认 16 页面仍可达。
- [ ] 验证命令：

```powershell
./gradlew :backend:test :backend:bootJar
docker compose config
docker compose up --build -d
docker compose ps
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

## 9. 质量门与收尾

- [ ] 加载 `trellis-check`，核对 PRD 每条验收标准、跨层数据流、安全边界、测试和无关改动。
- [ ] 检查仓库中无真实密码、JWT secret、MinIO secret、`.env`、token 或预签名 URL。
- [ ] 检查数据库 migration 不被重写、API 不返回 Entity、普通用户不能命中管理接口、缓存按 userId 隔离。
- [ ] 更新 `.trellis/spec/`：把“无后端”的旧约束改为实际架构，并增加 backend/API/MinIO 规范。
- [ ] 记录验证结果与剩余限制；只有完整质量门通过后才进入提交和归档。

## 10. 主要风险与回滚点

- Gradle/Boot/AGP 插件兼容：先完成空模块构建；失败时将 backend 改为仓库内独立 Gradle build，不改 Android wrapper。
- MinIO 预签名 host：必须分别验证 Docker 内部 endpoint 和模拟器可访问的公共 endpoint；失败时不重写已签名 URL。
- token 刷新并发：在接业务 Repository 前通过 MockWebServer 单飞测试。
- DataStore 用户串数据：所有 remote cache key 带 userId，登录/退出设备测试验证。
- API/Android 漂移：以 OpenAPI + JSON fixture 双向测试拦截，不靠人工字段记忆。
- 回滚 Android remote 实现时只切换单一 Repository 装配，禁止 local/remote 双写。
