# 目录与代码组织

## 工程边界

后端是 `services/backend/` 下的独立 Gradle 工程，主包为 `com.lautung.phonecar.backend`。Android 只能通过 `/api/v1` 调用后端，不共享 Java 类型、Entity、migration 或 Gradle catalog。

```text
services/backend/src/main/
├── java/com/lautung/phonecar/backend/
│   ├── auth/       # 登录、JWT、refresh token、Security、管理员初始化
│   ├── common/     # 跨领域 HTTP 错误、分页、OpenAPI 与请求限流
│   ├── content/    # 发现内容、关注、发布管理
│   ├── media/      # MinIO 配置、媒体元数据和管理接口
│   ├── service/    # 预约、维保和订阅演示业务
│   ├── user/       # 用户、个人设置和默认数据初始化
│   └── vehicle/    # 模拟车辆状态和乐观锁更新
└── resources/
    ├── application.yml
    └── db/migration/
```

## 放置规则

- 按业务领域放置 Controller、Service、Entity 和 Repository，不创建全局 `controllers/`、`entities/` 目录。
- `common/` 只保存确实被多个领域使用的类型；现有例子是 `ApiException`、`ApiExceptionHandler`、`PageResponse`、`OpenApiConfig` 和请求限流配置。Web Admin Cookie 会话仍属于 `auth/`，不能因为被前端使用就移到 `common/`。
- HTTP 请求/响应 DTO 优先作为所属 Controller 的 `public record`，例如 `VehicleStateController.VehicleStatePatch` 和 `UserPreferencesController.PreferencesResponse`。
- 领域内部投影可放在 Service 中，例如 `AuthService.AuthResult`、`ContentService.ContentView`；仅实现细节使用 `private record`，例如 `MediaService.DetectedImage`。
- Entity 和 Repository 与所属领域同包。仅供包内 Service/Controller 使用的 Repository 保持 package-private；确实跨领域注入的 Repository 才声明 `public`。
- Spring 配置放在拥有该能力的领域：`SecurityConfig` 在 `auth/`，`MinioConfig` 在 `media/`。

## 命名与依赖方向

- 类型使用职责后缀：`*Controller`、`*Service`、`*Entity`、`*Repository`、`*Config`、`*Properties`。
- Controller 负责 HTTP 映射、Bean Validation、JWT subject 读取和 DTO 转换，不返回 JPA Entity。
- Service 负责事务性业务规则；Repository 只声明持久化查询，不包含 HTTP 概念。
- Entity 保存持久状态与紧邻状态的变换，例如 `VehicleStateEntity.defaults()`、`apply()`。当前 `apply()` 直接接收同包的 `VehicleStateController.VehicleStatePatch`，这是已有耦合而非通用放置规则；新增 Entity 不复制该模式，是否拆出领域命令应由独立重构任务决定。Entity 不依赖 Security 或 MinIO 客户端。

## 示例

错误：

```java
package com.lautung.phonecar.backend.controllers;

@GetMapping("/api/v1/users/{id}")
UserEntity get(@PathVariable UUID id) { ... }
```

正确：

```java
package com.lautung.phonecar.backend.user;

@RequestMapping("/api/v1/me")
public class CurrentUserController {
    public record CurrentUserResponse(UUID id, String username, UserRole role) {}
}
```

当前用户 ID 来自 JWT subject，响应使用 DTO，代码归属 `user` 领域。
