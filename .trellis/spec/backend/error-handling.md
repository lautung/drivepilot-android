# 错误处理

## HTTP 错误契约

- 预期业务错误抛出 `ApiException(HttpStatus, code, message)`；`code` 是客户端可依赖的稳定大写下划线标识。
- `ApiExceptionHandler` 是唯一全局 HTTP 错误映射入口，响应使用 Spring `ProblemDetail`。
- 每个错误响应包含 `status`、`title`、`detail`、请求路径 `instance`、稳定 `code` 和 `fieldErrors` 数组。
- Bean Validation 失败统一为 `400 VALIDATION_FAILED`，`fieldErrors` 元素固定为 `{field,message}`。
- 数据库唯一性等完整性冲突在边界统一映射为 `409 DATA_CONFLICT`；已知业务冲突应提前抛出更具体的 `ApiException`。
- `application.yml` 禁止默认错误响应包含异常 message 或 stacktrace。

## 抛出位置

- Controller 使用 Bean Validation 处理请求形状，显式检查分页等组合规则。
- Service/领域逻辑抛出业务错误，例如 `USERNAME_TAKEN`、`INVALID_REFRESH_TOKEN`、`MEDIA_IN_USE`。
- Repository 不抛 HTTP 异常；查询为空后由调用方转换为领域错误。
- 外部存储异常转换为稳定上层错误，例如 MinIO 失败转为 `503 MEDIA_STORAGE_UNAVAILABLE`，不向客户端暴露 SDK 异常或内部 endpoint。

## 安全边界

- 错误 detail 不包含密码、JWT、refresh token、哈希、SQL、对象存储凭证、内部文件路径或堆栈。
- 认证失败使用统一 `INVALID_CREDENTIALS`，不能区分“用户不存在”和“密码错误”。
- 用户资源不存在或不属于当前用户均返回 `404`，避免泄露其他用户数据是否存在。
- Controller 不捕获 `Exception` 后返回 `200` 或自定义 Map；新增统一映射应放入 `ApiExceptionHandler`。

## 示例

错误：

```java
return users.findByUsername(username).orElseThrow();
```

这会生成不稳定的框架错误且可能暴露内部行为。

正确：

```java
private ApiException invalidCredentials() {
    return new ApiException(
            HttpStatus.UNAUTHORIZED,
            "INVALID_CREDENTIALS",
            "Username or password is incorrect");
}
```

测试同时断言 HTTP status、`application/problem+json`、`code` 和 `fieldErrors`，不能只断言状态码。
