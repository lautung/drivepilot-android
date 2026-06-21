# PhoneCar 生产级全栈路线图

## 实施原则

- 当前文件是父任务路线图；每一阶段创建独立 Trellis 子任务并分别评审、实现、验证和归档。
- 不一次性重写现有 Android 或 Backend；先建立契约与测试，再渐进替换薄弱路径。
- 每个子任务必须留下可展示证据：测试报告、CI 记录、架构说明、截图或可运行环境。
- runtime、`.env`、签名文件、token、云密钥和构建产物不得提交。

## P0：后端契约与安全基线（推荐第一个实施任务）

### 范围

- [ ] 盘点所有现有 Controller、状态码、DTO、Problem Details code 和权限，导出首份 OpenAPI 快照。
- [ ] 拆分过大的 `BackendIntegrationTest`，补齐预约、维保、订阅、偏好、车辆乐观锁、内容关注、Admin RBAC 的 good/bad/boundary case。
- [ ] 使用 PostgreSQL Testcontainers 验证从空库执行全部 Flyway migration 和 JPA validate。
- [ ] 增加 MinIO/S3 集成测试，覆盖合法上传、格式/大小拒绝、引用删除冲突、对象存储失败补偿。
- [ ] 增加 `ADMIN_VIEWER` 角色和 method-level 写权限；创建幂等公开演示账号 seed。
- [ ] 设计并实现 Admin Web cookie refresh 会话，保留 Android JSON token 契约。
- [ ] 增加认证/refresh/上传限流和 production profile 配置校验。
- [ ] CI 检查 OpenAPI 漂移、测试实际执行且容器测试没有被跳过。

### 验收

- 所有现有 API 至少有成功、认证/授权和主要失败契约测试。
- `ADMIN_VIEWER` 读取成功、任何写请求返回 `403`；`ADMIN` 写入成功。
- 浏览器 refresh token 不出现在响应 JSON、LocalStorage 或日志。
- OpenAPI 快照可生成、可 diff，后端改动未更新契约时 CI 失败。

## P1：Vue Admin MVP

- [ ] 在 `apps/admin-web/` 创建 Vue 3 + TypeScript + Vite 工程。
- [ ] 配置 pnpm、ESLint、Prettier、Vue Router、Pinia、TanStack Vue Query、Element Plus。
- [ ] 从 OpenAPI 生成 TypeScript 类型和 API client 边界。
- [ ] 实现登录、会话恢复、退出、路由守卫、401 refresh、403 页面和全局错误处理。
- [ ] 实现 Dashboard、媒体上传/列表/删除、内容列表/筛选/编辑/发布/下架/删除。
- [ ] 实现 `ADMIN_VIEWER` 只读 UI；后端权限测试作为最终保障。
- [ ] 使用 Vitest、Vue Testing Library 和 MSW 覆盖组件与 API 状态。
- [ ] 使用 Playwright 覆盖 Admin 和 Viewer 两条关键流程。

### 验收

- `pnpm lint && pnpm typecheck && pnpm test && pnpm build` 通过。
- Admin 可完成媒体与内容生命周期；Viewer 无法发起或绕过写操作。
- loading、空数据、分页、验证错误、401、403、409、413、503 均有明确界面状态。

## P1：Android 服务端闭环

- [ ] 分离本地 UI 状态、用户缓存、远端确认状态和瞬时请求状态。
- [ ] 修复离线启动：网络失败时保留有效会话材料和最近缓存，展示离线状态而非清空数据。
- [ ] 按车辆、偏好、订阅、服务记录、发现内容拆分刷新状态和重试。
- [ ] 接入预约/维保查询，UI 从服务端记录恢复确认状态并阻止重复提交。
- [ ] 将发现内容关注接到真实 content ID；移除与服务端重复的本地关注事实。
- [ ] 补齐 Remote Repository/MockWebServer 测试：401 单飞刷新、409 刷新、失败回滚、缓存隔离、部分接口失败。
- [ ] 新增真实 Activity + 测试后端设备流程，同时保留快速的纯 UI 导航测试。
- [ ] 配置正式 Release build、签名注入、HTTPS base URL 和 release 网络安全策略。

### 验收

- 断网重启可读取最近缓存并明确标记离线；恢复网络后可重试同步。
- 预约、维保、订阅和内容关注在重启、换设备和刷新后与服务端一致。
- Debug 与 Release 均构建成功，Release 不允许明文 HTTP 或弱默认 API 地址。

