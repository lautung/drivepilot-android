# Android 与后端独立构建设计

## 1. 问题定义

当前仓库的源码已经分别位于 `app/` 与 `backend/`，真正的问题是构建所有权不唯一：根 Gradle 同时管理 Android 与后端，而后端又带有独立 `settings.gradle.kts`；Docker 构建还需要复制根 Wrapper 和共享版本目录。目标是让目录边界、构建边界和运行边界保持一致。

## 2. 设计原则

1. **单仓库、双工程**：保持一个 Git 历史，但 Android 和后端可以从各自目录独立构建。
2. **根目录不参与 Gradle 构建**：根目录只放仓库级文档、Trellis、基础设施和编排工具。
3. **不改变业务行为**：本任务只移动文件和拆分构建配置，不改 API、DTO、页面、Entity、migration 或持久化语义。
4. **仅通过 HTTP 协作**：Android 和后端不共享源码、Gradle 模块或版本目录。
5. **一次切换、可机械回滚**：不保留旧根 Gradle 兼容层，避免迁移后继续存在两套入口；用根 PowerShell 工具提供统一入口。

## 3. 目标目录

```text
PhoneCar/
├── apps/
│   └── android/
│       ├── app/
│       ├── gradle/
│       │   ├── libs.versions.toml
│       │   └── wrapper/
│       ├── build.gradle.kts
│       ├── settings.gradle.kts
│       ├── gradle.properties
│       ├── gradlew
│       └── gradlew.bat
├── services/
│   └── backend/
│       ├── src/
│       ├── gradle/
│       │   ├── libs.versions.toml
│       │   └── wrapper/
│       ├── build.gradle.kts
│       ├── settings.gradle.kts
│       ├── gradle.properties
│       ├── gradlew
│       ├── gradlew.bat
│       └── Dockerfile
├── infra/
│   ├── docker-compose.yml
│   └── .env.example
├── docs/
│   ├── prototype/
│   ├── screenshots/
│   └── superpowers/
├── tools/
│   ├── import-prototype-assets.ps1
│   └── verify-all.ps1
├── .trellis/
├── AGENTS.md
├── README.md
└── README.zh-CN.md
```

静态 `contracts/openapi/` 不在本任务创建；当前后端仍通过 `/v3/api-docs` 暴露运行时 OpenAPI。

## 4. 路径映射

| 当前路径 | 目标路径 | 说明 |
| --- | --- | --- |
| `app/` | `apps/android/app/` | 保持 Android module 名为 `:app` |
| 根 Android/共享 Gradle 文件 | `apps/android/` | 只保留 Android 插件和依赖 |
| `backend/src/`、构建文件 | `services/backend/` | 后端成为完整独立 Gradle 根工程 |
| `backend/Dockerfile` | `services/backend/Dockerfile` | 构建上下文缩小为后端工程 |
| `docker-compose.yml` | `infra/docker-compose.yml` | 仓库级运行基础设施 |
| `.env.example` | `infra/.env.example` | 真实 `.env` 继续忽略 |
| `doc/智驾车控APP原型_v8.html` | `docs/prototype/智驾车控APP原型_v8.html` | 合并重复文档根目录 |

## 5. Android 构建边界

- `apps/android/settings.gradle.kts` 只包含 `:app`，保留 Google、Maven Central 和 Gradle Plugin Portal 仓库配置。
- `apps/android/build.gradle.kts` 只声明 Android Application、Compose Compiler 和 Kotlin Serialization 插件。
- `apps/android/gradle/libs.versions.toml` 只保留 Android/Kotlin/Retrofit/OkHttp/Coil/Test 依赖，不出现 Spring Boot、MinIO 或 Testcontainers。
- `apps/android/gradle.properties` 保留 UTF-8、AndroidX、Kotlin 风格和 non-transitive R 配置。
- 当前 Gradle 9.1 Wrapper 原样迁移，不在结构重构中升级 AGP、Kotlin 或 Gradle。
- 本地 `local.properties` 若存在，移动到 `apps/android/local.properties`，继续保持 ignored，不进入提交。
- APK 路径变为 `apps/android/app/build/outputs/apk/debug/app-debug.apk`。

## 6. 后端构建边界

