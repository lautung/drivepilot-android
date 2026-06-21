# Android 与后端独立构建实施计划

## 1. 实施前基线

- [x] 记录 `git status --short`，确认并保护用户已暂存的 `.idea/vcs.xml`。
- [x] 确认 PowerShell 7、Java 21、Android SDK/adb、Docker 和 Docker Compose 可用；兼容问题才退回 Windows PowerShell 5。
- [x] 执行当前 Android JVM 测试、lint、Debug 构建与后端测试/bootJar，建立迁移前基线。
- [x] 执行当前 `docker compose config --quiet`，记录有效服务、project name 和卷名。
- [x] 搜索所有有效的 `app/`、`backend/`、根 Gradle、Wrapper 和 Compose 路径引用；归档任务只作为历史证据，不修改。

## 2. 拆出 Android 独立工程

- [x] 创建 `apps/android/`，使用 `git mv` 将 `app/` 迁移为 `apps/android/app/`。
- [x] 将根 `settings.gradle.kts`、`build.gradle.kts`、`gradle.properties`、Wrapper 和相关 Gradle 文件迁移到 `apps/android/`。
- [x] 将 settings 改为只包含 `:app`，从根插件声明和 version catalog 删除全部后端项。
- [x] 若根 `local.properties` 存在，将其移动到 `apps/android/local.properties` 并确认仍被忽略。
- [x] 从 `apps/android/` 运行 `./gradlew.bat projects` 和 `./gradlew.bat assembleDebug`，先验证工程发现和最小构建。

## 3. 拆出后端独立工程

- [x] 创建 `services/`，使用 `git mv` 将 `backend/` 迁移为 `services/backend/`。
- [x] 给后端增加独立 Gradle 9.1 Wrapper、最小 `gradle.properties` 和专用 `gradle/libs.versions.toml`。
- [x] 调整后端 settings，使 version catalog 只读取工程内文件；确认不存在对仓库根或 Android 路径的引用。
- [x] 从 `services/backend/` 运行 `./gradlew.bat projects` 和 `./gradlew.bat bootJar`，先验证独立工程与固定 Jar 名。
- [x] 删除仓库根遗留的 Gradle build/settings/Wrapper/catalog，确认根目录不再是 Gradle 工程。

## 4. 迁移基础设施与仓库工具

- [x] 使用 `git mv` 将 `docker-compose.yml`、`.env.example` 移入 `infra/`。
- [x] Compose 固定 project name 为 `phonecar`，将后端 build context 改为 `../services/backend`，保持服务、端口、环境变量和卷 key 不变。
- [x] 重写后端 Dockerfile，使其只复制 `services/backend` 构建上下文内文件并使用后端 Wrapper。
- [x] 新增 `tools/verify-all.ps1`：分别执行 Android、后端和 Compose 校验，使用严格错误处理和 UTF-8 输出。
- [x] 将原型 HTML 移到 `docs/prototype/`，更新 `tools/import-prototype-assets.ps1` 的输入和资源输出路径。

## 5. 更新忽略规则、文档和规范

- [x] 更新 `.gitignore`，覆盖所有嵌套 `.gradle`、`.kotlin`、`build`、`local.properties`、`.env`、runtime、临时文件和密钥路径。
- [x] 更新 `README.md` 与 `README.zh-CN.md` 的目录图、环境要求、Android/后端独立命令、Docker 命令、API 覆盖和 APK 路径。
- [x] 更新 `.trellis/spec/android/` 中的源码、测试、构建文件、version catalog 和命令路径。
- [x] 更新 `.trellis/spec/backend/` 中的独立构建命令和目录说明；不改 API/存储契约语义。
- [x] 扫描 `.codex/`、根工具和其他活动文档，修复仍指向旧路径的有效配置；不修改 `.trellis/tasks/archive/`。

## 6. 完整验证

### Android 独立构建

