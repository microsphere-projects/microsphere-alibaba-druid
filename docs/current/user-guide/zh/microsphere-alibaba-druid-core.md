# microsphere-alibaba-druid-core

[English](../microsphere-alibaba-druid-core.md)

## 模块用途

`microsphere-alibaba-druid-core` 是 Microsphere Alibaba Druid 的基础模块，包含在 Alibaba Druid 连接池上拦截 SQL
语句执行所需的全部能力：

- `AbstractStatementFilter` —— 一个模板化的 `com.alibaba.druid.filter.Filter`，为每一次语句执行（`Statement` 与
  `PreparedStatement`）包装 *前置* / *后置* 回调，并能从执行的 SQL 中解析出**资源名**（例如 `SELECT users`）。
- `LoggingStatementFilter` —— 开箱即用的 `AbstractStatementFilter`，在 `DEBUG` 级别记录每条语句执行前后的信息。
- `PropertyConstants` —— 全项目共享的 `microsphere.alibaba.druid.*` 配置属性名常量。

本模块不依赖 Spring，因此在纯 JDBC 应用中同样可用。

## Maven 坐标

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-core</artifactId>
</dependency>
```

| 项目 | 值 |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-core` |
| 版本 | 由 `microsphere-alibaba-druid-dependencies` BOM 管理（`current` 文档线为 `0.2.21`） |
| 打包方式 | `jar` |

导入一次 BOM，之后无需再写版本号：

