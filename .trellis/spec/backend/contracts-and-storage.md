# API、认证、数据库与对象存储契约

## 场景：增加或修改后端跨层能力

### 1. Scope / Trigger

- 触发：任何 REST 签名、认证行为、数据库 schema、环境变量或 MinIO 集成变化。
- 服务端是登录用户业务状态的权威来源；车控、预约和订阅只属于演示业务，不连接真实车辆或支付系统。

### 2. Signatures

- 公共：`POST /api/v1/auth/{register,login,refresh,logout}`、`GET /actuator/health`、`GET /v3/api-docs`。
- 用户：`GET/PATCH /api/v1/vehicle-state`、`GET/PATCH /api/v1/me/preferences`、发现内容、车辆预订、维保预约和订阅接口。
- 管理：`/api/v1/admin/media` 与 `/api/v1/admin/discovery/contents`，要求 `ADMIN`。
- 数据库主键使用 UUID，时间使用 UTC `Instant`，有限值使用大写枚举字符串；车辆状态 PATCH 必须携带 `version: long`。
- 列表响应固定为 `items/page/size/totalElements/totalPages`。

### 3. Contracts

- 注册/登录请求：`username` 长度 3..64，`password` 长度 8..128；普通注册固定创建 `USER`。
- token 响应：`accessToken`、`refreshToken`、`accessExpiresAt`、`user{id,username,role}`。access token 是短期 HMAC JWT；refresh token 是 256-bit opaque 值，数据库只存 SHA-256，轮换读取必须使用悲观写锁。
- 错误使用 Spring `ProblemDetail`，附加稳定 `code` 和 `fieldErrors[{field,message}]`，不暴露堆栈、SQL、凭证或内部路径。
- 媒体 multipart 字段名为 `file`；只允许 JPEG/PNG/WebP、默认不超过 10 MiB，校验声明 MIME 与文件魔数，使用随机对象键和 SHA-256。
- 必需环境键：`DB_URL/DB_USERNAME/DB_PASSWORD`、`JWT_SECRET`、`ADMIN_USERNAME/ADMIN_PASSWORD`、`MINIO_ENDPOINT/MINIO_PUBLIC_ENDPOINT/MINIO_ACCESS_KEY/MINIO_SECRET_KEY/MINIO_BUCKET`。TTL 使用 ISO-8601 duration。
- 内部 MinIO endpoint 用于对象读写；公共 endpoint 只用于生成客户端可访问的短期预签名 URL。

### 4. Validation & Error Matrix

| 条件 | HTTP / code |
| --- | --- |
| 请求字段非法 | `400 VALIDATION_FAILED` |
| 用户名重复 | `409 USERNAME_TAKEN` |
| 登录失败 | `401 INVALID_CREDENTIALS` |
| refresh 无效、过期或已轮换 | `401 INVALID_REFRESH_TOKEN` |
| 缺少/无效 access token | `401` |
| USER 访问管理接口 | `403` |
| 资源不存在或不属于当前用户 | `404` |
| 车辆 version 过期 | `409 STATE_VERSION_CONFLICT` |
| 媒体被内容引用 | `409 MEDIA_IN_USE` |
| 文件过大或类型非法 | `413` / `415` |
| MinIO 不可用 | `503 MEDIA_STORAGE_UNAVAILABLE` |

### 5. Good / Base / Bad Cases

- Good：同一 refresh token 的并发轮换只有一个成功；媒体上传成功后保存元数据，发现内容只返回已发布记录和预签名 URL。
- Base：首次注册在同一事务内创建默认车辆状态和个人设置；重复启停订阅保持幂等。
- Bad：客户端传入 userId、角色或对象键并被服务端信任；公开 bucket；修改已执行的 `V1` migration；Controller 直接序列化懒加载 Entity。

### 6. Tests Required

- Testcontainers PostgreSQL：Flyway/JPA validate、密码哈希、用户隔离、乐观锁、refresh 轮换及复用拒绝。
- Security/HTTP：USER/ADMIN 权限、ProblemDetail、分页、UUID/Instant/enum、Swagger/OpenAPI。
- MinIO/Compose 冒烟：bucket 初始化、合法上传、私有读取签名、引用删除冲突和 Android 图片加载。
- 修改 migration 时必须从空数据库执行全量 migration；绝不能通过 Hibernate 自动更新掩盖 schema 问题。

### 7. Wrong vs Correct

#### Wrong

```java
refreshTokens.findByTokenHash(hash).orElseThrow(); // 并发请求可能同时消费
```

#### Correct

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
```

轮换方法保持事务边界，先锁定并撤销旧 token，再签发和保存新 token。

## 常见错误

> 已执行 migration 出现列类型不匹配时新增下一个 migration 修正，不能回写旧 migration。Docker 镜像必须复制 `bootJar` 的固定文件名，避免通配或实际产物名漂移导致空/不可启动镜像。
