# 后端契约与安全基线实施计划

## 1. 锁定现有行为

- [x] 读取 backend 目录、数据库、错误、契约、质量和基础设施规范。
- [x] 记录当前 `test bootJar`、Compose health、OpenAPI 和 Android JVM 兼容基线。
- [x] 建立共享 PostgreSQL Testcontainers 支持类并拆分现有 3 个测试，不在拆分时改变业务行为。
- [x] 为偏好、车辆、预约、维保、订阅、发现内容和 Admin 权限补 characterization tests。

## 2. 对象存储测试

- [x] 增加 MinIO Testcontainer 配置和隔离 bucket。
- [x] 覆盖 JPEG/PNG/WebP 成功上传、声明 MIME/魔数不匹配、大小限制和 SHA-256。
- [x] 覆盖预签名读取、媒体引用删除冲突、删除失败状态和对象存储不可用的稳定错误。
- [x] 确认测试和日志不输出 access key、secret 或预签名 URL。

## 3. OpenAPI 基线

- [x] 创建 `contracts/openapi/` 与规范化快照。
- [x] 增加只比较的 `OpenApiContractTest` 和显式更新方式。
- [x] 在仓库验证工具中检查快照漂移。
- [x] 为认证、角色、Cookie、Problem Details、分页和 multipart 补充必要 OpenAPI 元数据。

## 4. Viewer 角色与权限

- [x] 新增 Flyway migration 扩展 `users.role` CHECK。
- [x] 新增 `ADMIN_VIEWER` enum 和配置化幂等初始化器。
- [x] 启用 method security；标记 Admin GET 为 Viewer/Admin，Admin 写操作为 Admin-only。
- [x] 增加 USER、Viewer、Admin 权限矩阵测试，包含通过伪造前端请求直接调用写接口。

## 5. Web Admin 会话

- [x] 提取 AuthService 返回模型，使移动 JSON 和 Web Cookie 适配器共享认证/轮换逻辑。
- [x] 实现 admin login/refresh/logout Controller 和无 refresh token 的响应 DTO。
- [x] 增加 Cookie 配置、创建/清理工具和生产安全属性。
- [x] 测试角色拒绝、成功登录、Cookie 属性、refresh 轮换、旧 token 复用拒绝、logout 撤销和缺失 Cookie。
- [x] 重跑现有 Android auth/MockWebServer 测试，确认 JSON 契约兼容。

## 6. 限流与生产守卫

- [x] 选择维护活跃的 token-bucket 实现并通过 version catalog 声明。
- [x] 对 login/refresh 和 media upload 应用独立策略，生成 `429 RATE_LIMITED` 和 `Retry-After`。
- [x] 明确可信代理配置，默认不信任任意 `X-Forwarded-For`。
- [x] 增加 production profile 启动守卫和配置测试。
- [x] 更新 `application.yml`、`infra/.env.example` 和本地 Compose 的 Viewer/Cookie/限流配置。

## 7. 完整验证与规范更新

- [x] 从空 PostgreSQL 容器运行所有 migration 与 JPA validate。
- [x] 运行所有后端测试，确认 PostgreSQL/MinIO 测试实际执行。
- [x] 运行 `bootJar`、Compose config/build/up/health 和 `/v3/api-docs` smoke。
- [x] 运行 Android JVM 测试，验证现有认证兼容。
- [x] 检查 OpenAPI 快照无漂移、Git 无 `.env`/secret/runtime/build 产物。
- [x] 使用 `trellis-check` 完成质量检查并更新 backend spec。

## 验证命令

```powershell
Push-Location services/backend
.\gradlew.bat clean test bootJar --console=plain
Pop-Location

Push-Location apps/android
.\gradlew.bat testDebugUnitTest --console=plain
Pop-Location

docker compose -f infra/docker-compose.yml config --quiet
docker compose -f infra/docker-compose.yml build backend
docker compose -f infra/docker-compose.yml up -d
docker compose -f infra/docker-compose.yml ps
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8080/v3/api-docs
pwsh -File .\tools\verify-all.ps1
```

## 风险与检查点

- **测试拆分**：先保持原测试断言，再删除旧聚合测试，避免测试覆盖在重构中丢失。
- **角色 migration**：不得回写 V1；迁移后从空库和已有数据两种路径验证。
- **双认证适配**：移动接口快照先锁定，Web Controller 只做传输适配，不复制 token 逻辑。
- **Cookie**：开发环境允许 HTTP 仅由显式非 production 配置决定；production 必须 Secure。
- **限流**：单实例实现必须有容量上限/过期清理，防止 key 无限增长。
- **回滚**：数据库变更保持旧代码可读；Viewer seed 在新代码健康后启用。

## 开始实施前

- [x] 用户评审 PRD、design 和 implement。
- [x] 评审通过后运行 `task.py start 06-21-backend-contract-security-baseline`。
- [x] 加载 `trellis-before-dev` 后再修改业务代码。
