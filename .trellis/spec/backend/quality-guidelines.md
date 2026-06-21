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

- `BackendIntegrationTest` 使用 `@SpringBootTest`、`@AutoConfigureMockMvc` 和 PostgreSQL 16 Testcontainers。
- `@Testcontainers(disabledWithoutDocker = true)` 允许无 Docker 环境跳过容器测试；正式验证后端持久化时必须在 Docker 可用环境确认测试实际执行。
- `@DynamicPropertySource` 注入容器 datasource、隔离的管理员账号，并关闭测试中的 MinIO bucket 初始化。
- 测试使用随机 UUID 后缀用户名，避免重复运行时唯一约束冲突。
- MockMvc 契约测试同时验证 `ProblemDetail`、UUID、UTC Instant、enum、分页、USER/ADMIN 权限和 `/v3/api-docs`。

## 改动与验证矩阵

| 改动 | 最低验证 |
| --- | --- |
| Controller、DTO、Validation | MockMvc good/base/bad case，断言 status、content type、code 和字段 |
| Auth、refresh token、Security | PostgreSQL 集成测试；轮换、复用拒绝、USER/ADMIN 权限 |
| Entity、Repository、事务、锁 | Testcontainers；Flyway 全量执行、JPA validate、并发/隔离约束 |
| Migration | 从空数据库执行全部 migration，不能只在已有卷上验证 |
| MinIO/媒体 | 合法/非法格式、大小、私有访问、引用删除冲突和 503 映射 |
| Docker/Compose | config、build、healthy、`/actuator/health`、`/v3/api-docs` |

## 评审检查

- Controller 是否返回 DTO 而非 Entity，用户 ID 是否只来自 JWT subject。
- 写操作是否有明确事务，读操作是否使用 read-only 事务，Repository 查询是否保持用户隔离。
- 新字段是否同步更新 migration、Entity、DTO、Android 消费方和契约测试。
- 错误是否使用稳定 `ApiException` code，是否避免泄露堆栈、SQL和凭据。
- migration 是否只新增版本，JPA 是否继续 `ddl-auto=validate`、`open-in-view=false`。
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
