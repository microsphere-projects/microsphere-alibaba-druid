# microsphere-alibaba-druid-core (`1.x`)

[中文](zh/microsphere-alibaba-druid-core.md)

## Purpose

`microsphere-alibaba-druid-core` is the foundation module of Microsphere Alibaba Druid on the `1.x` line. It contains
everything needed to intercept SQL statement execution on an Alibaba Druid connection pool:

- `AbstractStatementFilter` — a template `com.alibaba.druid.filter.Filter` that wraps every statement execution
  (`Statement` and `PreparedStatement`) with a *before* / *after* callback and resolves a **resource name** (for
  example `SELECT users`) from the executed SQL.
- `LoggingStatementFilter` — a ready-to-use `AbstractStatementFilter` that logs each statement before and after
  execution at `DEBUG` level.
- `PropertyConstants` — the `microsphere.alibaba.druid.*` configuration property-name constants shared by all modules.

This module has no Spring dependency at all, so it also works in plain JDBC applications.

## Maven Coordinates

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-core</artifactId>
</dependency>
```

| Item | Value |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-core` |
| Version | managed by the `microsphere-alibaba-druid-dependencies` BOM (`0.1.21` on the `1.x` line) |
| Packaging | `jar` |

Import the BOM once, then omit versions:

```xml
<properties>
    <microsphere-alibaba-druid.version>0.1.21</microsphere-alibaba-druid.version>
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

With Gradle:

```kotlin
implementation(platform("io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.1.21"))

implementation("io.github.microsphere-projects:microsphere-alibaba-druid-core")
```

Compile dependencies (`com.alibaba:druid`, `microsphere-java-core`, `microsphere-annotation-processor`) are declared
`<optional>true</optional>`, so **you must add `com.alibaba:druid` yourself** — it is not pulled in transitively. The
Druid version (`1.2.28`) is managed by [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md).

## API Reference

### `io.microsphere.alibaba.druid.constants.PropertyConstants`

`public interface PropertyConstants` (since `1.0.0`) — the property-name constants of the whole project.

| Constant | Type | Value / meaning |
|---|---|---|
| `ALIBABA_DRUID_PROPERTY_NAME_PREFIX` | `String` | `"microsphere.alibaba.druid"` — the prefix used by `@ConfigurationProperties` in the Spring Boot module |
| `DEFAULT_ALIBABA_DRUID_ENABLED_PROPERTY_VALUE` | `String` | `"true"` — the default value of the `enabled` property |
| `ALIBABA_DRUID_ENABLED_PROPERTY_NAME` | `String` | `"microsphere.alibaba.druid.enabled"`; annotated with `@ConfigurationProperty(type = boolean.class, defaultValue = "true", source = APPLICATION_SOURCE)` |

```java
// From PropertyConstantsTest (microsphere-alibaba-druid-core test sources)
assertEquals("microsphere.alibaba.druid", ALIBABA_DRUID_PROPERTY_NAME_PREFIX);
assertEquals("microsphere.alibaba.druid.enabled", ALIBABA_DRUID_ENABLED_PROPERTY_NAME);
```

### `io.microsphere.alibaba.druid.filter.AbstractStatementFilter`

`public abstract class AbstractStatementFilter extends com.alibaba.druid.filter.FilterAdapter` (since `1.0.0`).

Two extension scenarios are supported, as described in the class Javadoc:

- **One-time operations** — override `execute(StatementProxy, ThrowableSupplier)`.
- **Two-phase operations** — override `beforeExecute(...)` and `afterExecute(...)`.

Protected fields:

| Field | Type | Description |
|---|---|---|
| `logger` | `io.microsphere.logging.Logger` | Logger named after the concrete filter subclass |
| `dataSource` | `com.alibaba.druid.proxy.jdbc.DataSourceProxy` | Set by `init(DataSourceProxy)` |
| `validationSQL` | `String` | The `DruidDataSource.getValidationQuery()` value, cached at init; may be `null` |

Key methods:

| Signature | Semantics |
|---|---|
| `public final void init(DataSourceProxy dataSource)` | Called by Druid. Stores the `DataSourceProxy`; when it is a `DruidDataSource`, caches its validation query. Logs at `TRACE`. Not overridable. |
| `protected <T> T execute(StatementProxy statement, ThrowableSupplier<T> callback) throws SQLException` | Resolves the resource name, calls `beforeExecute`, runs the callback, then always calls `afterExecute` in a `finally` block. Any `Throwable` from the callback is wrapped into `java.sql.SQLException`. Override for one-time (non two-phase) behavior. |
| `protected abstract void beforeExecute(StatementProxy statement, String resourceName) throws Throwable` | Invoked before the intercepted statement runs. |
| `protected abstract void afterExecute(StatementProxy statement, String resourceName, Object result, Throwable failure)` | Invoked after the intercepted statement runs; `result` and `failure` are `null` when the other one is set. |
| `protected String buildResourceName(StatementProxy statement)` | Parses `statement.getLastExecuteSql()` with `SQLUtils.parseStatements(sql, dataSource.getDbType())` and dispatches to the type-specific overloads. Returns the SQL itself when it equals the cached `validationSQL`, and `"UNRECOGNIZED"` when the statement cannot be recognized. |
| `protected String buildResourceName(SQLStatement sqlStatement)` | Dispatches to `SELECT` / `UPDATE` / `INSERT` / `DELETE` overloads; returns `null` for anything else or when parsing fails. |
| `protected String buildResourceName(SQLSelectStatement selectStatement)` | `"SELECT <table alias>"` |
| `protected String buildResourceName(SQLUpdateStatement updateStatement)` | `"UPDATE <table alias>"` |
| `protected String buildResourceName(SQLInsertStatement insertStatement)` | `"INSERT <table alias>"` |
| `protected String buildResourceName(SQLDeleteStatement deleteStatement)` | `"DELETE <table alias>"` |

Intercepted Druid callbacks — all declared `final`, all funnel into `execute(...)`, so subclasses cannot miss them:

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

`public class LoggingStatementFilter extends AbstractStatementFilter` (since `1.0.0`). Both callbacks log at `DEBUG`
level only when debug is enabled:

```java
// beforeExecute
logger.debug("beforeExecute(statement : {} , resource name : '{}') : {}", statement.getLastExecuteSql(), resourceName);
// afterExecute
logger.debug("afterExecute(statement : {} , resource name : '{}' , result : {} , failure : {})",
        statement.getLastExecuteSql(), resourceName, result, failure);
