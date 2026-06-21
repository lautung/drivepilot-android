# 数据库与 JPA

## Schema 所有权

- PostgreSQL schema 只通过 `services/backend/src/main/resources/db/migration/V<N>__<description>.sql` 演进。
- `application.yml` 固定 `spring.jpa.hibernate.ddl-auto=validate`、`open-in-view=false`、JDBC UTC；Hibernate 不创建或更新 schema。
- 已执行 migration 永不回写。类型修正使用下一个版本，例如 `V2__normalize_hash_columns.sql` 修改 `token_hash` 和 `sha256`。

## Entity 约定

- 表名和列名使用 snake_case，并通过 `@Table`、`@Column(name = ...)` 明确映射。
- 主键使用 UUID；当前由 Service/初始化器通过 `UUID.randomUUID()` 创建，不使用数据库自增。
- 时间使用 `Instant` 与 PostgreSQL `TIMESTAMPTZ`；仅维保日期使用 `LocalDate`/`DATE`。
- Entity 提供 JPA 所需的 `protected` 无参构造，不向 Controller 直接暴露。
- 状态变更放在 Entity 方法中并同步更新时间，例如 `VehicleStateEntity.apply(patch, now)`。
- 并发车辆状态使用 `@Version long version`；客户端 PATCH 必须提交已读取的 version。
- 有限状态在 Java 使用 enum，在 migration 中使用 `CHECK` 约束，序列化值保持大写字符串。

## Repository 与事务

- Repository 继承 `JpaRepository<Entity, UUID>`；简单查询使用 Spring Data 派生方法，例如 `findByUsername`、`findByUserIdAndPlan`。
- 并发消费 refresh token 必须通过 `RefreshTokenRepository.findByTokenHash` 的 `PESSIMISTIC_WRITE` 锁读取。
- 写操作使用 `@Transactional`；只读查询使用 `@Transactional(readOnly = true)`。当前事务边界既存在于 Service，也存在于直接承载简单业务的 Controller。
- 用户资源查询必须包含 JWT subject 对应的 userId，不能仅按客户端提供的资源 ID 查询后再返回。
- 分页统一使用 `PageRequest` 和 `PageResponse<T>`，page 从 0 开始，size 范围为 1..100。

## Migration 约定

- 外键明确声明删除行为；用户拥有的数据使用 `ON DELETE CASCADE`，媒体引用等共享关系不隐式级联。
- 常用用户时间序查询建立组合索引，例如 `idx_vehicle_reservations_user(user_id, created_at DESC)`。
- 范围、不为空、唯一性和枚举约束同时落实到数据库，不能只依赖 Bean Validation。
- 修改 schema 后必须从空 PostgreSQL 执行全部 migration，并让 JPA validate 成功。

## 示例

错误：

```java
Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
```

在并发轮换中两个事务可能同时消费同一 token。

正确：

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
```

轮换逻辑保持一个事务：锁定旧 token、撤销、创建新 token 后提交。