## P2：跨端测试与本地一键环境

- [ ] 重构 `infra/docker-compose.yml` 为可复用本地栈，包含 Backend、PostgreSQL、MinIO 和 Admin 静态站点。
- [ ] 提供幂等 seed/reset 命令，生成 Admin、Viewer、Android demo 数据和已发布内容。
- [ ] 增加 OpenAPI 契约测试与 Android JSON fixture。
- [ ] Playwright 在真实本地栈验证媒体上传→内容发布→Viewer 查看。
- [ ] Android 在同一测试栈验证登录→车辆写入→重启恢复→离线缓存。
- [ ] `tools/verify-all.ps1` 纳入 Admin 和跨端 smoke，可按参数选择设备/容器重测试。

## P2：CI/CD 与生产部署

- [ ] 建立 PR 工作流：Backend、Android、Admin、OpenAPI、Compose config、secret scan、依赖审计。
- [ ] 构建 Backend OCI image、Admin 静态制品、签名 Android APK；生成 SBOM 并扫描镜像。
- [ ] 制品推送到受控 registry/release，不提交到 Git。
- [ ] 在 `infra/production/` 提供反向代理、Backend 容器、环境变量契约和健康检查模板。
- [ ] 配置域名、TLS、安全头、请求大小和速率限制。
- [ ] 通过 CI 部署固定 image digest；部署后执行 migration/health/smoke。
- [ ] 保留上一版本 digest，健康检查失败自动停止发布并可一键回滚。
- [ ] Android Release 指向 HTTPS API，APK 作为简历演示制品发布。

## P2：可观测性、恢复与安全收口

- [ ] Backend 增加结构化日志、correlation ID、受保护指标和关键业务指标。
- [ ] 配置 uptime、5xx、延迟、数据库连接和容器异常告警。
- [ ] Admin 增加错误边界和脱敏错误上报；Android 接入可替换的崩溃上报边界。
- [ ] 配置托管 PostgreSQL 自动备份/PITR 和对象存储生命周期策略。
- [ ] 完成一次恢复演练并记录 RPO、RTO、步骤和验证结果。
- [ ] 执行 OWASP 基线检查：认证、授权、上传、CORS/CSRF、CSP、secret、依赖和日志泄露。
- [ ] 每日重置 Android 演示账号数据；Viewer 始终只读。

## 建议子任务顺序

1. `backend-contract-security-baseline`
2. `admin-web-mvp`
3. `android-server-state-closure`
4. `cross-client-e2e`
5. `production-cicd-deployment`
6. `observability-recovery-security`

`admin-web-mvp` 与 `android-server-state-closure` 在后端契约稳定后可并行，但当前 Codex inline 模式按顺序执行和验收。

## 统一验证命令目标

```powershell
Push-Location services/backend
.\gradlew.bat test bootJar
Pop-Location

Push-Location apps/admin-web
pnpm install --frozen-lockfile
pnpm lint
pnpm typecheck
pnpm test --run
pnpm build
Pop-Location

Push-Location apps/android
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleRelease
Pop-Location

docker compose -f infra/local/docker-compose.yml config --quiet
pwsh -File .\tools\verify-all.ps1 -IncludeContainers -IncludeWebE2E
pwsh -File .\tools\verify-all.ps1 -IncludeDeviceTests
```

## 风险与回滚点

- **认证迁移**：Android 与 Web 会话契约分开增加，不直接破坏现有 `/auth/*`；每步保留兼容测试。
- **角色 migration**：新增 enum/constraint 使用新 Flyway migration；先扩展数据库约束，再发布读取新角色的代码。
- **对象存储迁移**：先用 S3 兼容集成测试验证 endpoint/path-style/presign，再切生产配置；不移动现有对象直到校验完成。
- **状态模型重构**：先用测试锁定现有 UI 行为，再逐领域替换 Repository；保留旧本地实现用于测试，不做一次性重写。
- **生产部署**：镜像使用不可变 digest；数据库变更必须向前兼容至少一个应用版本，避免回滚时旧代码无法启动。

## 启动实施前评审门

- [ ] 用户确认本路线图及优先级。
- [ ] 创建第一个子任务 `backend-contract-security-baseline`，把 P0 范围拆成可在一次任务中完成的验收项。
- [ ] 第一个子任务完成 Trellis PRD、design、implement 评审后再 `task.py start`。