```xml
<properties>
    <microsphere-alibaba-druid.version>0.2.21</microsphere-alibaba-druid.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-alibaba-druid-dependencies</artifactId>
            <version>${microsphere-alibaba-druid.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

编译期依赖（`com.alibaba:druid`、`microsphere-java-core`、`microsphere-annotation-processor`）都声明为
`<optional>true</optional>`，因此**必须由你自己添加 `com.alibaba:druid`** —— 它不会被传递引入。Druid 的版本
（`1.2.28`）由 [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md) 管理。

## API 说明

### `io.microsphere.alibaba.druid.constants.PropertyConstants`

`public interface PropertyConstants`（自 `1.0.0` 起）—— 整个项目的属性名常量。

| 常量 | 类型 | 值 / 含义 |
|---|---|---|
| `ALIBABA_DRUID_PROPERTY_NAME_PREFIX` | `String` | `"microsphere.alibaba.druid"` —— Spring Boot 模块中 `@ConfigurationProperties` 使用的前缀 |
| `DEFAULT_ALIBABA_DRUID_ENABLED_PROPERTY_VALUE` | `String` | `"true"` —— `enabled` 属性的默认值 |
| `ALIBABA_DRUID_ENABLED_PROPERTY_NAME` | `String` | `"microsphere.alibaba.druid.enabled"`；带注解 `@ConfigurationProperty(type = boolean.class, defaultValue = "true", source = APPLICATION_SOURCE)` |

```java
// From PropertyConstantsTest (microsphere-alibaba-druid-core test sources)
assertEquals("microsphere.alibaba.druid", ALIBABA_DRUID_PROPERTY_NAME_PREFIX);
assertEquals("microsphere.alibaba.druid.enabled", ALIBABA_DRUID_ENABLED_PROPERTY_NAME);
```

### `io.microsphere.alibaba.druid.filter.AbstractStatementFilter`

`public abstract class AbstractStatementFilter extends com.alibaba.druid.filter.FilterAdapter`（自 `1.0.0` 起）。

正如该类 Javadoc 所述，支持两种扩展场景：

- **一次性操作** —— 覆写 `execute(StatementProxy, ThrowableSupplier)`。
- **两阶段操作** —— 覆写 `beforeExecute(...)` 与 `afterExecute(...)`。

受保护字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `logger` | `io.microsphere.logging.Logger` | 以具体 filter 子类命名的 Logger |
| `dataSource` | `com.alibaba.druid.proxy.jdbc.DataSourceProxy` | 由 `init(DataSourceProxy)` 赋值 |
| `validationSQL` | `String` | 初始化时缓存的 `DruidDataSource.getValidationQuery()`，可能为 `null` |

关键方法：

| 方法签名 | 语义 |
|---|---|
| `public final void init(DataSourceProxy dataSource)` | 由 Druid 调用。保存 `DataSourceProxy`；若其是 `DruidDataSource`，则缓存其校验 SQL，并在 `TRACE` 级别打日志。不可覆写。 |
| `protected <T> T execute(StatementProxy statement, ThrowableSupplier<T> callback) throws SQLException` | 先解析资源名，调用 `beforeExecute`，执行 callback，随后在 `finally` 块中必定调用 `afterExecute`。callback 抛出的任何 `Throwable` 都会被包装为 `java.sql.SQLException`。一次性操作场景可覆写本方法。 |
| `protected abstract void beforeExecute(StatementProxy statement, String resourceName) throws Throwable` | 被拦截语句执行前调用。 |
| `protected abstract void afterExecute(StatementProxy statement, String resourceName, Object result, Throwable failure)` | 被拦截语句执行后调用；`result` 与 `failure` 二者只有一个非空。 |
| `protected String buildResourceName(StatementProxy statement)` | 用 `SQLUtils.parseStatements(sql, dataSource.getDbType())` 解析 `statement.getLastExecuteSql()`，再分派到按类型划分的重载方法。若 SQL 等于缓存的 `validationSQL` 则直接返回该 SQL；无法识别时返回 `"UNRECOGNIZED"`。 |
| `protected String buildResourceName(SQLStatement sqlStatement)` | 分派到 `SELECT` / `UPDATE` / `INSERT` / `DELETE` 的重载；其他类型或解析失败返回 `null`。 |
| `protected String buildResourceName(SQLSelectStatement selectStatement)` | `"SELECT <表别名>"` |
| `protected String buildResourceName(SQLUpdateStatement updateStatement)` | `"UPDATE <表别名>"` |
| `protected String buildResourceName(SQLInsertStatement insertStatement)` | `"INSERT <表别名>"` |
| `protected String buildResourceName(SQLDeleteStatement deleteStatement)` | `"DELETE <表别名>"` |

被拦截的 Druid 回调 —— 全部声明为 `final`，全部汇入 `execute(...)`，因此子类不可能漏掉：

```java
public final boolean preparedStatement_execute(FilterChain chain, PreparedStatementProxy statement) throws SQLException
public final ResultSetProxy preparedStatement_executeQuery(FilterChain chain, PreparedStatementProxy statement) throws SQLException
public final int preparedStatement_executeUpdate(FilterChain chain, PreparedStatementProxy statement) throws SQLException
public final ResultSetProxy statement_executeQuery(FilterChain chain, StatementProxy statement, String sql) throws SQLException
public final boolean statement_execute(FilterChain chain, StatementProxy statement, String sql) throws SQLException
public final boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, int autoGeneratedKeys) throws SQLException
public final boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, int[] columnIndexes) throws SQLException
public final boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, String[] columnNames) throws SQLException
public final int[] statement_executeBatch(FilterChain chain, StatementProxy statement) throws SQLException
public final int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql) throws SQLException
public final int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, int autoGeneratedKeys) throws SQLException
public final int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, int[] columnIndexes) throws SQLException
public final int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, String[] columnNames) throws SQLException
```

### `io.microsphere.alibaba.druid.filter.LoggingStatementFilter`

`public class LoggingStatementFilter extends AbstractStatementFilter`（自 `1.0.0` 起）。两个回调仅在 DEBUG 开启时输出：

```java
// beforeExecute
logger.debug("beforeExecute(statement : {} , resource name : '{}') : {}", statement.getLastExecuteSql(), resourceName);
// afterExecute
logger.debug("afterExecute(statement : {} , resource name : '{}' , result : {} , failure : {})",
        statement.getLastExecuteSql(), resourceName, result, failure);
```

## 使用示例

### 在普通 `DruidDataSource` 上注册 filter

```java
DruidDataSource dataSource = new DruidDataSource();
dataSource.getProxyFilters().add(new LoggingStatementFilter());
dataSource.init();
```

项目自身的测试基类 `AbstractFilterTest`（`microsphere-alibaba-druid-core` 测试源码）采用的正是同一种写法，把任意
filter 挂到 `AbstractAlibabaDruidTest` 构建的数据源上：

```java
public abstract class AbstractFilterTest<F extends Filter> extends AbstractAlibabaDruidTest {

