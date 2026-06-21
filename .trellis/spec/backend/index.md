# Backend 开发规范

`services/backend/` 是独立的 Java 21 / Spring Boot 4.1 Gradle 工程，提供 `/api/v1` 认证、模拟车辆、个人设置、服务记录、发现内容和 MinIO 媒体管理接口。它不得读取 Android 或仓库根 Gradle 配置。

## 规范索引

| 文档 | 适用范围 |
| --- | --- |
| [API、认证、数据库与对象存储契约](./contracts-and-storage.md) | Controller/DTO、Security、Flyway、PostgreSQL、MinIO、Docker 配置 |
| [独立构建与基础设施](./build-and-infrastructure.md) | 后端 Gradle 根、Dockerfile、Compose、环境变量与跨工程边界 |

## 开发前检查

- 修改 API、DTO、Entity、migration、认证或 MinIO 前必须完整阅读契约文档。
- schema 只通过新增 Flyway migration 演进，JPA 保持 `ddl-auto=validate`。
- Controller 不返回 Entity；用户资源必须从 JWT subject 取 userId，不能信任客户端 userId。
- 媒体 bucket 保持私有，Android 只接收预签名读取 URL，不接收 MinIO 凭证。
- 修改后端构建、Docker 或 Compose 前完整阅读“独立构建与基础设施”。

## 质量检查

```powershell
Push-Location services/backend
.\gradlew.bat test bootJar
Pop-Location
docker compose -f infra/docker-compose.yml config --quiet
docker compose -f infra/docker-compose.yml up --build -d
docker compose -f infra/docker-compose.yml ps
```

同时检查普通用户访问 `/api/v1/admin/**` 返回 `403`、`/v3/api-docs` 可用、仓库中没有 `.env` 或真实 secret。
