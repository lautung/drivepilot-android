# Compose UI 规范

## 页面契约

- 页面 Composable 接收只读 `DemoState` 或最小只读字段，以及以 `on...` 命名的事件回调；页面不获取 ViewModel、Store 或 NavController。
- 参数顺序遵循“状态/展示数据在前，导航和操作回调在后”。示例：`MaintenanceScreen(state, onBack, onService, onDay, onConfirm)`、`PrivacyScreen(state, onBack, onLocation, onCamera, onClear)`。
- `PhoneCarApp` 是唯一收集 `PhoneCarViewModel.state` 并把方法引用传给页面的组合根。
- 可持久交互不得只保存在 `remember`。当前 `remember { mutableStateOf(...) }` 仅用于确认弹窗是否显示等瞬时 UI 状态；确认后的业务结果通过回调进入 ViewModel。

## 组件分层

- 跨页面重复的视觉/交互原语放在 `ui/components/PrototypeComponents.kt`：`PrototypeTopBar`、`QuickAction`、`SettingsToggleCard`、`PrimaryActionButton`、`ConfirmActionDialog`。
- 只被同一页面组使用的组件保持 `private` 并与页面同文件，例如 `WheelChoice`、`ServiceChoice`、`PrivacyToggle`。
- 可复用组件接受展示值、事件 lambda，并在需要布局组合时接受 `Modifier`。`Modifier` 应作用于组件最外层；不要在组件内部覆盖调用方传入的尺寸或 weight。
- 图标资源参数使用 `@DrawableRes` 标注。参考 `QuickAction` 和 `SettingsToggleCard`。

## 视觉风格

- 根主题固定为浅色 `PhoneCarTheme`；品牌色和 Slate 色阶集中在 `ui/theme/Color.kt`，Material 配色在 `Theme.kt`。
- 优先复用 `BrandBlue`、`Slate*`、`AlertRed`、`SuccessGreen` 等主题常量。只属于单一场景的色值可局部声明，如深色直播/哨兵背景和浅蓝提示卡。
- 页面主要使用白色背景、16–24dp 圆角卡片、`LazyColumn` 与 14–20dp 间距。深色场景仍沿用相同的圆角、层级和本地资源风格。
- 原型强调本地高保真展示：图片通过 `painterResource` 加载，Solar 矢量图标不替换成近似 Material 图标。新资源延续 `drawable-nodpi` 位图和 `ic_solar_*_bold` 矢量命名。
- 当前原型文案直接以中文字符串写在 Composable 中，`strings.xml` 只承载应用名等 Android 元数据。除非任务明确要求本地化，不要在局部改动中混做全量字符串迁移。

## 交互与语义

- 可点击图标提供可用于测试和无障碍识别的中文 `contentDescription`，例如“返回”“分享”“车辆图片，进入车体控制”。纯装饰图片/图标使用 `null`。
- 图标加文字构成一个动作时可使用 `semantics(mergeDescendants = true)`，`QuickAction` 是基准实现。
- 文本不足以稳定定位或需要验证几何边界时使用语义明确的 `testTag`。当前 `discover_feature_card` 用于回归卡片裁剪范围。
- 破坏性或提交类操作先显示 `ConfirmActionDialog`，确认后才调用业务回调。参考预定、维保、取消救援和清理缓存流程。
- 开关、Slider 和选择卡片的选中状态必须由传入 state 决定，事件只上报目标值；不要在页面内维护与持久状态重复的副本。

## 页面布局习惯

- 带返回的普通详情页使用 `PrototypeTopBar`；仅深色沉浸页可定义同文件私有深色 TopBar。
- 长内容使用 `LazyColumn` 和显式 `contentPadding` / `verticalArrangement`，保证设备测试可滚动查找离屏入口。
- 页面级 Composable 保持可直接由测试渲染：不读取全局单例、不执行真实外部副作用。
- 现有屏幕只面向手机竖屏；不要假设平板/横屏布局已经支持。适配范围变化需要独立设计和测试。

## 避免

- 不要从页面直接调用 Repository/DataStore，或把 NavController 传进 screen。
- 不要复制已有公共按钮、TopBar、开关卡片和确认弹窗的实现。
- 不要给装饰元素伪造可点击语义，也不要让关键可点击图标缺失描述。
- 不要引入网络图片、运行时下载字体或依赖网络的地图组件；当前演示必须完全离线。
