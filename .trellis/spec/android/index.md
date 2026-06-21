# Android 开发规范

本目录描述 PhoneCar Android 客户端的真实开发方式。客户端通过 Retrofit 调用 Spring Boot 后端，并在 Compose UI、ViewModel、Repository、网络 DTO 和 Preferences DataStore 之间保持明确边界。

## 规范索引

| 文档 | 适用范围 |
| --- | --- |
| [架构与目录](./architecture-and-directory.md) | 技术栈、模块边界、目录和命名 |
| [数据、状态与持久化](./data-state-and-persistence.md) | 不可变状态、Repository 接口、DataStore、错误边界 |
| [Compose UI](./compose-ui.md) | 页面、组件、视觉资源、语义和局部状态 |
| [导航与事件接口](./navigation-and-events.md) | 路由、导航栈、状态提升和回调命名 |
| [测试与质量](./testing-and-quality.md) | JVM 单测、Compose 设备测试、验证命令 |

## 开发前检查

根据改动范围读取对应文档：

- 新增或移动 Kotlin/资源文件：先读“架构与目录”。
- 修改 `DemoState`、Repository、ViewModel 或 DataStore：读“数据、状态与持久化”。
- 修改页面或公共 Composable：读“Compose UI”。
- 新增页面、入口或返回行为：同时读“导航与事件接口”和“测试与质量”。
- 任何业务改动完成后：按“测试与质量”选择最小验证集；导航或语义改动必须运行设备测试。

共享思考指南仍位于 `../guides/`。跨 UI、状态、持久化三层的改动还应阅读 [跨层思考指南](../guides/cross-layer-thinking-guide.md)。

## 项目硬约束

- 源码、脚本和文档使用 UTF-8；Kotlin 使用官方代码风格（`gradle.properties`）。
- 当前产品是联网同步、支持缓存读取的高保真手机竖屏原型；只允许调用 PhoneCar 演示后端，不得加入真实车控、支付、定位、相机或蓝牙行为。
- 网络业务写入必须成功后再更新确认状态；断网时只读最近缓存，不实现离线写入队列。
- Android 是 `apps/android/` 下的独立 Gradle 工程，只包含 `:app`，不得读取后端 Gradle 配置。
- 依赖通过 `apps/android/gradle/libs.versions.toml` 集中声明，应用依赖写在 `apps/android/app/build.gradle.kts`。
- 不修改与任务无关的生成文件、IDE 配置或原型素材。
