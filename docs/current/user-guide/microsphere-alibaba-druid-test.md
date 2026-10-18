# microsphere-alibaba-druid-test

[中文](zh/microsphere-alibaba-druid-test.md)

## Purpose

`microsphere-alibaba-druid-test` is the plain (non-Spring) testing toolkit of the project. It gives you a working
`DruidDataSource` backed by H2 and drives SQL through it, so you can verify your `Filter` implementations:

- `AbstractAlibabaDruidTest` — a JUnit 5 base class that builds/initializes a `DruidDataSource`, creates a `users`
  table, executes `Statement` and `PreparedStatement` traffic, then drops the table and closes the pool.
- `AlibabaDruidTestUtils` — static helpers that load `META-INF/druid/h2.properties` and build a `DruidDataSource` from
  `Properties`.
- `@AlibabaDruidTest` + `@AlibabaDruidRuntime` + `AlibabaDruidTestExtension` — a JUnit Jupiter extension that builds an
  initialized `DruidDataSource` and injects it into test fields (static and instance) or method parameters.

Despite its name, this module **does not depend on `microsphere-alibaba-druid-core`** — it needs Druid and JUnit 5 only,
which is why `core` itself uses it in `test` scope.

## Maven Coordinates

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-test</artifactId>
    <scope>test</scope>
</dependency>
```

| Item | Value |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-test` |
| Version | managed by the `microsphere-alibaba-druid-dependencies` BOM (`0.2.21` on the `current` line) |
| Packaging | `jar` |
| Compile dependencies | none from this repository — everything below is optional |
| Optional dependencies | `microsphere-annotation-processor`, `org.junit.jupiter:junit-jupiter`, `microsphere-java-test`, `microsphere-logging-test`, `com.alibaba:druid`, `com.h2database:h2`, `ch.qos.logback:logback-classic` |

Import the BOM once, then omit versions:

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

All dependencies — including `junit-jupiter`, `druid` and `h2` — are `<optional>true</optional>`, so a test that uses
this module must also declare Druid, H2 and the JUnit 5 engine itself.

## API Reference

### `io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils`

`public abstract class AlibabaDruidTestUtils` (since `1.0.0`) — private constructor, static methods only.

| Member | Signature | Semantics |
|---|---|---|
| `H2_PROPERTIES_RESOURCE` | `public static final String = "META-INF/druid/h2.properties"` | The shipped H2 data-source properties resource |
| `DEFAULT_PROPERTIES_RESOURCE` | `public static final String = H2_PROPERTIES_RESOURCE` | The default resource used when nothing is specified |
| `PACKAGE_NAME_PREFIX` | `public static final String = "com.alibaba.druid."` | Package prefix used to recognize Druid types |
| `getDefaultProperties` | `public static @Nonnull Properties getDefaultProperties() throws IOException` | Loads `DEFAULT_PROPERTIES_RESOURCE` |
| `loadProperties` | `public static @Nonnull Properties loadProperties(String resourceName) throws IOException` | Loads any properties resource from the class path |
| `loadProperties` | `public static @Nonnull Properties loadProperties(String[] keyValuePairs) throws IOException` | Builds `Properties` from inline `"key = value"` entries joined by the line separator |
| `properties` | `static @Nonnull Properties properties(String content) throws IOException` | Package-private parsing helper |
| `buildDefaultDruidDataSource` | `public static @Nonnull DruidDataSource buildDefaultDruidDataSource() throws IOException` | `buildDruidDataSource(getDefaultProperties())` |
| `buildDruidDataSource` | `public static @Nonnull DruidDataSource buildDruidDataSource(Properties properties)` | `new DruidDataSource()` + `configFromProperties(properties)`; **not** initialized — call `init()` yourself |

The bundled `META-INF/druid/h2.properties` (main resources):

```properties
druid.name=h2
druid.url=jdbc:h2:mem:test_mem
druid.username=sa
druid.password=
druid.driverClassName=org.h2.Driver
```

Asserted by `AlibabaDruidTestUtilsTest` (test sources):

