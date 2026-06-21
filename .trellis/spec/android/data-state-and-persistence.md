# 数据、状态与持久化

## 状态流

项目采用单向数据流：

```text
Spring Boot API -> Remote Repository -> confirmed snapshot
                                 -> DemoStateStore
Preferences DataStore / InMemoryDemoStateStore
    -> Flow<DemoState>
    -> PhoneCarViewModel.state (StateFlow)
    -> PhoneCarApp collectAsStateWithLifecycle()
    -> Screen(state, onEvent...)
    -> ViewModel -> domain Repository -> API 或 DemoStateStore.update
```

证据文件：`data/local/DemoStateStore.kt`、`data/repository/Repositories.kt`、`ui/PhoneCarViewModel.kt`、`ui/PhoneCarApp.kt`。

## 模型规则

- `DemoState` 是不可变 `data class`；交互状态用 `copy` 生成新值，不暴露可变集合。
- 受范围限制的值通过模型方法或 Repository 入口收敛，而不是只依赖 Slider 等 UI 控件。现有方法将风量限制为 1..5、车窗限制为 0..100、温度限制为 16f..30f。
- 可推导数据使用只读计算属性。车辆总价由基础价与轮毂加价计算，不另存一份易失配状态。
- 有限选项使用 enum，并以 enum 名写入 Preferences；新增 enum 值必须保持旧持久化值可读。

参考：`data/model/DemoState.kt` 和 `DemoStateTest.kt`。

## Repository 接口风格

- 接口按功能域划分：`VehicleRepository`、`ContentRepository`、`ServiceRepository`、`ProfileRepository`。
- 读取契约统一暴露 `val state: Flow<DemoState>`；修改契约使用表达意图的 `suspend fun`，例如 `setVehicleLocked`、`selectPaint`、`confirmReservation`。
- 布尔赋值方法使用 `set<Name>(enabled/value)`；选择项使用 `select<Name>`；真正的反转操作才使用 `toggle<Name>`。
- 本地默认实现只通过 `DemoStateStore.update { current -> ... }` 改写状态。远端实现只在 API 成功后写入确认快照；失败保留缓存并暴露可重试错误。
- Composable 不依赖 Repository 接口；ViewModel 是 UI 和数据层之间的唯一当前桥接点。

## ViewModel 规则

- 使用 `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DemoState())` 将 Store Flow 转成 UI StateFlow。
- 对外事件方法是非挂起函数；统一经私有 `launch` 在 `viewModelScope` 中调用 Repository。
- Factory 由 `AppContainer` 提供，Activity 通过 `viewModels` 获取实例。不要在 Activity 或 Composable 中手动构造生产 Store。

## Preferences DataStore 规则

- DataStore 委托定义在顶层 Context 扩展上，并由 Application Context 进入 `AppContainer`（`AppContainer.kt`）。
- 每个 `DemoState` 持久字段在 `PreferenceKeys` 中有显式 typed key，并同时出现在 `writeTo` 与 `toDemoState`。
- 缺失字段以 `DemoState()` 的默认值恢复；未知 enum 名称回退到对应默认项。该行为由 `DemoStatePreferencesTest` 覆盖。
- 更新在 `writeMutex.withLock` 中执行，读取当前状态、变换后一次性清空并重写完整快照，保持并发写入串行。
- 测试使用 `InMemoryDemoStateStore`，不让 Repository 单测依赖 Android DataStore 或设备。

新增持久字段时必须成组更新：`DemoState` 默认值、`PreferenceKeys`、`writeTo`、`toDemoState`、round-trip 测试；若字段有范围约束，再增加模型或 Repository 边界测试。

## 认证、网络和缓存边界

- access token 只保存在内存；refresh token 使用 Android Keystore AES/GCM 加密后落盘。
- OkHttp interceptor 添加 Bearer token；Authenticator 对并发 `401` 单飞刷新并最多重试一次，失败清会话。
- 登录后从服务端刷新同步字段；旧匿名业务状态不得上传。退出清 token、远端缓存和发现内容，同时保留发现标签、摄像头角度、未提交车型/日期等本地 UI 状态。
- 车辆 PATCH 携带服务端 `version`；`409` 后拉取权威状态并要求用户重试。
- Coil 对预签名图片使用 `mediaId` 作为 memory/disk cache key，不能使用会变化的 URL 作为稳定键。

## 错误和日志边界

- 网络异常不能进入 Composable；Repository 映射为用户可重试错误。后端 `ProblemDetail.code` 是稳定机器契约，未知 code 使用通用提示。
- `PreferencesDemoStateStore.state` 只把 `IOException` 当作可恢复的本地读取故障并回退为空 Preferences；其他异常继续抛出。不要用宽泛 `catch` 静默吞掉编程错误。
- 非法 enum 持久化值属于兼容性输入，通过 `enumOrNull` 回退，而不是让启动崩溃。
- `PhoneCarViewModel.syncError` 暴露网络错误提示；不要在 Composable 中直接 `try/catch` Retrofit 异常。