    @Override
    protected void customize(DruidDataSource dataSource) {
        dataSource.getProxyFilters().add(createFilter());
    }

    protected abstract F createFilter();
}
```

### 编写自己的语句 filter

改写自 `microsphere-alibaba-druid-spring` 测试源码中的 `TestStatementFilter`：

```java
public class TestStatementFilter extends AbstractStatementFilter {

    @Override
    protected void beforeExecute(StatementProxy statement, String resourceName) throws Throwable {
        assertNotNull(statement);
    }

    @Override
    protected void afterExecute(StatementProxy statement, String resourceName, Object result, Throwable failure) {
        assertNotNull(statement);
    }
}
```

不依赖任何框架的最小实现（来自 `AbstractStatementFilterTest`）：

```java
AbstractStatementFilter filter = new AbstractStatementFilter() {
    @Override
    protected void beforeExecute(StatementProxy statement, String resourceName) throws Throwable {
    }

    @Override
    protected void afterExecute(StatementProxy statement, String resourceName, Object result, Throwable failure) {
    }
};
```

### 你会观察到的资源名

对 `AbstractAlibabaDruidTest`（默认使用 H2）执行的 SQL，`buildResourceName` 解析结果为：

| SQL | 资源名 |
|---|---|
| `SELECT id,name FROM users` | `SELECT users` |
| `UPDATE users set name = 'Z' WHERE id = 5` | `UPDATE users` |
| `INSERT INTO users (id, name) VALUES (1, 'Mercy')` | `INSERT users` |
| `DELETE FROM users WHERE id = 1` | `DELETE users` |
| 已配置的校验 SQL | 校验 SQL 本身 |
| 无法解析的语句 | `UNRECOGNIZED` |

### Spring / Spring Boot 用户

通常不需要手工添加 filter。请使用 [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
（`@EnableAlibabaDruid`）或 [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
（自动配置 + `microsphere.alibaba.druid.filter.classes`）。

## 配置属性

core 模块只*定义*属性名，真正的*绑定*发生在 Spring Boot 模块。

| 属性 | 类型 | 默认值 | 定义位置 | 作用 |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true` | `PropertyConstants.ALIBABA_DRUID_ENABLED_PROPERTY_NAME` | Druid 自动配置的总开关（`matchIfMissing = true`） |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter`（spring-boot 模块） | 决定哪些 filter Bean 类型会被注入每个 `DruidDataSource` |

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `current`（release 分支） |
| 最新已发布版本 | `0.2.21`（`main` 上的 `0.2.22-SNAPSHOT` 是未发布的开发版） |
| Javadoc `@since` | `PropertyConstants`、`AbstractStatementFilter`、`LoggingStatementFilter` 均为 `1.0.0` |
| Java | 17+（根 `pom.xml` 中 `<java.version>17</java.version>`） |
| CI JDK 矩阵 | 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`，由 `microsphere-alibaba-druid-parent` 管理 |

## 兼容范围

| 维度 | `current` 文档线支持情况 |
|---|---|
| Alibaba Druid | `1.2.28`（由父 POM 管理） |
| Java | 17、21、25（要求 JDK 17+） |
| Spring Boot / Spring Cloud | `3.0.13`/`2022.0.5`、`3.3.13`/`2023.0.6`、`3.4.13`/`2024.0.3`、`3.5.15`/`2025.0.3`，即 Spring Cloud `2022.0.x` – `2025.0.x`；这些版本**继承自外部父 POM 链** `microsphere-spring-cloud-parent:0.2.26`，并非本仓库声明 |
| 本模块的框架要求 | 无 —— 只需 Druid，Spring 为可选 |

仍在使用 Spring Cloud `Hoxton` – `2021.0.x` / JDK 8？请查看 [1.x 用户文档](../../../1.x/user-guide/README.md)。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)
- [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
- [项目 README](../../../../README.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-core.md)

## English

- [English version of this document](../microsphere-alibaba-druid-core.md)