```java
Properties properties = getDefaultProperties();
assertEquals("h2", properties.getProperty("druid.name"));
assertEquals("jdbc:h2:mem:test_mem", properties.getProperty("druid.url"));
assertEquals("sa", properties.getProperty("druid.username"));
assertEquals("", properties.getProperty("druid.password"));
assertEquals("org.h2.Driver", properties.getProperty("druid.driverClassName"));
```

### `io.microsphere.alibaba.druid.test.AbstractAlibabaDruidTest`

`public abstract class AbstractAlibabaDruidTest` (since `1.0.0`).

Lifecycle:

| Phase | Method | What happens |
|---|---|---|
| `@BeforeEach` | `public void init() throws Throwable` | `buildDruidDataSource()` → `customize(dataSource)` → `dataSource.init()` → `initData()` (`CREATE TABLE users (id INT, name VARCHAR(50))`) |
| `@Test` | `public void test() throws Throwable` | Runs `testExecuteStatement()` then `testExecutePreparedStatement()` |
| `@AfterEach` | `public void destroy() throws Throwable` | `destroyData()` (`DROP TABLE users`) then `dataSource.close()` |

Extension points:

| Method | Default | Override to |
|---|---|---|
| `protected DruidDataSource buildDruidDataSource() throws IOException` | `AlibabaDruidTestUtils.buildDefaultDruidDataSource()` | supply your own data source (e.g. a Spring-managed one — see `AbstractDruidSpringTest`) |
| `protected void customize(DruidDataSource dataSource)` | empty | register filters: `dataSource.getProxyFilters().add(new LoggingStatementFilter())` |
| `protected DruidDataSource getDruidDataSource()` | returns the current instance | change how the pool is looked up |
| `protected void testExecuteStatement() throws Throwable` | INSERT ×4 (all `executeUpdate` overloads), `addBatch`/`executeBatch`, `executeQuery`, UPDATE, DELETE ×4 + `DELETE FROM users`, asserting affected row counts | replace the SQL workload |
| `protected void testExecutePreparedStatement() throws Throwable` | `executeUpdate` on `INSERT INTO users (id, name) VALUES (?, ?)`, then `prepareStatement(sql, TYPE_FORWARD_ONLY)` and `prepareStatement(sql, TYPE_FORWARD_ONLY, CONCUR_UPDATABLE)` selects | replace the prepared-statement workload |
| `protected void executeStatement(ThrowableConsumer<Statement> consumer) throws Throwable` | borrows a `Statement`, always closes it | run your own SQL through the filtered pool |
| `protected void executePreparedStatement(String sql, ThrowableConsumer<PreparedStatement> consumer) throws Throwable` | borrows a `PreparedStatement` for `sql`, always closes it | run your own parameterized SQL |
| `protected void executeConnection(ThrowableConsumer<Connection> consumer) throws Throwable` | borrows a `Connection` from `getDruidDataSource()`, always closes it | anything connection-level |

`executeStatement`, `executePreparedStatement` and `executeConnection` accept `io.microsphere.lang.function.ThrowableConsumer`,
so your lambdas may throw checked exceptions without wrappers.

### `io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidTest`

`@Target(TYPE) @Retention(RUNTIME) @ExtendWith(AlibabaDruidTestExtension.class) @Inherited public @interface AlibabaDruidTest`
(since `1.0.0`).

| Attribute | Type | Default | Meaning |
|---|---|---|---|
| `properties` | `String[]` | `{}` | Inline `druid.*=value` entries that **override** the ones from `propertiesResource()` |
| `propertiesResource` | `String` | `AlibabaDruidTestUtils.DEFAULT_PROPERTIES_RESOURCE` (`META-INF/druid/h2.properties`) | Class-path resource fed to `DruidDataSource.configFromProperties(Properties)` |
| `filters` | `String[]` | `{}` | Declared filter names (intended for `DruidDataSource.setFilters(String)`, e.g. `{"log", "stat", "wall"}`). In `0.2.21` the extension does **not** apply it — configure filters via the properties resource (`druid.filters=...`) or the filter's own `@AutoLoad` mechanism |

