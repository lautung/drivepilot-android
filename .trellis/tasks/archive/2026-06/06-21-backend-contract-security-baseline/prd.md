# 后端契约与安全基线

## Goal

在不破坏现有 Android 认证和业务 API 的前提下，为 Android 与后续 Vue Admin 建立稳定、可自动验证的 Spring Boot 契约与安全基线，使后续客户端开发不依赖未经测试的隐式行为。

## Requirements

- 为现有认证、用户偏好、车辆状态、预约、维保、订阅、发现内容、媒体和 Admin 内容接口建立分领域 HTTP/持久化测试。
- 使用真实 PostgreSQL Testcontainers 从空库执行 Flyway 并让 JPA `validate`；媒体流程使用 S3/MinIO 兼容测试容器，不以 mock 替代对象存储关键行为。
- 导出确定性的 OpenAPI 快照并在测试/CI 中检测漂移；Problem Details、分页、UUID、Instant 和枚举格式必须进入契约。
- 新增 `ADMIN_VIEWER` 只读角色：允许读取 Admin 列表和详情，所有创建、更新、发布、下架、上传和删除操作必须由后端拒绝。
- 提供幂等的公开演示 Viewer 初始化配置；真实密码只从环境变量注入，不写入 Git。
- 为 Vue SPA 提供浏览器专用登录、refresh 和 logout 契约：access token 返回给前端并只驻留内存，refresh token 只进入 `HttpOnly` Cookie。
- 保持现有 Android `/api/v1/auth/register|login|refresh|logout` JSON 契约兼容。
- 浏览器会话使用 `HttpOnly`、`SameSite`、明确 Path 和生产环境 `Secure` Cookie；开发环境通过 Vite 同源代理调用，不开放宽泛 CORS。
- 对认证和媒体上传入口增加可测试的限流；被限流请求返回稳定 Problem Details code。
- production profile 在弱 JWT secret、弱管理员/Viewer 凭据或非安全 Cookie 配置下拒绝启动。
- 所有新增 migration 只追加版本，不修改已经存在的 `V1`/`V2`。
- 本任务同步更新后端规范、环境变量示例和验证脚本，但不实现 Vue 页面或修改 Android 业务代码。

## Acceptance Criteria

- [x] 后端测试按领域拆分，覆盖所有现有 Controller 的成功、主要验证失败、认证/授权和资源边界；Docker 可用时容器测试实际执行且不被静默跳过。
- [x] 从空 PostgreSQL 执行全部 migration 后 JPA 校验成功；`ADMIN_VIEWER` 数据库约束、JWT role 和权限行为一致。
- [x] 合法媒体上传、非法格式/大小、对象存储失败、引用删除冲突和 Admin/Viewer 权限有真实对象存储集成测试。
- [x] `contracts/openapi/openapi.json` 可重复生成；未提交的契约漂移会使验证失败。
- [x] `USER` 访问 Admin 返回 `403`；`ADMIN_VIEWER` 的 Admin GET 成功且任何写请求返回 `403`；`ADMIN` 读写成功。
- [x] Web 登录只允许 `ADMIN`/`ADMIN_VIEWER`，响应体不含 refresh token；refresh Cookie 满足属性要求，轮换和 logout 撤销有自动化测试。
- [x] 现有 Android 认证契约测试继续通过，响应字段和 refresh 轮换语义不变。
- [x] 认证与上传限流返回 `429` 和稳定错误码，且不会把密码、token、Cookie 或内部地址写入响应/日志。
- [x] production profile 配置保护有自动化测试，默认开发 secret 不能误用于生产启动。
- [x] `services/backend` 的 `test`、`bootJar`、Docker Compose 配置和仓库统一验证通过。

## Out of Scope

- Vue Admin 页面、前端工程和 Playwright 测试。
- Android 离线缓存、预约历史和发现关注改造。
- 公网 VPS、托管数据库/对象存储、CI 部署和监控告警。
- 邮箱验证、找回密码、MFA、真实用户运营和应用商店合规。

## Compatibility

- 已发布 Android Debug 客户端必须继续使用现有 JSON token 接口。
- `/api/v1`、Problem Details 和现有业务 DTO 不做无关重命名。
- 角色扩展通过新 Flyway migration 完成，旧 `USER`/`ADMIN` 数据保持有效。

## Notes

- Keep `prd.md` focused on requirements, constraints, and acceptance criteria.
- Lightweight tasks can remain PRD-only.
- For complex tasks, add `design.md` for technical design and `implement.md` for execution planning before `task.py start`.