```

## Usage

### Register a filter with a plain `DruidDataSource`

```java
DruidDataSource dataSource = new DruidDataSource();
dataSource.getProxyFilters().add(new LoggingStatementFilter());
dataSource.init();
```

The same pattern is used by the project's own test base class `AbstractFilterTest`
(`microsphere-alibaba-druid-core` test sources), which plugs any filter into the data source built by
`AbstractAlibabaDruidTest`:

```java
public abstract class AbstractFilterTest<F extends Filter> extends AbstractAlibabaDruidTest {

    @Override
    protected void customize(DruidDataSource dataSource) {
        dataSource.getProxyFilters().add(createFilter());
    }

    protected abstract F createFilter();
}
```

### Write your own statement filter

Adapted from `TestStatementFilter` in the `microsphere-alibaba-druid-spring` test sources:

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

Minimal, framework-free variant (as used by `AbstractStatementFilterTest`):

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

### Resource names you will observe

For the SQL executed by `AbstractAlibabaDruidTest` (H2 by default), `buildResourceName` resolves:

| SQL | Resource name |
|---|---|
| `SELECT id,name FROM users` | `SELECT users` |
| `UPDATE users set name = 'Z' WHERE id = 5` | `UPDATE users` |
| `INSERT INTO users (id, name) VALUES (1, 'Mercy')` | `INSERT users` |
| `DELETE FROM users WHERE id = 1` | `DELETE users` |
| The configured validation query | the validation query string itself |
| Anything unparseable | `UNRECOGNIZED` |

### Spring / Spring Boot users

You rarely add filters by hand. Use [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
(`@EnableAlibabaDruid`) or [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
(auto-configuration + `microsphere.alibaba.druid.filter.classes`).

## Configuration Properties

The core module only *defines* the property names; they are *bound* in the Spring Boot module.

| Property | Type | Default | Defined by | Effect |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true` | `PropertyConstants.ALIBABA_DRUID_ENABLED_PROPERTY_NAME` | Master switch of the Druid auto-configurations (`matchIfMissing = true`) |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter` (spring-boot module) | Which filter bean types get injected into every `DruidDataSource` |

## Version Information

| Item | Value |
|---|---|
| Documentation line | `1.x` (generated from the `release-1.x` branch) |
| Latest released version | `0.1.21` (`0.1.22-SNAPSHOT` is the unpublished branch version) |
| Since (Javadoc) | `1.0.0` for `PropertyConstants`, `AbstractStatementFilter`, `LoggingStatementFilter` |
| Java baseline | `8` — inherited from the external parent chain (`microsphere-spring-cloud-parent:0.1.26` → `microsphere-build:0.3.14`); **not declared** in this repository |
| CI JDK matrix | 8 / 11 / 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`, managed by `microsphere-alibaba-druid-parent` |

## Compatibility Range

| Dimension | Supported on the `1.x` line |
|---|---|
| Alibaba Druid | `1.2.28` (as managed by the parent POM) |
| Java | 8 and newer (bytecode is class-file version 52, so the same jars also load on JDK 11/17/21/25) |
| Spring Boot / Spring Cloud | `2.3.12.RELEASE`/`Hoxton.SR12`, `2.4.13`/`2020.0.5`, `2.6.15`/`2021.0.9` — i.e. Spring Cloud `Hoxton` – `2021.0.x`; these versions are inherited from the external parent POM chain `microsphere-spring-cloud-parent:0.1.26` and are not declared in this repository |
| Framework requirement of this module | none — it needs Druid only, Spring is optional |

## Differences from the `current` line

None at the API level: the sources of this module are identical on both lines — only the version (`0.1.21` vs
`0.2.21`), the JDK 8 baseline, and the Boot 2 compatibility matrix differ.

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)
- [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
- [Project README](../../../README.md)
- [current-line version of this document](../../current/user-guide/microsphere-alibaba-druid-core.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-core.md](../developer-guide/microsphere-alibaba-druid-core.md)

## 中文

- [简体中文：microsphere-alibaba-druid-core](zh/microsphere-alibaba-druid-core.md)