```java
@AlibabaDruidTest(
    properties = {
        "druid.name=mysql",
        "druid.url=jdbc:mysql://localhost:3306/test",
        "druid.username=root",
        "druid.password=123456",
        "druid.driverClassName=com.mysql.jdbc.Driver"
    }
)
class MyTest {
}
```

*(Snippet taken from the `AlibabaDruidTest` Javadoc.)*

### `io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidRuntime`

`@Target({FIELD, PARAMETER}) @Retention(RUNTIME) public @interface AlibabaDruidRuntime` (since `1.0.0`) — injects the
Druid runtime into:

| Component | Static field | Instance field | Method parameter |
|---|---|---|---|
| `javax.sql.DataSource` | Y | Y | Y |
| `javax.sql.ConnectionPoolDataSource` | Y | Y | Y |
| `com.alibaba.druid.proxy.jdbc.DataSourceProxy` | Y | Y | Y |
| `com.alibaba.druid.pool.DruidDataSource` | Y | Y | Y |

Any type that is `DataSource`-assignable or whose name starts with `com.alibaba.druid.` is a candidate
(`AlibabaDruidTestExtension.isAlibabaDruidClass`).

### `io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidTestExtension`

`class AlibabaDruidTestExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor,
AfterEachCallback, ParameterResolver` — **package-private** (since `1.0.0`), activated by `@AlibabaDruidTest`.

| Behaviour | Detail |
|---|---|
| Static field injection | `beforeAll` → `injectFields(context, null)` fills `@AlibabaDruidRuntime` static fields |
| Instance field injection | `postProcessTestInstance` fills non-static fields |
| Parameter injection | `supportsParameter` / `resolveParameter` for `@AlibabaDruidRuntime` parameters |
| Build | `buildDruidDataSource(AlibabaDruidTest)` loads `propertiesResource()`, overlays `properties()` (`putAll`), calls `configFromProperties(...)` and `init()` |
| Storage | class-level store (`beforeAll`/`afterAll`) vs method-level store (per test), keyed `testClass-or-method-signature:componentClassName` |
| Teardown | `afterEach` closes the method-scoped pool; `afterAll` closes the class-scoped one; `close(...)` is null- and already-closed-safe |
| Annotation lookup | `getAlibabaDruidTest(Class)` falls back to the enclosing class, which makes `@Nested` test classes work when the outer class is annotated |

## Usage

### Subclass and plug a filter in

`core`'s own base class is the pattern (from `microsphere-alibaba-druid-core` test sources):

```java
public abstract class AbstractFilterTest<F extends Filter> extends AbstractAlibabaDruidTest {

    @Override
    protected void customize(DruidDataSource dataSource) {
        dataSource.getProxyFilters().add(createFilter());
    }

    protected abstract F createFilter();
}
```

```java
@LoggingLevelsClass(levels = {"TRACE", "INFO", "ERROR"})
public class LoggingStatementFilterTest extends AbstractFilterTest<LoggingStatementFilter> {

    @Override
    protected LoggingStatementFilter createFilter() {
        return new LoggingStatementFilter();
    }
}
```

The simplest possible subclass, `DefaultAlibabaDruidTest` (test sources of this module), needs no code at all:

```java
public class DefaultAlibabaDruidTest extends AbstractAlibabaDruidTest {
}
```

### Use the JUnit 5 annotations

From `DruidDataSourcePoolMetadataTest` in `microsphere-alibaba-druid-spring-boot`:

```java
@AlibabaDruidTest
class DruidDataSourcePoolMetadataTest {

    @AlibabaDruidRuntime
    private DruidDataSource druidDataSource;

    private DruidDataSourcePoolMetadata metadata;

    @BeforeEach
    void setUp() {
        this.metadata = new DruidDataSourcePoolMetadata(this.druidDataSource);
    }
}
```

Field, static-field and parameter injection together, as exercised by `AlibabaDruidTestExtensionTest` (test sources):

