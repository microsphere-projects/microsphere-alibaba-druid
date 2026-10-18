# microsphere-alibaba-druid-test (`1.x`)

[中文](zh/microsphere-alibaba-druid-test.md)

## Purpose

`microsphere-alibaba-druid-test` is the Spring-free test-support module of the `1.x` line. It gives a repeatable,
framework-independent way to run real SQL against a real `DruidDataSource` while your `Filter` implementations are
attached:

- `AbstractAlibabaDruidTest` — a JUnit 5 test template: builds an H2-backed `DruidDataSource`, creates a `users`
  table, executes a fixed matrix of `Statement` / `PreparedStatement` calls, then drops the table and closes the pool.
- `AlibabaDruidTestUtils` — non-instantiable helpers that load `META-INF/druid/h2.properties` and turn `Properties`
  into a configured `DruidDataSource`.
- `@AlibabaDruidTest` + `@AlibabaDruidRuntime` + `AlibabaDruidTestExtension` — declarative JUnit 5 support that injects
  a live `DruidDataSource` into fields (instance or static) and test-method parameters.
- `META-INF/druid/h2.properties` — the default data-source descriptor used by all of the above.

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
| Version | managed by the `microsphere-alibaba-druid-dependencies` BOM (`0.1.21` on the `1.x` line) |
| Packaging | `jar` (main sources, not test fixtures) |
| Compile dependencies | none — everything is optional |
| Optional dependencies | `microsphere-annotation-processor`, `junit-jupiter`, `microsphere-java-test`, `microsphere-logging-test`, `com.alibaba:druid`, `com.h2database:h2`, `logback-classic` |

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
testImplementation(platform("io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.1.21"))

testImplementation("io.github.microsphere-projects:microsphere-alibaba-druid-test")
```

> Because JUnit 5, Druid and H2 are all `optional`, you must add them yourself (`junit-jupiter`, `com.alibaba:druid`,
> `com.h2database:h2`). This module has **no** compile dependency on `microsphere-alibaba-druid-core`, which is why
> `core` can safely depend on it with `test` scope.

## API Reference

### `io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils`

`public abstract class AlibabaDruidTestUtils` with a `private` constructor (since `1.0.0`) — not instantiable.

| Constant / method | Signature | Notes |
|---|---|---|
| `H2_PROPERTIES_RESOURCE` | `public static final String = "META-INF/druid/h2.properties"` | The bundled descriptor |
| `DEFAULT_PROPERTIES_RESOURCE` | `public static final String = H2_PROPERTIES_RESOURCE` | Alias used as the annotation default |
| `PACKAGE_NAME_PREFIX` | `public static final String = "com.alibaba.druid."` | Used by the extension to recognize Druid types |
| `getDefaultProperties` | `public static Properties getDefaultProperties() throws IOException` | Loads `DEFAULT_PROPERTIES_RESOURCE` |
| `loadProperties` | `public static Properties loadProperties(String resourceName) throws IOException` | Loads any classpath properties resource |
| `loadProperties` | `public static Properties loadProperties(String[] keyValuePairs) throws IOException` | Builds `Properties` from `"key = value"` strings |
| `buildDefaultDruidDataSource` | `public static DruidDataSource buildDefaultDruidDataSource() throws IOException` | `buildDruidDataSource(getDefaultProperties())`; **not** initialized — call `init()` |
| `buildDruidDataSource` | `public static DruidDataSource buildDruidDataSource(Properties properties)` | Wraps `new DruidDataSource()` + `configFromProperties(properties)` |

The bundled `META-INF/druid/h2.properties` and its verified content (from `AlibabaDruidTestUtilsTest`):

```properties
druid.name=h2
druid.url=jdbc:h2:mem:test_mem
druid.username=sa
druid.password=
druid.driverClassName=org.h2.Driver
```

```java
Properties properties = getDefaultProperties();
assertEquals("h2", properties.getProperty("druid.name"));
assertEquals("jdbc:h2:mem:test_mem", properties.getProperty("druid.url"));
assertEquals("sa", properties.getProperty("druid.username"));
assertEquals("", properties.getProperty("druid.password"));
assertEquals("org.h2.Driver", properties.getProperty("druid.driverClassName"));
```

All keys use the `druid.` prefix accepted by `DruidDataSource.configFromProperties(Properties)`.

### `io.microsphere.alibaba.druid.test.AbstractAlibabaDruidTest`

`public abstract class AbstractAlibabaDruidTest` (since `1.0.0`). Lifecycle:

| Hook | Annotation | Behavior |
|---|---|---|
| `init()` | `@BeforeEach` | `buildDruidDataSource()` → `customize(dataSource)` → `dataSource.init()` → creates table `users (id INT, name VARCHAR(50))` |
| `buildDruidDataSource()` | — | `protected DruidDataSource buildDruidDataSource() throws IOException`, returns `buildDefaultDruidDataSource()`; override to supply your own |
| `customize(DruidDataSource)` | — | `protected void customize(DruidDataSource dataSource)` — empty by default; the place to add filters |
| `getDruidDataSource()` | — | `protected DruidDataSource getDruidDataSource()` — the live pool |
| `test()` | `@Test` | Runs `testExecuteStatement()` then `testExecutePreparedStatement()` — so subclasses inherit an end-to-end interception test for free |
| `testExecuteStatement()` | — | `INSERT` (all four `executeUpdate` overloads), `addBatch` + `executeBatch`, `executeQuery`, `UPDATE`, `DELETE` (all `execute` overloads) |
| `testExecutePreparedStatement()` | — | `prepareStatement(sql)` + `executeUpdate`, `prepareStatement(sql, TYPE_FORWARD_ONLY)` + `executeQuery`, `prepareStatement(sql, TYPE_FORWARD_ONLY, CONCUR_UPDATABLE)` + `execute` |
| `executeStatement(ThrowableConsumer<Statement>)` / `executePreparedStatement(String, ThrowableConsumer<PreparedStatement>)` / `executeConnection(ThrowableConsumer<Connection>)` | — | resource-managing helpers; everything is closed in a `finally` block, throwables are declared `throws Throwable` |
| `destroy()` | `@AfterEach` | `DROP TABLE users` then `dataSource.close()` |

Simplest possible usage (as `DefaultAlibabaDruidTest` does):

```java
public class DefaultAlibabaDruidTest extends AbstractAlibabaDruidTest {
}
```

Adding a filter under test:

```java
public abstract class AbstractFilterTest<F extends Filter> extends AbstractAlibabaDruidTest {

