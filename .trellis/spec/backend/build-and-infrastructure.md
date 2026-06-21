# 独立构建与基础设施

## 场景：修改后端构建、Docker 或本地基础设施

### 1. Scope / Trigger

- 触发：修改 `services/backend/` 下的 Gradle settings、Wrapper、version catalog、Dockerfile，或者修改 `infra/docker-compose.yml` 与环境变量示例。
- 目标：Android 和后端必须保持两个独立 Gradle 根工程；仓库根只负责编排，不得重新包含两端模块。

### 2. Signatures

PowerShell 7 下的后端构建签名：

```powershell
Set-Location services/backend
.\gradlew.bat test bootJar
```

仓库根的基础设施签名：

```powershell
docker compose -f infra/docker-compose.yml config --quiet
docker compose -f infra/docker-compose.yml build backend
docker compose -f infra/docker-compose.yml up -d
```

Android 只能从自己的根执行：

```powershell
Set-Location apps/android
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

### 3. Contracts

- `apps/android/settings.gradle.kts` 只包含 `:app`；`services/backend/settings.gradle.kts` 没有子项目。
- 两端各自拥有 `gradlew*`、`gradle/wrapper/`、`gradle/libs.versions.toml` 和 `gradle.properties`，不得通过 `../` 读取对方或仓库根配置。
- 后端 Jar 固定为 `services/backend/build/libs/phonecar-backend.jar`。
- backend 镜像构建上下文固定为 `../services/backend`，Dockerfile 只能复制上下文内文件。
- Compose 顶层 project name 固定为 `phonecar`，服务名和卷 key 保持 `postgres`、`minio`、`backend`、`phonecar-postgres`、`phonecar-minio`。
- 必需环境键沿用 [API、认证、数据库与对象存储契约](./contracts-and-storage.md)；真实值只放 `infra/.env` 或部署平台，不进入 Git。
- `.dockerignore` 必须排除 build、Gradle cache、`.env`、runtime、临时文件和密钥。

### 4. Validation & Error Matrix

| 条件 | 结果 / 处理 |
| --- | --- |
| 后端 settings/catalog 引用 `apps/android` 或仓库根 Gradle | 拒绝变更，独立构建边界失效 |
| Android catalog 出现 Spring Boot/MinIO/Testcontainers | 拒绝变更，依赖所有权错误 |
| Compose project name 不再是 `phonecar` | 可能创建新容器/卷；恢复固定名称后再启动 |
| Dockerfile 找不到固定 bootJar | 镜像构建失败；核对 archiveFileName 和 COPY 路径 |
| backend build context 包含整个仓库 | 拒绝变更，扩大 secret/缓存泄露面 |
| Git 中出现 `.env`、密钥、runtime 或 build 输出 | 删除出暂存区并补充 ignore 规则 |
| Java 版本不是 21 | Gradle toolchain 或容器构建失败；使用 Java 21 环境 |

### 5. Good / Base / Bad Cases

- Good：两端分别运行 Wrapper 可独立通过；Docker context 只传输后端源码和构建文件；Compose 复用现有 `phonecar` 卷。
- Base：从仓库根运行 `tools/verify-all.ps1`，脚本进入两个工程目录执行原生命令并校验 Compose。
- Bad：在仓库根恢复 `settings.gradle.kts` 并同时 include `:app`、`:backend`；后端 Dockerfile 从 `../gradle` 复制 Android version catalog；执行 `docker compose down -v` 清除开发数据。

### 6. Tests Required

- Android：`testDebugUnitTest lintDebug assembleDebug`；运行应用或设备测试前先用 `adb devices` 复用已有模拟器。
- 后端：`test bootJar`，并断言固定 Jar 存在。
- Docker：`config --quiet`、`build backend`、`up -d`，断言三个服务 healthy。
- HTTP：断言 `/actuator/health` 返回 `UP`，`/v3/api-docs` 可读取。
- 仓库卫生：确认根无 Gradle settings/Wrapper，两个 catalog 无交叉依赖，Git 状态不包含 secret、runtime 或 build 输出。

### 7. Wrong vs Correct

#### Wrong

```yaml
services:
  backend:
    build:
      context: ..
      dockerfile: services/backend/Dockerfile
```

整个仓库进入 Docker build context，会重新耦合 Android，并扩大本地文件进入构建上下文的风险。

#### Correct

```yaml
name: phonecar
services:
  backend:
    build:
      context: ../services/backend
      dockerfile: Dockerfile
```

构建只依赖后端工程，同时固定 Compose project name 以复用既有容器和卷。
