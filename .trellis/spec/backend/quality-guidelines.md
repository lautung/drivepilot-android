# 质量与测试

## 必须执行的门禁

后端改动至少从独立工程根执行：

```powershell
Push-Location services/backend
.\gradlew.bat test bootJar
Pop-Location
```

涉及 Docker、环境变量或数据库连接时追加：

```powershell
docker compose -f infra/docker-compose.yml config --quiet
docker compose -f infra/docker-compose.yml build backend
docker compose -f infra/docker-compose.yml up -d
docker compose -f infra/docker-compose.yml ps
```

不得以根 Gradle `:backend:*` 任务替代；仓库根不是 Gradle 工程。

## 当前测试结构

- `BackendIntegrationSupport` 使用 `@SpringBootTest`、`@AutoConfigureMockMvc`，在测试 JVM 生命周期内共享 PostgreSQL 16 和 MinIO Testcontainers。
- 领域测试分别位于 `auth/user/vehicle/service/content/media/contract` 包；测试使用随机 UUID 后缀用户名隔离数据。
- 容器测试是后端质量门的一部分，不静默跳过；Docker 不可用时应明确报告环境阻塞。
- `@DynamicPropertySource` 注入 datasource、MinIO、隔离的 Admin/Viewer 和测试 bucket。
- `OpenApiContractTest` 比较规范化快照；普通测试不修改工作树。
- 当前全量后端门禁执行 10 个 suite、21 个 test，覆盖 PostgreSQL、MinIO、MockMvc、限流和 production 配置守卫。

## 改动与验证矩阵

| 改动 | 最低验证 |
| --- | --- |
| Controller、DTO、Validation | MockMvc good/base/bad case，断言 status、content type、code 和字段 |
| Auth、refresh token、Security | PostgreSQL 集成测试；轮换、复用拒绝、USER/ADMIN 权限 |
| Entity、Repository、事务、锁 | Testcontainers；Flyway 全量执行、JPA validate、并发/隔离约束 |
| Migration | 从空数据库执行全部 migration，不能只在已有卷上验证 |
| MinIO/媒体 | 合法/非法格式、大小、私有访问、引用删除冲突和 503 映射 |
| Web Admin 会话 | login 角色、响应无 refresh token、Cookie 属性、轮换、旧 token 拒绝、logout 清理 |
| 限流 | 单元测试可信代理/key；独立 Spring context 验证 `429` ProblemDetail 和 `Retry-After` |
| OpenAPI | 快照无漂移；Bearer、公开 auth 空 security、Admin refresh Cookie security |
| Docker/Compose | config、build、healthy、`/actuator/health`、`/v3/api-docs` |

## 评审检查

- Controller 是否返回 DTO 而非 Entity，用户 ID 是否只来自 JWT subject。
- 写操作是否有明确事务，读操作是否使用 read-only 事务，Repository 查询是否保持用户隔离。
- 新字段是否同步更新 migration、Entity、DTO、Android 消费方和契约测试。
- 错误是否使用稳定 `ApiException` code，是否避免泄露堆栈、SQL和凭据。
- migration 是否只新增版本，JPA 是否继续 `ddl-auto=validate`、`open-in-view=false`。
- Admin/Viewer 是否只能走 Cookie refresh 端点，移动 JSON 登录是否仍只服务 USER。
- 新接口是否更新 OpenAPI 快照，安全方案是否正确，是否误把公开 auth 标成 Bearer。
- Git 中是否没有 `.env`、secret、runtime、Gradle cache、build 产物或测试日志。

## 示例测试

```java
mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"x\",\"password\":\"short\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.fieldErrors").isArray());
```

只断言 `isBadRequest()` 不足以保护客户端契约；至少同时断言稳定错误码和结构。

更新 OpenAPI 快照必须显式执行并评审 diff：

```powershell
Push-Location services/backend
.\gradlew.bat '-Dphonecar.updateOpenApiSnapshot=true' test --tests '*OpenApiContractTest'
Pop-Location
```
