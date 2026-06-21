# Web Admin 开发规范

`apps/admin-web/` 是独立的 Vue 3 / TypeScript / Vite SPA，通过 OpenAPI 生成类型调用 Spring Boot。它不得读取 Android 源码或复制后端 DTO。

## 规范索引

| 文档 | 适用范围 |
| --- | --- |
| [管理端契约与质量](./admin-contracts-and-quality.md) | 工程边界、认证、OpenAPI、页面状态、测试和构建 |

## 开发前检查

- 修改页面、API、会话或权限前完整阅读“管理端契约与质量”。
- API 改动必须先更新后端测试与 `contracts/openapi/openapi.json`，再运行 `pnpm api:generate`。
- 远端数据使用 TanStack Vue Query；Pinia 只保存内存会话，不复制列表缓存。
- Element Plus 按组件导入；页面保持表格/列表驱动，不套用第三方 Admin 模板。
- 新增写入口时同时验证 `ADMIN_VIEWER` UI 无入口、后端直接请求返回 `403`。

## 质量检查

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
```

真实流程追加 `pnpm test:e2e`，要求本地 Compose 后端、PostgreSQL 和 MinIO healthy。
