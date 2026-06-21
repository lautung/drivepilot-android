# Vue 管理端 MVP 技术设计

## 1. 目标与边界

本任务交付一个位于 `apps/admin-web/` 的独立 Vue SPA，并补充它实际需要的最小后端筛选契约。管理端与 Android 共享 Spring Boot API，但拥有独立构建、测试和部署生命周期。

改动范围：

- 新建 `apps/admin-web/` 前端工程；
- 扩展 Admin 内容列表的 `status`、`category` 可选筛选参数；
- 更新 `contracts/openapi/openapi.json`、对应后端测试、根验证脚本和必要 Trellis 规范；
- 不修改 Android 代码、数据库 schema、生产部署或公开数据重置机制。

## 2. 工程结构

视觉基准保存于 [`dashboard-concept.png`](./dashboard-concept.png)。实现必须保持其表格驱动的容器模型、真白主画布、深海军蓝侧栏和克制的信息密度；内容与媒体页沿用同一应用壳与组件语言。

### 2.1 视觉系统锁定

- 主画布：`#FFFFFF`；侧栏：`#08264A` 至 `#0B2F59` 的纯色层级，不使用渐变。
- 品牌/交互色：`#2F6BFF`；正文：`#142033`；次级文字：`#526277`；分隔线：`#D7E0EA`。
- 成功：`#0AA85F`；警告：`#F59E0B`；停用：`#8C98A8`；危险操作只在文字/确认动作使用语义红色。
- 桌面侧栏宽约 `232px`，顶部栏高约 `72px`；主内容最大宽度不额外包裹巨型圆角卡片。
- 页面标题 30..32px/700，区块标题 18..20px/650，控件与表格正文 14px，辅助文字 12..13px。
- 圆角以 8px 为主、抽屉/对话框不超过 12px；边框 1px；阴影只用于浮层。
- 图标采用一致的 1.75px 线性风格；导航选中态为蓝色矩形背景和轻微圆角，不使用胶囊。
- 窄屏收起侧栏为顶部/抽屉导航，表格保持横向滚动，不转成重复卡片列表。

```text
apps/admin-web/
├── src/
│   ├── app/                 # 应用入口、路由、插件、布局
│   ├── features/
│   │   ├── auth/            # 内存会话、登录、权限能力
│   │   ├── dashboard/       # 健康与只读摘要
│   │   ├── contents/        # 内容列表、筛选、编辑与状态动作
│   │   └── media/           # 媒体列表、上传与删除
│   ├── shared/
│   │   ├── api/             # OpenAPI 生成类型、typed client、错误归一化
│   │   ├── components/      # 跨 feature 通用组件
│   │   └── utils/           # 无业务状态的纯函数
│   └── test/                # Vitest setup 与 MSW server
├── e2e/                     # Playwright Admin/Viewer 流程
├── package.json
├── pnpm-lock.yaml
└── vite.config.ts
```

- Vue Router 负责页面导航和权限 meta；Pinia 只保存当前进程内的认证状态与用户信息。
- TanStack Vue Query 是远端数据唯一缓存层；不把 API 列表复制到 Pinia。
- Element Plus 提供表格、分页、表单、上传、对话框和反馈组件；业务组件保持在 feature 内。
- 使用 Composition API 和 `<script setup lang="ts">`，不引入 Options API、class store 或额外状态框架。

## 3. API 类型与边界

`contracts/openapi/openapi.json` 是唯一契约源。前端脚本使用 `openapi-typescript` 生成 `src/shared/api/schema.d.ts`，请求层使用 `openapi-fetch` 直接消费生成的 `paths` 类型。

```text
Spring Controller / DTO
  -> Springdoc OpenAPI snapshot
  -> openapi-typescript generated paths
  -> typed API client
  -> feature query/mutation composables
  -> Vue components
```

- 生成文件提交到 Git，以便代码评审看见契约变化并让 CI 检测漂移。
- 组件不得直接解析 `Response` 或复制 DTO；Problem Details 在 API 边界统一解析为前端 `ApiProblem`。
- `/actuator/health` 不属于业务 OpenAPI。Dashboard 只读取其 `status`，通过小型运行时校验转为 `UP`、`DOWN` 或 `UNKNOWN`，不假设敏感 details 存在。
- 日期在显示边界统一格式化；UUID、枚举和 UTC 时间保持服务端原始语义。

## 4. 最小后端筛选扩展

当前 `GET /api/v1/admin/discovery/contents` 只接受 `page`、`size`，无法正确支持跨页状态/分类筛选。接口扩展为：

```http
GET /api/v1/admin/discovery/contents?page=0&size=20&status=DRAFT&category=ACTIVITY
```

- `status`、`category` 均可省略、单独使用或组合使用；排序继续按 `createdAt DESC`。
- `ContentService.adminList` 根据四种组合选择明确的 Repository 查询；不引入动态查询框架。
- 非法枚举继续由统一 Problem Details 处理，契约测试断言响应结构。
- 不变更响应 DTO、权限、数据库或 Android 公共内容接口。
- 更新 OpenAPI 快照后再生成前端类型，保证实现顺序由服务端契约向客户端单向传播。

## 5. 浏览器会话设计

### 5.1 状态

```text
unknown -> refreshing -> authenticated
                    \-> anonymous
authenticated -> refreshing -> authenticated | anonymous
authenticated -> logging-out -> anonymous
```

