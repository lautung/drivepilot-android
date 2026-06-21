# 探索项目完整性与下一步

## Goal

基于当前仓库而不是历史原型，把 PhoneCar 建设为接近真实生产环境、可通过代码与演示验证的简历项目，识别影响“可发布、可部署、可观测、可恢复、可持续开发”的工程缺口，并给出有依据的下一步优先级。

## Requirements

- 盘点 Android、后端、基础设施、文档和测试目前已经实现的能力。
- 从代码、测试、配置、历史任务和可执行验证中区分“已实现”“仅有界面/接口”“未接通”“未验证”。
- 优先发现阻断本地端到端运行、核心用户流程和后续维护的缺口，避免只按 TODO 注释判断。
- 输出分阶段路线图，明确每一阶段的用户价值、依赖、风险和完成证据。
- 生产化范围必须覆盖 Android Release、后端公网部署、安全基线、数据备份恢复、可观测性、运维手册和真实端到端验证。
- 首个里程碑是生产级练手项目，不承担真实商业运营、邀请制用户管理或应用商店公开审核；完成度以代码、自动化验证和在线环境为准。
- 简历定位为纯全栈岗位；项目需要同时提供可评审的前端、后端、数据库、测试、交付和运维证据，不能只以 Android 客户端代表全部前端能力。
- 新增独立 Web Admin 管理端并采用前后端分离架构：前端独立工程、独立构建，只通过版本化 HTTP/JSON API 调用 Spring Boot，不在 Web 框架中复制后端业务逻辑。
- Admin 前端采用 Vue 3 + TypeScript + Vite；使用 Vue Router 管理路由、Pinia 管理认证和局部客户端状态、TanStack Vue Query 管理服务端状态、Element Plus 提供后台组件。
- 提供公网 HTTPS 演示环境：公开只读 Admin 演示账号、仅项目维护者持有的私有管理员账号、每日重置的演示数据；Android APK 与 Admin 共用同一套 API。
- 生产部署采用单台 Linux VPS 承载反向代理和后端容器，外接托管 PostgreSQL 与托管 S3 兼容对象存储，通过正式域名和 HTTPS 提供服务；首期不使用 Kubernetes。
- 本任务当前只做探索和规划；未经用户批准，不修改业务代码，不进入实现阶段。

## Confirmed Facts

- 当前仓库包含 `apps/android/` 独立 Android Gradle 工程、`services/backend/` 独立 Spring Boot 工程、`infra/` 和统一验证脚本。
- Android 已有 Compose 页面、认证/网络/本地持久化、Repository 和 JVM/设备测试；后端已有认证、用户、车辆状态、发现内容、服务预约、媒体管理及集成测试。
- 当前工作树在创建本任务前是干净的，任务创建后仅新增本任务目录。
- 当前代码没有显著业务 `TODO/FIXME`；完整度必须通过端到端契约、运行验证和产品流程核对判断。
- 2026-06-21 实测完整质量门通过：Android JVM 测试、lint、Debug APK、已有模拟器上的 5 个设备测试、后端测试与 bootJar、Docker Compose 配置全部成功。
- Docker 中 Spring Boot、PostgreSQL、MinIO 均为 healthy；复用 `emulator-5554` 安装 Debug APK后，真实注册可进入首页，车辆解锁从数据库 `vehicle_locked=true/version=0` 更新为 `false/version=1`。
- 当前设备测试全部通过 `PhoneCarViewModel(InMemoryDemoStateStore())` 绕过真实认证和网络，因此没有自动覆盖 App→后端端到端流程。
- 后端只有 3 个集成测试方法，尚未自动覆盖预约/维保/订阅完整契约、内容关注、媒体上传与删除、管理员内容生命周期等主要接口。
- Android 仅创建预约和维保记录，不读取后端已有的查询接口；登录刷新会把 `reservationConfirmed` 和 `maintenanceBooked` 重置为 `false`，无法在重启/换设备后反映服务端记录。
- Android 已声明发现内容关注 API，但当前直播关注仍写入本地 `DemoState`，没有调用后端关注接口。
- 进程重启时若 refresh token 因断网刷新失败，`AuthRepository.restore()` 会清除会话并进入退出状态；这与 README 所述“断网读取最近缓存”目标存在冲突。
- 网络同步把车辆、偏好、订阅、发现内容四个请求绑定为一次全有或全无刷新，错误统一为单条网络提示，缺少分域加载、重试和提交中防重状态。

## Acceptance Criteria

- [x] 给出当前系统能力矩阵，结论能够定位到具体代码、配置或测试。
- [x] 运行适合当前环境的静态/自动化验证，并记录无法运行项目的明确原因。
- [x] 列出按影响和依赖排序的缺口，区分阻断项、MVP 项和后续增强项。
- [x] 给出推荐的首个实施任务，范围足够小且具有可验证的完成标准。
- [x] 将最终探索结论持久化到 Trellis 规划产物，并由用户确认是否进入实现。

## Out of Scope

- 本轮不直接实现功能或重构现有架构。
- 不接入真实车辆控制、支付、定位、相机或蓝牙能力。
- 不把原型页面数量本身当作后端业务完整度要求。
- 不实现邀请码、真实商业运营、支付、客服工单或应用商店审核流程。
- 不单独制作简历展示文档、演示视频或简历话术；README、部署说明和恢复手册等必要工程文档不在此限制内。

## Open Questions

- [已决策] 完整度目标选择“可发布/生产化产品”，不止于端到端演示 MVP。
- [已修正] 项目是练手和简历加分项目，不需要邀请码或真实邀请制运营；此前“邀请制生产内测”决策作废。
- [已决策] 采用 Linux VPS + 托管 PostgreSQL + 托管对象存储 + HTTPS 域名的最小生产架构。
- [已决策] 简历主要面向纯全栈岗位，不以 Android 专项岗位为主。
- [已决策] 新增独立 Admin 管理端，采用前后端分离实现。
- [已决策] Admin 使用 Vue 3/TypeScript/Vite 技术栈，不使用 React 或 Next.js。
- [已决策] 提供公网演示入口，公开账号只读，写权限只授予私有管理员，演示数据定时重置。
- 当前无阻塞性产品问题；等待用户评审完整路线图后再创建并启动第一个实施子任务。

## Notes

- Keep `prd.md` focused on requirements, constraints, and acceptance criteria.
- Lightweight tasks can remain PRD-only.
- For complex tasks, add `design.md` for technical design and `implement.md` for execution planning before `task.py start`.
