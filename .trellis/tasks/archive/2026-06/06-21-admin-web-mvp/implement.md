# Vue 管理端 MVP 实施计划

## 1. 锁定契约与工程基线

- [x] 运行当前后端测试、OpenAPI 快照比较和根验证脚本，记录改动前基线。
- [x] 检查 Node.js 与 pnpm 环境；使用 pnpm lockfile 固定依赖，不提交全局工具或本地缓存。
- [x] 在根 `.gitignore` 补充 Node、Vite、Vitest、Playwright 构建/runtime/报告目录。
- [x] 在 `apps/admin-web/` 创建 Vue 3 + TypeScript + Vite 独立工程，配置严格 TypeScript、ESLint、Prettier 和包脚本。

## 2. 先完成后端筛选契约

- [x] 为 Admin 内容列表增加可选 `status`、`category` 参数，保持默认行为和 `createdAt DESC` 排序。
- [x] 在 Repository/Service 中覆盖无筛选、单字段和组合筛选四条路径，不引入数据库 migration。
- [x] 增加 MockMvc/Testcontainers 测试：四种筛选组合、分页总数、非法枚举、Viewer 读取和 Admin 写权限回归。
- [x] 显式更新并评审 `contracts/openapi/openapi.json`，普通后端测试确认无未提交契约漂移。

## 3. 建立前端 API 与测试底座

- [x] 配置 Vue Router、Pinia、TanStack Vue Query、Element Plus、Vitest、Vue Testing Library、MSW 和 Playwright。
- [x] 增加 OpenAPI 类型生成脚本和漂移检查；用 `openapi-fetch` 建立 typed client。
- [x] 实现 `ApiProblem` 归一化、字段错误映射、日期/文件大小格式化与健康响应运行时校验。
- [x] 配置 Vite `/api`、`/actuator` 代理和环境变量类型，默认连接 `http://localhost:8080`。
- [x] 先写 API 边界和错误映射单测，确认 HTML/非 JSON/网络错误不会泄露内部正文。

## 4. 实现认证与应用壳

- [x] 实现仅内存的 auth store、login/refresh/logout 调用和启动 bootstrap 状态机。
- [x] 实现 401 单飞 refresh、原请求最多重放一次、refresh 失败统一清理会话。
- [x] 实现登录页、受保护布局、路由守卫、404/403 页面、全局错误边界和 Viewer 只读横幅。
- [x] 集中定义角色能力，确保路由、按钮、上传区和提交动作消费同一 `canWrite` 来源。
- [x] 使用 MSW 覆盖登录成功/失败、启动恢复、并发 401、refresh 失败、403 和退出。

## 5. 实现 Dashboard 与内容管理

- [x] Dashboard 并行加载健康、内容总量、媒体总量，卡片独立处理 loading/error/retry。
- [x] 内容列表把分页与筛选同步到 URL query，覆盖空数据、末页回退和请求错误。
- [x] 内容表单实现分类、标题、摘要、普通多行正文和可选媒体选择，映射服务端字段错误。
- [x] 实现创建、编辑、发布、下架、删除及确认交互；成功后精确失效查询缓存。
- [x] Viewer 不渲染可执行写入口，组件测试确认通过键盘或直接事件也不能提交 mutation。

## 6. 实现媒体管理

- [x] 媒体列表展示分页与元数据，覆盖 loading、empty、error 和 retry。
- [x] 使用可报告进度的 multipart 上传适配器，复用 access token 和统一错误解析。
- [x] 增加 JPEG/PNG/WebP、10 MiB 客户端预检；覆盖 413、415、429、503 的明确提示。
- [x] 实现删除确认与 `MEDIA_IN_USE` 冲突提示；Viewer 禁用上传和删除。

## 7. 端到端与仓库集成

