# DrivePilot Admin Web

Vue 3 + TypeScript + Vite 管理端。开发服务器将 `/api` 和 `/actuator` 代理到默认的 `http://localhost:8080`。

```powershell
pnpm install --frozen-lockfile
pnpm dev
```

质量检查：

```powershell
pnpm lint
pnpm format:check
pnpm typecheck
pnpm test --run
pnpm api:check
pnpm build
```

真实端到端测试要求本地 PhoneCar Compose 后端为 healthy：

```powershell
pnpm test:e2e
```

API 类型由 `../../contracts/openapi/openapi.json` 生成。先更新并评审后端 OpenAPI 快照，再运行 `pnpm api:generate`；不要直接修改 `src/shared/api/schema.d.ts`。