- access token、过期时间和 `user` 只存在 Pinia 内存 store；不启用持久化插件。
- 页面启动时先执行一次 admin refresh，路由守卫等待 bootstrap 完成，避免先闪登录页再跳转。
- refresh/logout 使用 `credentials: include`；普通业务请求带 `Authorization: Bearer`。
- refresh Cookie 由浏览器管理，JavaScript 不读取也不模拟保存。

### 5.2 401 单飞刷新

typed client 外围维护一个模块级 `refreshPromise`：

1. 业务请求首次收到 401；
2. 若没有刷新任务则创建一个，否则等待现有任务；
3. 成功后使用新 access token重放原请求一次；
4. 重放仍为 401 或 refresh 失败时清空会话并进入登录页；
5. login、refresh、logout 自身以及已重放请求不得再次触发 refresh。

这避免并发 401 消费同一轮换 Cookie，且阻止无限重试。

### 5.3 授权能力

角色只转换一次为前端能力：`ADMIN` 的 `canWrite=true`，`ADMIN_VIEWER` 为 false。布局显示 Viewer 只读横幅，所有写按钮、上传区和提交动作基于同一能力源禁用。路由与 UI 限制只改善体验，真正授权继续由 Spring Security 保证。

## 6. 页面与数据流

### Dashboard

- 并行请求 `/actuator/health`、内容第一页和媒体第一页；分页响应的 `totalElements` 作为摘要。
- 单个卡片失败不阻断其余卡片，支持独立重试；不展示 Actuator 组件详情、数据库地址或配置。

### 内容管理

- URL query 保存 `page`、`size`、`status`、`category`，便于刷新和分享当前视图。
- 列表项已包含完整正文，编辑使用抽屉/对话框，不依赖不存在的详情接口。
- 表单字段为分类、标题、摘要、普通多行正文、可选媒体 ID；客户端先做与 OpenAPI 一致的基础校验，服务端仍是最终校验方。
- 创建/更新成功后失效匹配的内容查询；发布、下架和删除使用确认框，删除成功后处理空末页回退。

### 媒体管理

- 使用 `XMLHttpRequest` 上传适配器提供真实进度，同时沿用统一 access token、Cookie 和 Problem Details 解析规则。
- 客户端预检 JPEG/PNG/WebP 和 10 MiB 限制用于即时反馈；服务端继续校验 MIME、魔数和大小。
- 列表展示文件名、类型、大小、哈希摘要、状态和创建时间。当前列表契约没有预签名 URL，因此首版不伪造图片预览。
- `MEDIA_IN_USE` 映射为明确冲突提示；413、415、429、503 分别给出可执行反馈。

## 7. 错误模型

统一 `ApiProblem` 至少包含 `status`、`code`、`detail`、`fieldErrors` 和可选 request ID。业务分支只判断稳定 `code`：

| 状态/代码 | UI 行为 |
| --- | --- |
| 400 `VALIDATION_FAILED` | 回填字段错误并保留输入 |
| 401 | 单飞刷新；失败则回登录页 |
| 403 | Viewer 写操作提示只读；其他场景进入 403 页 |
| 404 | 提示资源已不存在并刷新列表 |
| 409 `MEDIA_IN_USE` | 提示先解除内容引用 |
| 413 `MEDIA_TOO_LARGE` | 提示 10 MiB 上限 |
| 415 媒体错误 | 提示允许格式或 MIME 不匹配 |
| 429 `RATE_LIMITED` | 展示稍后重试并尊重 `Retry-After` |
| 503 `MEDIA_STORAGE_UNAVAILABLE` | 保留操作上下文并允许重试 |
| 网络/非 JSON 错误 | 使用通用错误，不显示内部响应正文 |

## 8. 测试设计

- Vitest：纯函数、角色能力、Problem Details 映射、日期/文件大小格式化。
- Vue Testing Library + MSW：登录、启动 refresh、并发 401 单飞、路由守卫、Viewer 只读、内容表单、筛选分页和错误状态。
- 后端 MockMvc/Testcontainers：筛选四种组合、非法参数、分页元数据和 Viewer/Admin 权限回归。
- Playwright + 本地真实后端：Admin 登录后上传媒体并完成内容生命周期；Viewer 登录后浏览且无写入口，并用直接 API 调用确认 403。
- E2E 凭据通过进程环境覆盖，默认仅匹配仓库本地 Compose 的公开开发默认值；不记录 token、Cookie 或密码到日志/快照。

## 9. 本地开发与验证

- Vite 将 `/api`、`/actuator` 代理到可配置的本地后端地址，默认 `http://localhost:8080`。
- 前端生产构建输出只生成静态文件；Nginx/容器化和公网部署留给后续生产部署子任务。
- 根 `tools/verify-all.ps1` 增加前端 install-lockfile 校验、lint、typecheck、unit test 和 build；Playwright 真实 E2E 作为需要 Docker/浏览器的显式选项，避免普通验证隐式启动外部服务。

## 10. 兼容、风险与回滚

- 后端只增加可选查询参数，旧调用行为和返回结构不变；可独立回滚前端静态制品。
- OpenAPI 变更先落后端测试与快照，再生成前端类型，防止手写类型先行漂移。
- 最大风险是 refresh 轮换的并发竞态；由单飞刷新单测和真实 E2E 双重覆盖。
- 媒体上传失败不得把临时 UI 项当成成功数据；mutation 成功前不做持久化乐观更新。
- Viewer UI 失误不能升级权限；Playwright 直接调用写 API 验证后端最终拒绝。
- 若管理端构建阻塞，可删除 `apps/admin-web/` 并回滚可选筛选参数；无数据库迁移或不可逆状态。
