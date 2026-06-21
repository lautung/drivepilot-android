# 测试与质量

## 测试分层

### JVM 单元测试

放在 `app/src/test/java`，覆盖不依赖 Android 运行时的规则：

- `DemoStateTest`：默认值、派生价格、边界收敛、集合 toggle。
- `DemoStatePreferencesTest`：完整 round-trip、空 Preferences 默认值、非法 enum 回退。
- `RepositoriesTest`：使用 `InMemoryDemoStateStore` 验证功能域接口共享同一状态和边界规则。
- `AuthRepositoryTest`：登录/退出、会话恢复、refresh 轮换和并发 `401` 单飞刷新。

协程接口当前用 `runBlocking` 驱动。测试遵循 Arrange / Act / Assert 的空行分组，并使用 JUnit 4 `assertEquals`、`assertTrue`、`assertFalse`。

### Compose 设备测试

放在 `app/src/androidTest/java`，通过 `createComposeRule()` 直接渲染：

```kotlin
val viewModel = PhoneCarViewModel(InMemoryDemoStateStore())
composeRule.setContent { PhoneCarTheme { PhoneCarApp(viewModel) } }
```

- `PhoneCarNavigationTest` 验证四个顶层入口、详情页返回和具体布局回归。
- `AllScreensReachableTest` 遍历 16 个页面，必要时用 `performScrollToNode` 找到离屏入口。
- 优先通过用户可见文本和 `contentDescription` 查找节点；需要稳定结构或几何断言时才使用 `testTag` 和 unmerged tree。

`ExampleUnitTest` 与 `ExampleInstrumentedTest` 是 Android 模板烟雾测试，不是新测试的风格基准；新测试应参考上述领域测试和导航测试。

## 测试命名

- 测试类：`<Subject>Test`。
- 测试方法使用 camelCase，并用一个下划线分隔场景和期望，例如 `boundedControls_areClampedToPrototypeRanges`、`homeQuickAction_opensCabinAndBackReturnsHome`。
- 失败消息说明必须满足的用户可见约束；布局回归示例见 `discoverFeatureMetadata_staysInsideRoundedCard`。

## 改动与最小验证集

| 改动 | 必须验证 |
| --- | --- |
| `DemoState` 纯逻辑/默认值 | `testDebugUnitTest`，更新 `DemoStateTest` |
| Preferences 字段或 enum | `testDebugUnitTest`，更新 round-trip 与兼容性测试 |
| Repository/ViewModel 状态操作 | `testDebugUnitTest`，补充 Repository 测试 |
| Auth/Retrofit/DTO/缓存行为 | `testDebugUnitTest`，使用 MockWebServer，覆盖失败和并发刷新 |
| Compose 页面/公共组件 | `lintDebug`、`assembleDebug`；交互或语义变化再跑设备测试 |
| 路由、底栏、入口或返回栈 | `connectedDebugAndroidTest`，更新两类导航测试 |

PowerShell 7 下的项目命令：

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
.\gradlew.bat connectedDebugAndroidTest
```

运行应用或设备测试前先执行 `adb devices` 检查已启动的 Android 虚拟器；存在可用 emulator 时直接复用，仅在没有可用虚拟器时创建新的。

## 评审检查

- 新交互是否沿 `Screen -> ViewModel -> Repository -> Store` 更新，而不是形成第二状态源。
- 新持久字段是否同步更新模型、key、写入、读取和 round-trip 测试。
- 新路由是否使用 `AppRoute`、有可发现入口、正确返回，并纳入全页面可达测试。
- 可点击图标是否有语义，装饰图是否避免错误描述，测试定位是否面向用户行为。
- 是否保持缓存只读、在线写入、手机竖屏和高保真原型边界，且没有新增无授权权限或外部副作用。
- 是否复用了主题色、Solar 图标和公共组件，而不是复制局部实现。
- `lintDebug`、相关测试和构建结果是否通过；只记录实际运行过的命令。

## 禁止模式

- JVM 单测不允许用 `Thread.sleep`、真实网络、真实车辆/支付/定位依赖制造不稳定性；真实 Compose 联调只作为独立端到端冒烟。
- 不允许删除断言或降低语义可访问性来让 UI 测试通过。
- 不允许只验证“能点击”而不验证目标页面或状态结果；导航测试必须断言用户可见结果。
- 不允许把模板 `addition_isCorrect` 当作业务测试覆盖率。
