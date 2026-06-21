# 日志

## 当前基线

当前后端源码没有显式业务 Logger、`System.out` 或 `printStackTrace`。运行日志由 Spring Boot、Hibernate、HikariCP、Flyway、容器健康检查和 Docker 提供；应用健康通过 `/actuator/health` 暴露。

因此，不要在普通功能改动中顺手增加日志框架、请求/响应全量打印或异常堆栈输出。新增业务日志必须由明确的可观测性需求驱动，并纳入测试或运维验证。

## 允许记录的内容

- 稳定操作名称、结果类别、HTTP 状态、耗时和不透明资源 UUID。
- 启动/关闭、migration、连接池、bucket 初始化等基础设施状态。
- 外部依赖失败的类别与稳定错误码，但不记录客户端库完整异常对象所携带的敏感请求信息。

## 禁止记录的内容

- 密码、JWT、refresh token、token hash、Authorization header。
- `JWT_SECRET`、数据库密码、MinIO access/secret key、预签名 URL 查询串。
- 上传文件内容、完整请求/响应 body、SQL 参数和用户隐私设置值。
- 面向客户端的异常不能通过 `printStackTrace` 或默认错误页泄露。

## 级别约定

- `ERROR`：请求无法继续且需要人工处理的未知基础设施/程序错误；预期 4xx 业务分支不使用 ERROR。
- `WARN`：可恢复的外部依赖失败、异常但已处理的状态；避免对每次认证失败记录 WARN 造成噪声。
- `INFO`：低频生命周期与管理操作，不记录高频读请求。
- `DEBUG`：本地诊断，默认配置下不得依赖 DEBUG 才能理解生产故障。

## 新增日志时的写法

如果任务明确要求应用日志，使用 SLF4J 参数化日志，不拼接字符串，不记录原始异常消息中的凭据：

```java
private static final Logger log = LoggerFactory.getLogger(MediaService.class);

log.warn("Media delete failed: mediaId={}, code={}", id, "MEDIA_STORAGE_UNAVAILABLE");
```

禁止：

```java
System.out.println("token=" + refreshToken);
exception.printStackTrace();
```

引入首个业务 Logger 时，应同时补充日志捕获测试或明确的容器日志验证，并在本文件记录实际采用的字段格式。