    @Override
    protected void customize(DruidDataSource dataSource) {
        dataSource.getProxyFilters().add(createFilter());
    }

    protected abstract F createFilter();
}
```

*(This is `AbstractFilterTest` from the `microsphere-alibaba-druid-core` test sources;
`LoggingStatementFilterTest` extends it and returns `new LoggingStatementFilter()`.)*

### `io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidTest`

`@Target(TYPE) @Retention(RUNTIME) @ExtendWith(AlibabaDruidTestExtension.class) @Inherited public @interface
AlibabaDruidTest` (since `1.0.0`).

| Attribute | Type | Default | Meaning |
|---|---|---|---|
| `propertiesResource` | `String` | `AlibabaDruidTestUtils.DEFAULT_PROPERTIES_RESOURCE` | Classpath properties resource used to configure the pool |
| `properties` | `String[]` | `{}` | `"key = value"` pairs that **override** the resource, e.g. `"druid.url=jdbc:mysql://localhost:3306/test"` |
| `filters` | `String[]` | `{}` | Filter names passed to `DruidDataSource.setFilters(String)` (Druid built-ins such as `log`, `stat`, `wall`) |

Note: `filters` is declared on the annotation; the extension's data-source builder applies the `properties`
override on top of `propertiesResource` before calling `configFromProperties(...)` and `init()`.

### `io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidRuntime`

`@Target({FIELD, PARAMETER}) @Retention(RUNTIME) public @interface AlibabaDruidRuntime` (since `1.0.0`) — marks the
injection point. The target type must be a Druid candidate type: `DataSource`-assignable or a class whose name starts
with `com.alibaba.druid.` (the candidate list is computed once by
`findAllClasses(DruidDataSource.class, AlibabaDruidTestExtension::isAlibabaDruidClass)`).

### `io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidTestExtension`

Package-private `class AlibabaDruidTestExtension implements BeforeAllCallback, AfterAllCallback,
TestInstancePostProcessor, AfterEachCallback, ParameterResolver` (since `1.0.0`) — activated only through
`@AlibabaDruidTest`.

| Callback | Effect |
|---|---|
| `beforeAll` | Injects `@AlibabaDruidRuntime` **static** fields (one pool per test class) |
| `postProcessTestInstance` | Injects `@AlibabaDruidRuntime` **instance** fields (one pool per method) |
| `afterEach` | Retrieves and closes the method-level pool |
| `afterAll` | Retrieves and removes the class-level pool, then closes it |
| `supportsParameter` / `resolveParameter` | Resolves `@AlibabaDruidRuntime`-annotated test-method parameters |

Pools live in JUnit `Store`s obtained via `context.getStore(StoreScope.EXTENSION_CONTEXT, namespace)` with namespaces
`METHOD` and `CLASS`; the store key is `"<method signature or test class name>:<component class name>"`. `close(...)`
is null-safe and idempotent (it checks `isClosed()`).

## Usage

### Injection into instance fields, static fields and parameters

Mirrored from `AlibabaDruidTestExtensionTest` (`microsphere-alibaba-druid-test` test sources):

```java
@AlibabaDruidTest
class AlibabaDruidTestExtensionTest {