- [x] 配置 Playwright webServer、trace、screenshot 和 runtime 忽略规则，不提交测试产物。
- [x] 编写 Admin 真实流程：登录、上传媒体、创建内容、编辑、发布、下架、删除、退出。
- [x] 编写 Viewer 流程：登录、浏览 Dashboard/内容/媒体、无写入口、直接写 API 返回 403。
- [x] E2E 使用唯一标题/文件名隔离数据，并在流程结束清理；失败重跑不会依赖旧数据。
- [x] 更新 `tools/verify-all.ps1` 纳入前端 lockfile install、lint、typecheck、unit test、build，并为真实 Web E2E 提供显式开关。
- [x] 更新中英文 README 的仓库结构、技术栈、启动和验证命令；不创建简历展示文档。

## 8. 完整质量门与规范收尾

- [x] 运行前端 lint、format check、typecheck、unit/component test、OpenAPI drift check 和 production build。
- [x] 运行后端 `test bootJar`，确认筛选、认证、媒体、权限和 OpenAPI 全量回归。
- [x] 启动本地 Compose，确认 backend healthy；运行 Admin 与 Viewer Playwright 流程。
- [x] 运行根统一验证，检查 Android JVM/构建未受影响。
- [x] 检查 Git 不包含 `.env`、secret、token、Cookie、`node_modules`、dist、coverage、Playwright runtime、测试报告或日志。
- [x] 使用 `trellis-check` 完成全量质量检查；将新 Web 工程的真实约定写入 `.trellis/spec/` 后再提交与归档。

## 验证命令

```powershell
Push-Location apps/admin-web
pnpm install --frozen-lockfile
pnpm lint
pnpm format:check
pnpm typecheck
pnpm test --run
pnpm api:check
pnpm build
Pop-Location

Push-Location services/backend
.\gradlew.bat test bootJar --console=plain
Pop-Location

docker compose -f infra/docker-compose.yml config --quiet
docker compose -f infra/docker-compose.yml up --build -d
docker compose -f infra/docker-compose.yml ps

Push-Location apps/admin-web
pnpm test:e2e
Pop-Location

pwsh -File .\tools\verify-all.ps1
pwsh -File .\tools\verify-all.ps1 -IncludeWebE2E
```

## 风险与回滚点

- **契约顺序**：先改后端测试/快照，再生成前端类型；禁止先手写 DTO 迁就旧快照。
- **会话并发**：401 refresh 必须单飞并限制一次重放；出现循环跳转时先关闭自动重放回到显式登录。
- **Cookie**：不得通过读取 `document.cookie`、测试日志或持久化 store 模拟 refresh token。
- **分页筛选**：服务端负责全量筛选；前端不得只过滤当前页并伪造总数。
- **上传**：成功响应前不把媒体写入缓存；失败时保留可重试文件但不保留凭证。
- **权限**：Viewer UI 是体验层，后端 403 测试和 E2E 直接调用是安全验收点。
- **仓库脚本**：先保留原 Android/Backend 门禁，再追加 Web；若 Node 环境阻塞不得降低现有检查。
- **回滚**：无 migration；前端目录、可选查询参数和验证脚本可分别回滚，旧 Android/Swagger 调用不受影响。

## 开始实施前

- [x] 用户评审 `prd.md`、`design.md` 和 `implement.md`。
- [x] 评审通过后运行 `task.py start 06-21-admin-web-mvp`。
- [x] 加载 `trellis-before-dev` 及相关 Web/Backend 规范后再修改业务代码。

## 实施结果

- 前端 lint、format check、typecheck、9 个 Vitest/MSW/组件测试、OpenAPI drift check 和 production build 通过。
- 后端全量 `test bootJar` 通过；Android `testDebugUnitTest lintDebug assembleDebug` 回归通过。
- PostgreSQL、MinIO、Backend Compose 服务均为 healthy，`/actuator/health` 为 `UP`，OpenAPI 为 `3.1.0`。
- Playwright 在冷 Vite 缓存下通过 Admin 内容/媒体全生命周期与 Viewer UI/API 只读流程，共 2 条真实 E2E。
- Browser/IAB 完成登录、Dashboard、内容导航、控制台和 390px 窄屏检查；内置截图接口超时，视觉截图改用项目 Playwright 捕获并与概念图直接比较。
- Docker Hub 在最终重复构建时连续返回 EOF；本次后端改动已在此前成功构建的当前 healthy 容器中完成 E2E，该外部网络异常不影响代码验证结论。
