# 架构与目录

## 当前技术栈

- Android 位于 `apps/android/`，是只包含 `:app` 的独立 Gradle 工程；Java 后端位于独立的 `services/backend/` 工程。
- Kotlin 2.3.21、Jetpack Compose、Material 3、Navigation Compose 2.9.8。
- ViewModel + Kotlin Flow 管理状态，Preferences DataStore 1.2.1 保存本地 UI 状态和最近确认缓存。
- Retrofit 3、OkHttp 5 与 Kotlinx Serialization 负责 API；Coil 3 加载发现内容预签名图片。
- JUnit 4 执行 JVM 测试，AndroidX Test + Compose UI Test 执行设备测试。
- `minSdk 24`、`targetSdk 36`，Java 源/目标兼容级别为 11（`apps/android/app/build.gradle.kts`）。
- `MainActivity` 是唯一 Activity；`PhoneCarApplication` 和 `AppContainer` 完成轻量手工依赖装配。

Android 版本号以 `apps/android/gradle/libs.versions.toml` 为准，不读取后端 version catalog，也不在源码或其他 Gradle 文件重复硬编码。

## 目录职责

```text
apps/android/app/src/main/
├── AndroidManifest.xml
├── java/com/lautung/phonecar/
│   ├── AppContainer.kt                # DataStore 和 ViewModelFactory 装配
│   ├── PhoneCarApplication.kt         # Application 级容器入口
│   ├── MainActivity.kt                # edge-to-edge 与 Compose 根入口
│   ├── data/
│   │   ├── model/                     # 不可变 UI 状态、枚举、纯状态变换
│   │   ├── local/                     # DataStore 映射、状态存储接口与内存实现
│   │   ├── auth/                      # AuthState、会话与 Keystore refresh token
│   │   ├── remote/                    # Retrofit API、DTO、拦截器和单飞刷新
│   │   └── repository/                # 本地 UI 与远端业务 Repository
│   └── ui/
│       ├── components/                # 跨页面复用的 Composable
│       ├── navigation/                # AppRoute 等导航契约
│       ├── screens/                   # 页面及仅在同文件使用的私有子组件
│       └── theme/                     # 颜色、排版和 MaterialTheme
├── res/drawable*/                     # 本地 Solar 图标和原型图片
└── res/values/                        # 应用元数据和 Android 主题
```

测试镜像生产包路径：纯 Kotlin/数据测试放在 `apps/android/app/src/test/java`，Compose 导航与页面测试放在 `apps/android/app/src/androidTest/java`。

## 放置规则

- 纯数据、默认值、派生值和范围约束放入 `data/model`。示例：`DemoState.totalVehiclePrice`、`withFanLevel`。
- Preferences key、序列化和容错恢复放入 `data/local`。示例：`DemoStatePreferences.kt`。
- 面向功能域的状态变更放入 `data/repository`，不要从 Composable 直接写 DataStore。示例：`DefaultVehicleRepository`、`DefaultServiceRepository`。
- ViewModel 只负责暴露生命周期安全状态和把 UI 事件转发到 Repository。示例：`PhoneCarViewModel.kt`。
- 公共视觉原语放入 `ui/components`；只服务于一个页面组的小组件与页面同文件并声明为 `private`。示例：公共 `PrimaryActionButton`，局部 `WheelChoice`、`MetricCard`。
- 当前页面按业务区域成组：顶层、主页/座舱、车控详情、内容详情、服务详情、个人详情。新增页面优先放入对应 `*Screens.kt`；文件变得难以浏览时再按页面拆分，不创建空层级。

## 命名规范

- 包名全小写：`com.lautung.phonecar.ui.screens`。
- 类型和 Composable 使用 PascalCase：`DemoState`、`VehicleHomeScreen`、`ConfirmActionDialog`。
- 函数、属性和回调参数使用 camelCase：`setCabinTemperature`、`onCheckedChange`。
- 枚举项和 Preference key 属性使用大写下划线：`PERFORMANCE_21`、`CABIN_TEMPERATURE`；持久化字符串 key 使用小写下划线：`cabin_temperature`。
- 页面函数以 `Screen` 结尾；Repository 默认实现以 `Default` 开头；测试类以被测对象加 `Test` 命名。
- Drawable 使用小写下划线；Solar 图标沿用 `ic_solar_<name>_bold.xml`，原型位图使用描述性名称，如 `vehicle_configurator.jpg`。

## 不适用的模式

- Android 不直接依赖 backend Java 类、JPA Entity 或 migration；跨模块只通过 `/api/v1` JSON 契约交互。
- 不要为单一实现引入依赖注入框架。现有装配入口是 `AppContainer`，替换它应由明确的架构任务驱动。
- 不要把业务状态写入 Activity、NavController 或 Composable 的长期 `remember` 状态；这些状态必须沿现有数据流持久化。
- 不要把新依赖直接写成散落版本号；先增加 version catalog 条目。
- Release 默认禁止明文 HTTP；本地明文访问只放在 Debug manifest，API base URL 通过 `PHONECAR_API_BASE_URL` 覆盖。
