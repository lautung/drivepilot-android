# 导航与事件接口

## 路由定义

- `ui/navigation/AppRoute.kt` 是 16 个页面路由、中文标签和底栏图标的唯一来源。
- 路由字符串使用稳定的小写下划线格式，如 `body_control`、`driving_log`；不要在 `PhoneCarApp` 或页面中散落字符串字面量。
- 四个顶层入口由 `AppRoute.topLevel` 明确列出。只有这些路由显示底部导航栏。
- 当前路由没有参数，页面数据来自共享 `DemoState`。引入导航参数前应先定义类型、编码/恢复行为和测试，不使用临时字符串拼接。

## NavHost 所有权

- `PhoneCarApp` 创建并拥有 NavController、Scaffold、底栏和 NavHost。
- 页面只接收意图回调：打开页面使用 `onCabin`、`onMaintenance` 等，返回统一传入 `navController::popBackStack`。
- 顶层切换保持现有状态恢复策略：`popUpTo(findStartDestination()) { saveState = true }`、`launchSingleTop = true`、`restoreState = true`。
- 详情路由不显示底栏，依据当前 destination route 与 `AppRoute.topLevel` 集合判断。

## 事件接口风格

- 无参数动作：`() -> Unit`，命名为 `onBack`、`onConfirm`、`onClear`。
- 目标值变化：`(Boolean) -> Unit`、`(Int) -> Unit`、`(Enum) -> Unit`，命名为 `onAcEnabled`、`onDay`、`onCamera`。
- 页面把用户选择的目标值上报，不持有 Repository，也不启动协程。ViewModel 方法引用负责接入数据层。
- 回调语义应表达业务意图，不以控件实现命名。例如使用 `onReserve`，而不是 `onButtonClick`。

## 新页面检查清单

1. 在 `AppRoute` 添加稳定 route 和 label；只有顶层入口才提供底栏 icon 并加入 `topLevel`。
2. 在 `PhoneCarApp` 的 NavHost 注册 `composable(AppRoute.X.route)`。
3. 从现有页面通过命名事件回调进入新路由，详情页传入 `onBack`。
4. 若页面改变持久状态，事件必须贯通 ViewModel、对应功能域 Repository、Store 和序列化。
5. 更新 `AllScreensReachableTest`，确保新页面入口可发现、目标内容可见、返回栈正确；顶层入口变化同时更新 `PhoneCarNavigationTest`。

## 避免

- 不要在 screen 内调用 `rememberNavController()` 或硬编码 route。
- 不要通过全局变量、静态单例或 Activity 引用传递页面状态。
- 不要只注册路由而遗漏可发现入口、返回语义或全页面可达测试。
