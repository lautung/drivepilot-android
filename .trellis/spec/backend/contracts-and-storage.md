# API、认证、数据库与对象存储契约

## 场景：增加或修改后端跨层能力

### 1. Scope / Trigger

- 触发：任何 REST 签名、认证行为、数据库 schema、环境变量或 MinIO 集成变化。
- 服务端是登录用户业务状态的权威来源；车控、预约和订阅只属于演示业务，不连接真实车辆或支付系统。

### 2. Signatures

- 公共移动认证：`POST /api/v1/auth/{register,login,refresh,logout}`；只允许 `USER` 使用 JSON refresh token 契约。
- 公共 Web Admin 认证：`POST /api/v1/auth/admin/{login,refresh,logout}`；login/refresh 响应不含 refresh token，token 只通过 HttpOnly Cookie 轮换。
- 公共运维：`GET /actuator/health`、`GET /v3/api-docs`。
- 用户：`GET/PATCH /api/v1/vehicle-state`、`GET/PATCH /api/v1/me/preferences`、发现内容、车辆预订、维保预约和订阅接口。
- 管理：`GET /api/v1/admin/**` 允许 `ADMIN_VIEWER`/`ADMIN`；POST/PUT/PATCH/DELETE 只允许 `ADMIN`，并同时由 URL 规则和 `@PreAuthorize` 防守。
- 数据库主键使用 UUID，时间使用 UTC `Instant`，有限值使用大写枚举字符串；车辆状态 PATCH 必须携带 `version: long`。
- 列表响应固定为 `items/page/size/totalElements/totalPages`。

### 3. Contracts

- 注册/登录请求：`username` 长度 3..64，`password` 长度 8..72；普通注册固定创建 `USER`。
- Android token 响应：`accessToken`、`refreshToken`、`accessExpiresAt`、`user{id,username,role}`。access token 是短期 HMAC JWT；refresh token 是 256-bit opaque 值，数据库只存 SHA-256，轮换读取必须使用悲观写锁。
- Web Admin 响应：`accessToken`、`accessExpiresAt`、`user`；refresh token Cookie 使用 `HttpOnly`、`SameSite=Strict`、Path `/api/v1/auth/admin`，production 必须 `Secure`。Admin/Viewer 不得通过移动 `/auth/login` 获取 JSON refresh token。
- 角色固定为 `USER`、`ADMIN_VIEWER`、`ADMIN`。扩展角色必须新增 Flyway migration 同步 PostgreSQL CHECK，不得只改 Java enum。
- 错误使用 Spring `ProblemDetail`，附加稳定 `code` 和 `fieldErrors[{field,message}]`，不暴露堆栈、SQL、凭证或内部路径。
- 媒体 multipart 字段名为 `file`；只允许 JPEG/PNG/WebP、默认不超过 10 MiB，校验声明 MIME 与文件魔数，使用随机对象键和 SHA-256。
- 必需环境键：`DB_URL/DB_USERNAME/DB_PASSWORD`、`JWT_SECRET`、`ADMIN_USERNAME/ADMIN_PASSWORD`、`VIEWER_USERNAME/VIEWER_PASSWORD`、`ADMIN_COOKIE_SECURE`、`MINIO_ENDPOINT/MINIO_PUBLIC_ENDPOINT/MINIO_ACCESS_KEY/MINIO_SECRET_KEY/MINIO_BUCKET`。TTL/限流周期使用 ISO-8601 duration。
- 内部 MinIO endpoint 用于对象读写；公共 endpoint 只用于生成客户端可访问的短期预签名 URL。
- OpenAPI 唯一源是 Springdoc；规范化快照提交到 `contracts/openapi/openapi.json`。全局 `bearerAuth` 适用于业务 API，移动 auth 端点显式空 security，Admin refresh/logout 使用 `adminRefreshCookie`。
- 单实例请求限流使用 Bucket4j；认证按客户端 IP，上传按认证用户。只有 `TRUSTED_PROXY_ADDRESSES` 中的直连代理才允许提供 `X-Forwarded-For`。

### 4. Validation & Error Matrix

| 条件 | HTTP / code |
| --- | --- |
| 请求字段非法 | `400 VALIDATION_FAILED` |
| 用户名重复 | `409 USERNAME_TAKEN` |
| 登录失败 | `401 INVALID_CREDENTIALS` |
| 普通 USER 尝试 Web Admin 登录 | `403 ADMIN_ACCESS_REQUIRED` |
| refresh 无效、过期或已轮换 | `401 INVALID_REFRESH_TOKEN` |
| 缺少/无效 access token | `401` |
| USER 访问管理接口 | `403` |
| ADMIN_VIEWER 执行管理写操作 | `403` |
| 认证或上传超过限流 | `429 RATE_LIMITED` + `Retry-After` |
| 资源不存在或不属于当前用户 | `404` |
| 车辆 version 过期 | `409 STATE_VERSION_CONFLICT` |
| 媒体被内容引用 | `409 MEDIA_IN_USE` |
| 文件过大 | `413 MEDIA_TOO_LARGE` |
| 魔数不支持 | `415 UNSUPPORTED_MEDIA` |
| 声明 MIME 与魔数不一致 | `415 MEDIA_TYPE_MISMATCH` |
| MinIO 不可用 | `503 MEDIA_STORAGE_UNAVAILABLE` |

### 5. Good / Base / Bad Cases

- Good：同一 refresh token 的并发轮换只有一个成功；Web Admin 轮换 Cookie 且响应体不出现 refresh token；媒体声明 MIME 与实际魔数一致。
- Base：首次注册在同一事务内创建默认车辆状态和个人设置；Viewer 只读 Admin 列表；重复启停订阅保持幂等。
- Bad：Admin 通过移动登录获取 JSON refresh token；前端隐藏按钮但后端允许 Viewer 写；信任任意 `X-Forwarded-For`；公开 bucket；修改已执行 migration。

### 6. Tests Required

- Testcontainers PostgreSQL：从空库运行 Flyway/JPA validate、密码哈希、用户隔离、乐观锁、并发 refresh 只有一次成功。
- Security/HTTP：USER/ADMIN_VIEWER/ADMIN 矩阵、Web Cookie 属性和轮换、`429` ProblemDetail、分页、UUID/Instant/enum。
- OpenAPI：普通测试比较 `contracts/openapi/openapi.json`；只有显式 `-Dphonecar.updateOpenApiSnapshot=true` 才能更新，并必须评审 diff。
- MinIO：真实容器覆盖 bucket 初始化、合法上传、MIME/魔数/大小、预签名、引用删除冲突和 503 映射。
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

Web Admin 错误：

```java
return auth.login(username, password); // Admin refresh token 会进入 JSON
```

Web Admin 正确：

```java
AuthResult result = auth.loginAdmin(username, password);
setRefreshCookie(response, result.refreshToken());
return AdminSessionResponse.from(result); // 响应 DTO 不包含 refreshToken
```

## 常见错误

> 已执行 migration 出现列类型不匹配时新增下一个 migration 修正，不能回写旧 migration。Docker 镜像必须复制 `bootJar` 的固定文件名，避免通配或实际产物名漂移导致空/不可启动镜像。

> 多个 `@SpringBootTest` 类复用同一个 Spring context 时，不能让继承的 `@Container` 在每个子类结束后停止共享容器；使用 JVM 单例容器生命周期，否则后续测试会复用指向已停止 PostgreSQL 的连接池。