```powershell
Set-Location apps/android
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

- [x] 上述命令全部通过，APK 位于 `apps/android/app/build/outputs/apk/debug/app-debug.apk`。
- [x] 运行 `adb devices`；若已有 `device` 状态的模拟器则直接复用，没有时才从现有 AVD 启动或创建 AVD。
- [x] 在可用模拟器上运行 `.\gradlew.bat connectedDebugAndroidTest`，确认现有导航和页面测试通过。

### 后端独立构建

```powershell
Set-Location services/backend
.\gradlew.bat test bootJar
```

- [x] 测试通过，生成 `services/backend/build/libs/phonecar-backend.jar`。

### Docker 与集成配置

```powershell
docker compose -f infra/docker-compose.yml config --quiet
docker compose -f infra/docker-compose.yml build backend
docker compose -f infra/docker-compose.yml up -d
docker compose -f infra/docker-compose.yml ps
```

- [x] Compose 配置与 backend 镜像构建通过。
- [x] PostgreSQL、MinIO、backend 健康，`/actuator/health` 与 `/v3/api-docs` 可访问。
- [x] 不删除或重建用户已有的持久化卷；验证结束后只停止本任务启动的容器，不执行 `down -v`。

### 边界与仓库卫生

- [x] `apps/android` 的 settings/catalog 不包含后端插件或依赖。
- [x] `services/backend` 的 settings/catalog/Dockerfile 不引用 Android 或仓库根 Gradle 文件。
- [x] 根目录不存在 `settings.gradle.kts`、`build.gradle.kts`、`gradlew*`、`gradle.properties` 或 `gradle/`。
- [x] 非归档有效文档和脚本不再引用旧 `app/`、`backend/` 或根 Gradle 命令。
- [x] `git status --short` 中没有 build、runtime、`.env`、secret、临时文件或意外 IDE 文件。
- [x] `tools/verify-all.ps1` 在 PowerShell 7 下完整通过。

## 7. 审查与提交门禁

- [x] 对照 `prd.md` 验收标准逐项核对，确认没有业务代码或 API 行为变化。
- [x] 检查 `git diff --summary` 的 rename 结果和 `git diff --check`，避免空白、编码和误删除问题。
- [x] 更新 Trellis spec 后运行必要检查；保存验证证据到任务上下文。
- [x] 只暂存本任务文件，明确排除 `.idea/vcs.xml`、构建产物和本地配置。
- [x] 形成一个原子结构迁移提交；另加一个仅修正两端 Unix Wrapper 可执行位的补充提交。提交前已核对 Docker volume 与 secret 安全边界。

## 8. 回滚点

- **R1：Android 最小构建失败** — 在继续后端迁移前修复或反向移动 Android 路径。
- **R2：后端最小构建失败** — 在删除根 Gradle 文件前修复独立 settings/catalog/Wrapper。
- **R3：Compose 配置或镜像失败** — 保留已验证的双工程结构，只回滚 `infra/` 和 Dockerfile 路径变更。
- **R4：完整检查发现行为变化** — 回退对应配置或文档，不通过业务代码修改掩盖结构迁移问题。

## 9. 实际验证结果

- `pwsh -NoProfile -File .\tools\verify-all.ps1 -IncludeDeviceTests`：通过；Android 单元测试、lint、Debug 构建及 `emulator-5554` 上 5 项设备测试成功。
- `services/backend/.\gradlew.bat test bootJar`：通过；固定 Jar 已生成。
- `docker compose -f infra/docker-compose.yml build backend`：通过；Docker context 仅包含独立后端工程。
- `docker compose -f infra/docker-compose.yml up -d`：沿用 `phonecar` 项目名，PostgreSQL、MinIO、backend 均为 healthy。
- `GET /actuator/health`：`UP`；`GET /v3/api-docs`：OpenAPI `3.1.0`。
- 工作提交：`8079b1e refactor: separate Android and backend builds`。
- Wrapper 模式修正：`ed51649 fix: make Gradle wrappers executable`。
- 用户原有 `.idea/vcs.xml` 未进入上述提交，暂存 blob 在提交前后保持一致。
