# DrivePilot Android

[English](README.md) · **简体中文**

这是一个使用 Kotlin 与 Jetpack Compose 构建的高保真、离线运行的智能车控 APP 原型。项目完整复刻 16 个车控页面，内置图片素材与 Solar 风格图标，支持交互状态持久化，运行时不依赖网络。

> 本项目仅用于界面与交互演示，不会连接真实车辆，也不会执行真实支付、导航、蓝牙、相机、定位或远程车控操作。

## 界面截图

<table>
  <tr>
    <th>首页</th>
    <th>发现</th>
    <th>服务</th>
    <th>我的</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/home.png" width="220" alt="DrivePilot 首页" /></td>
    <td><img src="docs/screenshots/discover.png" width="220" alt="DrivePilot 发现页" /></td>
    <td><img src="docs/screenshots/services.png" width="220" alt="DrivePilot 服务页" /></td>
    <td><img src="docs/screenshots/profile.png" width="220" alt="DrivePilot 我的页面" /></td>
  </tr>
</table>

截图来自 MuMu Android 12 模拟器，原始分辨率为 1080 × 1920。

## 主要功能

- **车辆首页：** 车辆解锁、智能座舱、哨兵模式、车体控制、充电地图和数字钥匙。
- **发现：** 汽车生活内容、智能驾驶介绍和官方直播。
- **服务：** 在线选车、维保预约、道路救援和软件订阅。
- **我的：** 个人中心、行车报告、服务入口、账户安全和隐私设置。
- **16 个可达页面：** 所有原型目标页均通过 Navigation Compose 连通。
- **演示状态持久化：** 开关、滑杆、日期、车辆配置、订阅、权限和数字钥匙状态通过 DataStore 保存。
- **完全离线展示：** 图片、矢量图标、地图、图表、温控仪表和演示数据均内置在 APP 中。
- **高保真原型图标：** 使用本地 Solar 矢量路径，不以近似 Material 图标替代。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 开发语言 | Kotlin 2.3.21 |
| UI | Jetpack Compose + Material 3 |
| 页面导航 | Navigation Compose 2.9.8 |
| 状态管理 | ViewModel + 不可变 UI State + Kotlin Flow |
| 本地持久化 | Preferences DataStore 1.2.1 |
| 依赖注入 | 轻量手工 `AppContainer` |
| 自动化测试 | JUnit 4 + Compose UI Test + AndroidX Test |
| Android 版本 | minSdk 24，targetSdk 36 |

## 项目架构

```text
app/src/main/java/com/lautung/phonecar/
├── data/
│   ├── local/          # DataStore 与测试用内存状态
│   └── model/          # 不可变演示模型和配置项
├── ui/
│   ├── components/     # 公共原型组件
│   ├── navigation/     # 16 个应用路由
│   ├── screens/        # 首页、内容、服务和个人中心页面
│   └── theme/          # 颜色、排版和浅色主题
├── AppContainer.kt     # 手工仓储装配
└── MainActivity.kt     # 单 Activity Compose 入口
```

当前仓储使用离线演示实现。后续可以将基于 Flow 的接口替换为真实车辆、地图、内容或服务后端，无需改变页面层契约。

## 环境要求

- Android Studio，并安装 Android SDK 36
- Android 7.0（API 24）或更高版本的手机/模拟器
- 以下命令使用项目自带的 Windows Gradle Wrapper；macOS/Linux 请将 `.\gradlew.bat` 替换为 `./gradlew`

## 构建与安装

```powershell
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

生成的 APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 验证命令

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat assembleDebug
```

设备 UI 测试会遍历全部 16 个原型页面，并验证四个主入口、返回栈、关键控件、确认弹窗和状态更新。

## 项目范围

- 仅支持手机竖屏布局
- 固定浅色主题，哨兵与直播页面保留深色场景
- 使用本地演示数据，不需要生产环境 API 凭证
- 不请求定位、相机、蓝牙、网络、支付或真实车控权限
- 仅提供 Debug APK，不配置正式 Release 签名

原始交互参考保存在 [`doc/智驾车控APP原型_v8.html`](doc/%E6%99%BA%E9%A9%BE%E8%BD%A6%E6%8E%A7APP%E5%8E%9F%E5%9E%8B_v8.html)。