    @AlibabaDruidRuntime
    private static DruidDataSource druidDataSource;

    @AlibabaDruidRuntime
    private static DataSource dataSource;

    @Test
    @DisplayName("Test : Static Field Injection")
    void test() {
        assertObjects(druidDataSource, dataSource);
        close(druidDataSource);
        close(druidDataSource);
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

### Use the injected pool in a unit test

`DruidDataSourcePoolMetadataTest` (spring-boot test sources) is a typical consumer:

```java
@AlibabaDruidTest
class DruidDataSourcePoolMetadataTest {

    @AlibabaDruidRuntime
    private DruidDataSource druidDataSource;

    @Test
    void test() {
        DruidDataSourcePoolMetadata metadata = new DruidDataSourcePoolMetadata(druidDataSource);
        assertNotNull(metadata.getUsage());
    }
}
```

### Point the tests at another database

```java
@AlibabaDruidTest(
        propertiesResource = "META-INF/druid/mysql.properties",
        properties = {
                "druid.url=jdbc:mysql://localhost:3306/test",
                "druid.username=root",
                "druid.driverClassName=com.mysql.jdbc.Driver"
        }
)
class MysqlFilterTest extends AbstractAlibabaDruidTest {
}
```

`properties` entries win over the resource, so you can override a single key without writing a second file.

### No Spring needed

This module is used directly by `core` and indirectly by `spring-test`. If your test does not need a container,
prefer `@AlibabaDruidTest` / `AbstractAlibabaDruidTest` over
[microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md).

## Configuration Properties

The module reads no application properties. It configures `DruidDataSource` through `druid.*` keys only, which
`DruidDataSource.configFromProperties(Properties)` understands:

| Key (from `META-INF/druid/h2.properties`) | Value | Effect |
|---|---|---|
| `druid.name` | `h2` | Pool name |
| `druid.url` | `jdbc:h2:mem:test_mem` | In-memory H2 database |
| `druid.username` | `sa` | User |
| `druid.password` | *(empty)* | Password |
| `druid.driverClassName` | `org.h2.Driver` | JDBC driver |

The `microsphere.alibaba.druid.*` properties of the Boot module do not apply to this test module.

## Version Information

| Item | Value |
|---|---|
| Documentation line | `1.x` (generated from the `release-1.x` branch) |
| Latest released version | `0.1.21` (`0.1.22-SNAPSHOT` is the unpublished branch version) |
| Since (Javadoc) | `1.0.0` for `AbstractAlibabaDruidTest`, `AlibabaDruidTestUtils`, `@AlibabaDruidTest`, `@AlibabaDruidRuntime`, `AlibabaDruidTestExtension` |
| Java baseline | `8` — inherited from the external parent chain (`microsphere-spring-cloud-parent:0.1.26` → `microsphere-build:0.3.14`); **not declared** in this repository |
| CI JDK matrix | 8 / 11 / 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`, managed by `microsphere-alibaba-druid-parent` |
| JUnit | Jupiter 5.x as provided by the Boot line in effect (`Hoxton.SR12` → Boot `2.3.12.RELEASE`, `2021.0.9` → Boot `2.6.15`) |

## Compatibility Range

| Dimension | Supported on the `1.x` line |
|---|---|
| Alibaba Druid | `1.2.28` |
| Java | 8 and newer |
| JUnit | Jupiter 5 (the `Extension` APIs used — `StoreScope.EXTENSION_CONTEXT`, `Namespace` — require a JUnit 5 build that provides `StoreScope`) |
| Spring | not required — this module has no Spring dependency |
| Databases | any driver you put on the test classpath; H2 is the shipped default |

## Differences from the `current` line

The public API is identical. Only one test in this module is structured differently on `release-1.x`:
`AlibabaDruidTestExtensionTest` declares the static-field-injection case in the top-level class instead of a `@Nested
StaticFieldTest` class — an internal arrangement with no effect on the API you use. Additionally this artifact targets
JDK 8 and the Boot 2 matrix (`Hoxton` – `2021.0.x`).

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)
- [Project README](../../../README.md)
- [current-line version of this document](../../current/user-guide/microsphere-alibaba-druid-test.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-test.md](../developer-guide/microsphere-alibaba-druid-test.md)

## 中文

- [简体中文：microsphere-alibaba-druid-test](zh/microsphere-alibaba-druid-test.md)