- `services/backend/settings.gradle.kts` 是唯一后端 settings，工程名为 `phonecar-backend`，只使用 Maven Central/Plugin Portal。
- `services/backend/gradle/libs.versions.toml` 只声明 Spring Boot、Dependency Management、springdoc、MinIO 和 Testcontainers。
- 后端构建命令从根路径任务 `:backend:test :backend:bootJar` 改为后端目录内的 `test bootJar`。
- 后端拥有自己的 Wrapper 和最小 `gradle.properties`，不读取 `apps/android` 或仓库根 Gradle 文件。
- Java 21、Jar 名 `phonecar-backend.jar`、group/version 及测试配置保持不变。

## 7. Docker 与运行边界

- `infra/docker-compose.yml` 的 backend build context 使用 `../services/backend`，Dockerfile 使用上下文内相对路径，不再复制仓库根或 Android 文件。
- Compose 增加顶层项目名 `phonecar`，避免配置文件移入 `infra/` 后默认 project name 改变，保持现有容器和命名卷前缀兼容。
- PostgreSQL、MinIO、backend 服务名、端口、健康检查、环境变量默认值和卷 key 保持不变。
- `services/backend/Dockerfile` 使用后端自己的 Wrapper 构建 `bootJar`，运行阶段继续使用 Java 21 JRE 和非 root 用户。
- 从仓库根执行统一命令：`docker compose -f infra/docker-compose.yml ...`。

## 8. 仓库级工具与文档

- `tools/verify-all.ps1` 依次在两个工程目录执行各自 Wrapper，并执行 Compose 配置校验；任一步失败立即退出非零状态。
- 设备测试不盲目创建模拟器：先通过 `adb devices` 检查已启动设备；存在时直接复用，不存在时才选择或创建 AVD。
- `tools/import-prototype-assets.ps1` 更新原型和 Android 资源目标路径，保持 UTF-8 无 BOM 写入行为。
- README 中的结构图、先决条件、构建、安装、测试、Docker 和产物路径全部更新。
- `.trellis/spec/android/` 与 `.trellis/spec/backend/` 更新到新路径和独立命令；归档任务文档作为历史记录不重写。
- `.gitignore` 使用可覆盖嵌套工程的模式，例如 `**/.gradle/`、`**/build/`、`**/local.properties`，继续忽略 `.env`、runtime、临时文件和密钥。

## 9. 兼容性与不变量

- Android `applicationId`、namespace、Kotlin/Java package、Manifest、资源名不变。
- 默认 API 地址继续为 `http://10.0.2.2:8080/api/v1/`，`PHONECAR_API_BASE_URL` 覆盖方式不变。
- 后端 URL、JSON、认证、Flyway migration、数据库/MinIO 数据不变。
- Compose project name 固定为 `phonecar`，避免仅因文件路径变化产生新的卷。
- 旧根命令不继续兼容；README 和 `tools/verify-all.ps1` 是新的明确入口。保留旧根 Gradle 会重新制造双重所有权，因此不采用。

## 10. 风险与控制

| 风险 | 控制措施 |
| --- | --- |
| Android Studio 找不到 SDK 或 module | 移动 ignored 的 `local.properties`，重新从 `apps/android` 导入工程 |
| 版本目录拆分遗漏 alias | 分别运行两端 Gradle 配置和完整构建，搜索未解析 `libs.*` |
| Docker 构建找不到 Wrapper/Jar | 后端上下文内包含完整 Wrapper，Dockerfile 使用固定 Jar 名 |
| Compose 迁移后创建新卷 | 固定顶层项目名 `phonecar`，对比 `docker compose config` 的最终资源名 |
| 文档或工具残留旧路径 | 对非归档文本执行全仓路径搜索，逐项核对有效引用 |
| Git 将移动识别为删除/新增 | 使用 `git mv` 做机械移动，避免同一步大改业务文件 |
| 用户 IDE 改动被混入 | 全程保留并排除已暂存的 `.idea/vcs.xml` |

## 11. 回滚策略

- 在提交前失败：停止后续步骤，按路径映射反向移动文件并恢复配置内容；不得使用 `git reset --hard` 或覆盖用户 `.idea/vcs.xml`。
- Android 独立构建失败：先修复 Android settings/catalog；不能通过重新引用后端或根共享 catalog 绕过。
- 后端独立构建失败：先修复后端 settings/catalog/Wrapper；不能恢复为根 `:backend` 子模块作为长期状态。
- Docker 失败：保留已通过的双工程结构，回滚 Docker/Compose 路径调整后单独修复；不得删除已有数据库或 MinIO 卷。
- 最终提交应是一个可独立构建的结构迁移提交，便于使用普通 `git revert` 回退。
