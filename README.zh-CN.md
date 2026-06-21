# DrivePilot Android

[English](README.md) · **简体中文**

这是一个使用 Kotlin、Jetpack Compose 与 Spring Boot 构建的智能车控 APP 原型。Android 客户端保留 16 个高保真页面，并通过本地后端完成账号、模拟车况、个人设置、预约、订阅和发现内容同步。

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
- **账号与同步：** 支持用户名注册、登录、退出、令牌刷新，以及断网读取最近一次成功缓存。
- **图片内容管理：** 管理员通过 Swagger 上传图片和发布发现内容，图片保存在私有 MinIO 桶并通过短期预签名 URL 读取。
- **本地原型能力：** 矢量图标、地图、图表、未提交表单和页面选择仍保留在设备本地。
- **高保真原型图标：** 使用本地 Solar 矢量路径，不以近似 Material 图标替代。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 开发语言 | Kotlin 2.3.21 |
| UI | Jetpack Compose + Material 3 |
| 页面导航 | Navigation Compose 2.9.8 |
| 状态管理 | ViewModel + 不可变 UI State + Kotlin Flow |
| 本地持久化 | Preferences DataStore 1.2.1 |
| 网络与图片 | Retrofit 3 + OkHttp 5 + Kotlinx Serialization + Coil 3 |
| 依赖注入 | 轻量手工 `AppContainer` |
| 自动化测试 | JUnit 4 + Compose UI Test + AndroidX Test |
| Android 版本 | minSdk 24，targetSdk 36 |
| 后端 | Java 21 + Spring Boot 4.1 + PostgreSQL 16 + Flyway + MinIO |

## 项目架构

```text
app/src/main/java/com/lautung/phonecar/
├── data/
│   ├── auth/           # 会话与 Keystore refresh token
│   ├── local/          # DataStore 与最近成功缓存
│   ├── model/          # 不可变 UI 模型和配置项
│   ├── remote/         # Retrofit DTO、API 与令牌刷新
│   └── repository/     # 本地 UI 状态与远端业务仓储
├── ui/
│   ├── components/     # 公共原型组件
│   ├── navigation/     # 16 个应用路由
│   ├── screens/        # 首页、内容、服务和个人中心页面
│   └── theme/          # 颜色、排版和浅色主题
├── AppContainer.kt     # 手工仓储装配
└── MainActivity.kt     # 单 Activity Compose 入口

backend/src/main/
├── java/.../backend/   # auth、vehicle、user、service、content、media
└── resources/db/migration/ # 只向前演进的 Flyway migration
```

服务端业务状态是登录用户的权威来源；DataStore 保存最近成功状态及设备本地 UI 状态。所有写操作要求联网，失败时保留最近一次服务端确认状态。车控和订阅均为模拟业务，不连接真实车辆或支付系统。

## 环境要求

- Android Studio，并安装 Android SDK 36
- Android 7.0（API 24）或更高版本的手机/模拟器
- Java 21、Docker Desktop 与 Docker Compose
- 以下命令使用项目自带的 Windows Gradle Wrapper；macOS/Linux 请将 `.\gradlew.bat` 替换为 `./gradlew`

## 启动本地后端

复制 `.env.example` 为 `.env`，替换数据库密码、JWT secret、管理员密码和 MinIO secret，然后启动：

```powershell
docker compose up --build -d
docker compose ps
```

- 健康检查：`http://localhost:8080/actuator/health`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI：`http://localhost:8080/v3/api-docs`
- MinIO Console：`http://localhost:9001`

模拟器默认访问 `http://10.0.2.2:8080/api/v1/`。真机或其他网络环境可在构建时覆盖：

```powershell
.\gradlew.bat :app:assembleDebug -PPHONECAR_API_BASE_URL=http://192.168.1.10:8080/api/v1/
```

管理员由 `ADMIN_USERNAME`、`ADMIN_PASSWORD` 初始化。普通注册只能创建 `USER`，MinIO 凭证不会下发到 Android。公网部署前必须使用 HTTPS、强随机 secret 和反向代理。

## 构建与安装

```powershell
.\gradlew.bat :app:assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

生成的 APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 验证命令

```powershell
.\gradlew.bat :backend:test :backend:bootJar
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
.\gradlew.bat :app:connectedDebugAndroidTest
docker compose config --quiet
```

设备 UI 测试会遍历全部 16 个原型页面，并验证四个主入口、返回栈、关键控件、确认弹窗和状态更新。

## 项目范围

- 仅支持手机竖屏布局
- 固定浅色主题，哨兵与直播页面保留深色场景
- 后端只维护模拟车况和演示业务记录
- Android 仅新增网络权限，不请求定位、相机、蓝牙、支付或真实车控权限
- 仅提供 Debug APK，不配置正式 Release 签名

原始交互参考保存在 [`doc/智驾车控APP原型_v8.html`](doc/%E6%99%BA%E9%A9%BE%E8%BD%A6%E6%8E%A7APP%E5%8E%9F%E5%9E%8B_v8.html)。
