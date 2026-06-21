# Android 与后端独立构建重构

## 目标

在保留单一 Git 仓库和现有业务行为的前提下，将 Android 客户端与 Spring Boot 后端改造成两个目录、构建配置和验证流程均独立的工程，使根目录只承担仓库级文档、基础设施和统一编排职责。

## 已确认事实

- 用户已选择“单仓库、双独立工程”方案。
- 当前 Android 源码位于 `app/`，后端源码位于 `backend/`，源码本身没有交叉依赖。
- 当前根 `settings.gradle.kts` 同时包含 `:app` 与 `:backend`，两端共享根 Gradle Wrapper、插件声明和版本目录。
- `backend/settings.gradle.kts` 又允许后端以独立 Gradle build 运行，因此当前存在“根子模块 + 独立工程”的双重身份。
- 当前 Dockerfile 依赖仓库根 Wrapper 和版本目录，Docker Compose 也位于仓库根目录。
- Android 与后端只通过 `/api/v1` HTTP/JSON 交互；Android 不直接依赖后端 Java 类、Entity 或 migration。
- 当前工作区有用户已暂存的 `.idea/vcs.xml`，本任务不得覆盖、撤销或提交该无关改动。

## 要求

- Android 工程迁移到 `apps/android/`，并拥有独立的 `settings.gradle.kts`、根构建文件、Gradle Wrapper、版本目录和 Android 专用 `gradle.properties`。
- 后端工程迁移到 `services/backend/`，并拥有独立的 `settings.gradle.kts`、构建文件、Gradle Wrapper、版本目录和后端专用构建配置。
- 仓库根目录不再作为 Gradle 多项目构建；根目录只保留仓库级 README、Trellis 配置、文档、基础设施和编排脚本。
- Docker Compose 与环境变量示例迁移到 `infra/`；后端镜像构建上下文只能依赖后端工程，不再复制 Android 构建文件。
- 根 `tools/` 提供 UTF-8 PowerShell 7 优先的统一验证/开发入口，同时保留两端可以独立执行的原生命令。
- 更新中英文 README、Trellis Android/Backend 规范、工具脚本和所有有效路径引用。
- 更新 `.gitignore`，覆盖两个工程的 Gradle、构建产物、Android 本地配置、运行时文件、临时文件和密钥；不得提交真实 `.env`、secret、runtime 或临时产物。
- 迁移必须保持 Android applicationId、包名、API 基础地址行为、后端 API、数据库 migration、Docker 服务名和持久化卷兼容。
- 文件移动优先保留 Git 历史；第一阶段不重构业务类、Compose 页面、DTO、Controller 或数据库模型。

## 验收标准

- [x] `apps/android/` 可不依赖后端 Gradle 配置独立完成 JVM 测试、lint 和 Debug APK 构建。
- [x] 检测到已运行 Android 虚拟机时，直接复用该虚拟机完成现有设备测试；没有虚拟机时才创建。
- [x] `services/backend/` 可不依赖 Android Gradle 配置独立完成测试和 `bootJar` 构建。
- [x] `infra/docker-compose.yml` 配置校验通过，并可从 `services/backend/` 构建后端镜像。
- [x] Android 与后端的 Gradle 设置、版本目录和构建输出互不引用。
- [x] 仓库根不存在同时包含 Android 与后端的 `settings.gradle.kts` 或 `build.gradle.kts`。
- [x] README 和工具脚本中的所有构建、安装、测试、Docker 命令与新目录一致。
- [x] `.gitignore` 能阻止两个工程的本地配置、构建产物、runtime、临时文件和密钥进入版本控制。
- [x] 现有 Android 单元测试、lint、Debug 构建、设备导航测试、后端测试及 Docker Compose 配置检查均通过。
- [x] Git 变更不包含 `.idea/vcs.xml` 或其他与本任务无关的用户改动。

## 非目标

- 不修改现有业务功能、页面视觉、API 语义、数据库 schema 或认证流程。
- 不拆分为多个 Git 仓库。
- 不在本任务中引入新的依赖注入框架、业务模块化或微服务。
- 不在本任务中增加静态 OpenAPI 契约快照、客户端代码生成或契约漂移校验；继续保留现有运行时 `/v3/api-docs`，契约治理另立任务处理。
- 不提交运行时数据、构建产物、IDE 本地状态或真实凭据。

## 已确认决策

- 本次只处理结构和构建边界，不同时引入静态 OpenAPI 契约与漂移校验。
- 迁移采用一个原子任务完成，因为 Android 路径、后端路径、Docker 上下文、根脚本和文档命令必须同步切换，拆成可独立启动的子任务会留下不可构建的中间状态。
