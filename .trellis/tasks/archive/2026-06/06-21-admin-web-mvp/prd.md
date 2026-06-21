# Vue 管理端 MVP

## Goal

在 `apps/admin-web/` 新建一个可独立构建、测试和部署的 Vue 3 管理端，通过现有 Spring Boot OpenAPI 契约完成浏览器安全登录、内容管理和媒体管理，形成适合纯全栈简历展示的前后端分离闭环。

## Confirmed Facts

- 管理端技术栈已确定为 Vue 3、TypeScript、Vite，不采用 SSR 或 BFF。
- Spring Boot 是 API 契约唯一事实来源，当前快照位于 `contracts/openapi/openapi.json`。
- 浏览器认证已提供 `/api/v1/auth/admin/login|refresh|logout`：access token 仅返回响应体，refresh token 仅存放于 `HttpOnly` Cookie。
- `ADMIN` 可以读写；公开演示角色 `ADMIN_VIEWER` 只能读取 Admin 内容和媒体接口，后端会拒绝其写请求。
- 现有 Admin API 覆盖内容列表、创建、编辑、发布、下架、删除，以及媒体上传、列表、删除。
- 后端健康状态可从 `/actuator/health` 读取；当前没有独立 Dashboard 聚合接口。
- Android 当前把发现内容正文作为普通文本显示，没有 Markdown 或富文本渲染契约。
- 内容正文已确定使用普通多行文本，不引入 Markdown、HTML 或富文本编辑器。
- 开发环境应使用 Vite 同源代理转发 `/api` 和 `/actuator`，不为 SPA 增加宽泛 CORS。

## Requirements

### 工程与边界

- 在 `apps/admin-web/` 创建独立 pnpm 工程，使用 Vue 3、TypeScript、Vite、Vue Router、Pinia、TanStack Vue Query 和 Element Plus。
- 使用 ESLint、Prettier、Vitest、Vue Testing Library、MSW 和 Playwright 建立可重复执行的质量门。
- 从仓库 OpenAPI 快照生成 TypeScript 类型；业务 API 层只封装认证、错误归一化和少量调用语义，不手写重复 DTO。
- 不提交 `node_modules`、构建产物、Playwright runtime、测试报告、密钥或真实环境变量。

### 认证与授权

- 提供管理员登录、启动时会话恢复、主动退出、受保护路由、401 单次 refresh 后重放和 403 页面。
- access token 只驻留内存，不写 LocalStorage、SessionStorage、持久化 Pinia、URL 或日志；所有 refresh/logout 请求携带 Cookie。
- 并发请求同时遇到 401 时只允许一个 refresh 请求，其余请求等待同一结果；refresh 失败后清空内存会话并跳转登录页。
- `ADMIN_VIEWER` 在界面上显示清晰的只读标识，隐藏或禁用全部写入口；后端授权仍是最终安全边界。

### 页面与业务流程

- Dashboard 展示后端健康状态、当前账号与角色，以及由内容/媒体分页响应得到的只读总量摘要，不新增专用聚合接口。
- 内容管理支持分页、状态/分类筛选、创建、编辑、发布、下架和删除，并正确关联可选媒体。
- Admin 内容列表增加可选 `status`、`category` 查询参数，在后端按筛选条件分页；同步更新 OpenAPI 快照和契约测试，不在前端只过滤当前页。
- 媒体管理支持分页、图片上传进度、类型/大小校验反馈、元数据展示和删除冲突反馈。
- 登录、Dashboard、内容和媒体页面均覆盖 loading、空数据、错误、重试和无权限状态。
- API 错误基于 Problem Details 的稳定 `code` 归一化；不得依赖英文 `detail` 文案判断业务分支。

### 交互与可访问性

- 采用桌面优先的响应式后台布局，至少保证常见笔记本和窄屏浏览器可操作。
- 表单具备可见标签、键盘可达、提交中防重复、字段级校验和服务端错误回填。
- 删除、发布和下架等有影响操作必须二次确认，并在成功后精确失效相关查询缓存。

## Acceptance Criteria

- [x] `pnpm lint`、`pnpm typecheck`、`pnpm test` 和 `pnpm build` 全部通过。
- [x] OpenAPI 类型生成命令可重复执行，生成后工作树无非预期漂移；前端请求/响应类型不重复手写。
- [x] Admin 内容接口按 `status`、`category` 单独或组合筛选时，分页总数和结果均正确，Viewer 仍可读取且写权限矩阵不变。
- [x] Admin 登录后可恢复会话、退出，并完成媒体上传/删除和内容创建/编辑/发布/下架/删除的完整流程。
- [x] Viewer 可以登录和浏览 Dashboard、内容、媒体，但界面不能发起写操作；直接调用后端写接口仍返回 `403`。
- [x] access token 不持久化，refresh token 不出现在 JavaScript 可读存储、响应模型、日志或测试快照中。
- [x] loading、空数据、分页、字段验证、401、403、404、409、413、429 和 503 均有明确且可测试的界面反馈。
- [x] Vitest + Vue Testing Library + MSW 覆盖认证恢复、401 单飞刷新、角色权限、内容表单和主要错误映射。
- [x] Playwright 在真实后端环境覆盖 Admin 核心写流程与 Viewer 只读流程，并保留失败时可诊断的 trace/screenshot 配置但不提交 runtime 产物。

## Out of Scope

- Android 业务改造、跨端 E2E、生产部署、CI/CD、监控告警和公开演示数据定时重置。
- 用户、车辆、预约、维保和订阅的后台管理页面。
- SSR、BFF、微前端、国际化、主题市场、拖拽页面搭建器和复杂富文本编辑器。
- 在前端复制后端授权规则或以隐藏按钮替代后端权限校验。

## Decisions

- 内容正文采用普通多行文本，与当前 Android 展示契约一致；不接受 HTML，不增加 Markdown 渲染。
- Dashboard 复用健康端点和现有分页接口计算摘要，不为首版增加聚合 Controller。
- 内容筛选通过最小后端契约扩展完成，避免分页后在浏览器局部过滤造成错误结果。

## Open Questions

- 无阻塞性产品问题。
