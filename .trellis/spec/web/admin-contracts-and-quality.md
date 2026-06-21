# 管理端契约与质量

## 场景：修改 Vue Admin 跨层能力

### 1. Scope / Trigger

- 触发：修改 `apps/admin-web/` 的认证、OpenAPI 请求、内容/媒体流程、权限、路由、错误或构建测试。
- Vue SPA 与 Spring Boot 生产同源；本地 Vite 代理 `/api`、`/actuator` 到 `http://localhost:8080`，不得为开发便利开放宽泛 CORS。
- Android 与 Admin 只共享 HTTP 契约，不共享 Gradle、源码或客户端状态。

### 2. Signatures

- 会话：`POST /api/v1/auth/admin/{login,refresh,logout}`。
- 内容：`GET|POST /api/v1/admin/discovery/contents`、`PUT|DELETE /api/v1/admin/discovery/contents/{id}`、`POST .../{id}/{publish,unpublish}`。
- 内容筛选：`GET .../contents?page=<0-based>&size=<1..100>&status=<optional>&category=<optional>`。
- 媒体：`GET|POST /api/v1/admin/media`、`DELETE /api/v1/admin/media/{id}`；multipart 字段固定为 `file`。
- 健康：`GET /actuator/health`，Dashboard 只读取顶层 `status`。
- 前端命令：`pnpm api:generate|api:check|lint|format:check|typecheck|test|build|test:e2e`。

### 3. Contracts

- `contracts/openapi/openapi.json` 是 TypeScript API 类型唯一来源；生成文件为 `src/shared/api/schema.d.ts`，不得手写重复 DTO。
- access token 只存在 Pinia 内存和 API bridge；不得写入 LocalStorage、SessionStorage、URL、日志或持久化插件。
- refresh token 只由浏览器通过 `HttpOnly` Cookie 管理；refresh/logout 使用 `credentials: include`。
- 并发 401 共享一个 `refreshPromise`；刷新成功后每个原请求最多重放一次，失败则清空会话。
- `ADMIN` 的 `canWrite=true`；`ADMIN_VIEWER=false`。所有写控件消费同一能力源，后端 Spring Security 始终是最终授权边界。
- API 错误在 `shared/api/problem.ts` 归一化为 `ApiProblem{status,code,fieldErrors,retryAfter}`；业务分支只判断稳定 `code`。
- 内容正文是普通多行文本，不接收或渲染 HTML/Markdown。
- TanStack Vue Query 是远端缓存唯一拥有者；mutation 成功后按 feature query key 失效，Pinia 不保存列表。
- Element Plus 使用 `unplugin-vue-components` 按组件导入；小型管理端路由使用静态组件导入，避免冷 Vite 首次懒加载触发依赖优化 reload。

### 4. Validation & Error Matrix

| 条件 | UI 行为 |
| --- | --- |
| `400 VALIDATION_FAILED` | 保留输入，将 `fieldErrors` 回填到对应表单项 |
| `401` | 单飞 refresh，成功后重放一次；失败回登录页 |
| `403` | Viewer 写入口不存在；其他权限错误进入 403 状态 |
| `404 CONTENT_NOT_FOUND` | 提示资源不存在并刷新列表 |
| `409 MEDIA_IN_USE` | 提示先解除内容引用 |
| `413 MEDIA_TOO_LARGE` | 提示 10 MiB 上限 |
| `415 UNSUPPORTED_MEDIA|MEDIA_TYPE_MISMATCH` | 提示允许格式或 MIME 不一致 |
| `429 RATE_LIMITED` | 提示稍后重试，保留 `Retry-After` |
| `503 MEDIA_STORAGE_UNAVAILABLE` | 保留操作上下文并允许重试 |
| 非 JSON/网络错误 | 使用通用脱敏消息，不展示响应正文 |

### 5. Good / Base / Bad Cases

- Good：两个请求同时收到 401，只发送一次 refresh，均使用新 token 重放成功。
- Base：Viewer 登录后可读取 Dashboard、内容和媒体，页面没有新建、上传、编辑或删除入口。
- Bad：把 access token 写入持久化 Pinia；只过滤浏览器当前页；从英文 `detail` 判断错误；用隐藏按钮代替后端 403。

### 6. Tests Required

- Vitest：Problem Details 归一化、401 单飞、角色能力、格式化和边界解码。
- Vue Testing Library + MSW：登录会话、Viewer 只读、内容表单、服务端字段错误、loading/empty/error 状态。
- Playwright + 真实后端：Admin 上传/删除媒体及内容创建、编辑、发布、下架、删除；Viewer UI 无写入口且直接 API 写请求为 `403`。
- 后端筛选或 DTO 变化：先通过 MockMvc/Testcontainers 和 OpenAPI 快照，再生成前端类型。
- 每次提交前运行 index 中的完整前端门禁；跨仓库门禁运行 `tools/verify-all.ps1`。

### 7. Wrong vs Correct

#### Wrong

```ts
localStorage.setItem('accessToken', response.accessToken)
const visibleRows = currentPage.items.filter((item) => item.status === selectedStatus)
```

这会泄露 token，并让分页总数和筛选结果错误。

#### Correct

```ts
setAccessToken(response.accessToken) // module memory only
await listContents({ page: 0, size: 20, status: selectedStatus })
```

服务端在分页前筛选，客户端只展示契约结果。

## 常见陷阱

> **Vite 冷启动**：Element Plus 自动按组件导入且路由懒加载时，首次访问新页面可能触发依赖重新优化和整页 reload，导致导航中的动态模块请求失败。当前页面数量较少，路由组件保持静态导入；若未来恢复懒加载，必须先证明冷缓存 E2E 稳定。

> **Node 25 测试环境**：某些环境会暴露无效实验性 `localStorage` 并输出 `--localstorage-file` 警告。测试不得依赖全局存储清理；应直接断言 token 不进入 Pinia state 或可读存储接口。

> **Element Plus 组件测试**：Vitest 必须排除 `e2e/**`，并通过 `test.server.deps.inline: [/element-plus/]` 处理包内 CSS；否则 Node 会尝试直接加载 `.css`。