```java
@AlibabaDruidTest
class AlibabaDruidTestExtensionTest {

    @Nested
    @DisplayName("Test : Field Injection")
    class FieldTest extends AbstractAlibabaDruidTest {

        @AlibabaDruidRuntime
        private DruidDataSource druidDataSource;

        @AlibabaDruidRuntime
        private DataSource dataSource;

        @Override
        protected DruidDataSource buildDruidDataSource() throws IOException {
            return this.druidDataSource;
        }
    }

    @Nested
    @DisplayName("Test : Static Field Injection")
    class StaticFieldTest {

        @AlibabaDruidRuntime
        private static DruidDataSource druidDataSource;

        @AlibabaDruidRuntime
        private static DataSource dataSource;

        @Test
        void test() {
            assertObjects(druidDataSource, dataSource);

            close(druidDataSource);
            close(druidDataSource);
        }
    }

    @Nested
    @DisplayName("Test : Parameter Injection")
    class ParameterTest {

        @Test
        void test(@AlibabaDruidRuntime DruidDataSource druidDataSource, @AlibabaDruidRuntime DataSource dataSource) {
            assertObjects(druidDataSource, dataSource);
        }
    }
}
```

Note how `FieldTest` reuses the injected pool by overriding `buildDruidDataSource()` — otherwise
`AbstractAlibabaDruidTest` would create a second one.

### Build a data source yourself

```java
DruidDataSource dataSource = AlibabaDruidTestUtils.buildDefaultDruidDataSource();
dataSource.getProxyFilters().add(new LoggingStatementFilter());
dataSource.init();
try {
    // exercise SQL
} finally {
    dataSource.close();
}
```

## Configuration Properties

This module reads no `microsphere.alibaba.druid.*` property; it is configured through annotations and a properties
resource:

| Knob | Where | Default | Effect |
|---|---|---|---|
| `propertiesResource` | `@AlibabaDruidTest` | `META-INF/druid/h2.properties` | Druid `druid.*` keys fed to `configFromProperties` |
| `properties` | `@AlibabaDruidTest` | `{}` | Inline overrides applied on top of the resource |
| `filters` | `@AlibabaDruidTest` | `{}` | Declared but not applied by the extension in `0.2.21` |
| `customize(DruidDataSource)` | `AbstractAlibabaDruidTest` | empty | Add filters or tweak the pool before `init()` |

## Version Information

| Item | Value |
|---|---|
| Documentation line | `current` (release branch) |
| Latest released version | `0.2.21` (`0.2.22-SNAPSHOT` is the unpublished development version on `main`) |
| Since (Javadoc) | `1.0.0` for `AbstractAlibabaDruidTest`, `AlibabaDruidTestUtils`, `@AlibabaDruidTest`, `@AlibabaDruidRuntime`, `AlibabaDruidTestExtension` |
| Java | 17+ (`<java.version>17</java.version>` in the root `pom.xml`) |
| CI JDK matrix | 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`, managed by `microsphere-alibaba-druid-parent` |

## Compatibility Range

| Dimension | Supported on the `current` line |
|---|---|
| Alibaba Druid | `1.2.28` (as managed by the parent POM) |
| Java | 17, 21, 25 (JDK 17+ required) |
| JUnit | JUnit Jupiter 5 (the extension API used is `BeforeAllCallback` / `AfterAllCallback` / `TestInstancePostProcessor` / `AfterEachCallback` / `ParameterResolver`) |
| Database | H2 in-memory (`jdbc:h2:mem:test_mem`) out of the box; any driver usable through `properties` / `propertiesResource` |
| Framework requirement | none — Spring is not needed; for Spring-based tests use `microsphere-alibaba-druid-spring-test` |

Spring Cloud `Hoxton` – `2021.0.x` / JDK 8 users: see the [1.x User Guide](../../1.x/user-guide/README.md).

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
- [Project README](../../../README.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-test.md](../developer-guide/microsphere-alibaba-druid-test.md)

## 中文

- [简体中文：microsphere-alibaba-druid-test](zh/microsphere-alibaba-druid-test.md)
