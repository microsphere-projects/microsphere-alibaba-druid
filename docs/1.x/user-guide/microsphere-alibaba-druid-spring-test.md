# microsphere-alibaba-druid-spring-test (`1.x`)

[中文](zh/microsphere-alibaba-druid-spring-test.md)

## Purpose

`microsphere-alibaba-druid-spring-test` is the Spring-aware companion of `microsphere-alibaba-druid-test` on the
`1.x` line. It lets the same statement-execution matrix run against a `DruidDataSource` that lives **inside a Spring
context**, which is exactly what you need to verify filter injection:

- `AbstractDruidSpringTest` — extends `AbstractAlibabaDruidTest` but sources the data source from the `BeanFactory`
  instead of building one in code.
- `DruidDataSourceTestConfiguration` — a `@Configuration`-style class that registers a prototype-scoped
  `DruidDataSource` bean built from the default H2 properties.

## Maven Coordinates

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-spring-test</artifactId>
    <scope>test</scope>
</dependency>
```

| Item | Value |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-spring-test` |
| Version | managed by the `microsphere-alibaba-druid-dependencies` BOM (`0.1.21` on the `1.x` line) |
| Packaging | `jar` (main sources, not test fixtures) |
| Compile dependency | `microsphere-alibaba-druid-test` (non-optional) |
| Optional dependencies | `junit-jupiter`, `spring-test`, `spring-context`, `microsphere-spring-test`, `microsphere-spring-context`, `com.alibaba:druid`, `com.h2database:h2`, `logback-classic` |

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

testImplementation("io.github.microsphere-projects:microsphere-alibaba-druid-spring-test")
```

> Only `microsphere-alibaba-druid-test` comes in transitively. You must add `spring-test`, `spring-context`,
> `junit-jupiter`, `com.alibaba:druid` and `com.h2database:h2` yourself, because all of them are `optional` here.

## API Reference

### `io.microsphere.alibaba.druid.test.spring.AbstractDruidSpringTest`

`public abstract class AbstractDruidSpringTest extends AbstractAlibabaDruidTest` (since `1.0.0`).

| Member | Signature | Notes |
|---|---|---|
| `beanFactory` | `@Autowired private BeanFactory beanFactory` | Injected by the Spring TestContext framework; requires a Spring test extension to be active |
| `buildDruidDataSource()` | `protected DruidDataSource buildDruidDataSource()` | **Override** of the parent hook: returns `beanFactory.getBean(DruidDataSource.class)` — no `IOException` any more, and the data source is *already* initialized by the container |

Everything else (`@BeforeEach init()`, `@Test test()`, the `Statement`/`PreparedStatement` matrix, `@AfterEach destroy()`,
the `customize(...)` and `getDruidDataSource()` hooks) is inherited unchanged from
`AbstractAlibabaDruidTest` — see [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md).

Two consequences of pulling the bean from the container:

1. `customize(DruidDataSource)` is still called, but the returned instance is the container's bean, so the filters a
   `DruidDataSourceBeanPostProcessor` adds are visible to the test.
2. `init()` on an already-initialized `DruidDataSource` is a no-op in Druid, so the template lifecycle stays safe.

### `io.microsphere.alibaba.druid.test.spring.DruidDataSourceTestConfiguration`

`public class DruidDataSourceTestConfiguration` (since `1.0.0`) — one bean definition:

```java
@Bean(initMethod = "init", destroyMethod = "close")
@Scope(SCOPE_PROTOTYPE)
public DruidDataSource dataSource() throws IOException {
    return buildDefaultDruidDataSource();
}
```

| Aspect | Value | Why |
|---|---|---|
| Bean name | `dataSource` | Resolves `beanFactory.getBean(DruidDataSource.class)` by type |
| `initMethod` / `destroyMethod` | `init` / `close` | The container manages the pool lifecycle |
| Scope | `SCOPE_PROTOTYPE` | Each `getBean(...)` returns a **new** pool. `AbstractAlibabaDruidTest.destroy()` closes the pool after every test method, so a shared singleton would be unusable from the second method onward |
| Source of settings | `AlibabaDruidTestUtils.buildDefaultDruidDataSource()` | i.e. `META-INF/druid/h2.properties` from the `test` module |

## Usage

### Plain Spring test context

Exactly what `DefaultDruidSpringTest` (this module's own test sources) does:

```java
@ExtendWith(value = SpringExtension.class)
@ContextConfiguration(classes = {
        DruidDataSourceTestConfiguration.class,
        DefaultDruidSpringTest.class
})
public class DefaultDruidSpringTest extends AbstractDruidSpringTest {
}
```

### Spring Boot test context

The Boot and Cloud integration tests reuse the same base class and configuration (from
`AlibabaDruidAutoConfigurationIntegrationTest` and `AlibabaDruidCloudAutoConfigurationIntegrationTest`):

```java
@SpringBootTest(
        classes = {
                LoggingStatementFilter.class,
                DruidDataSourceTestConfiguration.class,
                AlibabaDruidAutoConfigurationIntegrationTest.class
        },
        webEnvironment = NONE,
        properties = {
                "microsphere.alibaba.druid.enabled=true",
                "microsphere.alibaba.druid.filter.classes=io.microsphere.alibaba.druid.filter.LoggingStatementFilter"
        })
@EnableAutoConfiguration
public class AlibabaDruidAutoConfigurationIntegrationTest extends AbstractDruidSpringTest {
}
```

Because the class extends `AbstractDruidSpringTest`, the inherited `@Test test()` executes the whole SQL matrix
*through* the auto-configured `DruidDataSourceBeanPostProcessor` — that is how filter injection gets covered.

### Verifying `@EnableAlibabaDruid`

`EnableAlibabaDruidTest` (`microsphere-alibaba-druid-spring` test sources) shows the annotation + base-class
combination, with the `@SpringJUnitConfig` shortcut instead of `@ExtendWith` + `@ContextConfiguration`:

```java
@SpringJUnitConfig(classes = {
        LoggingStatementFilter.class,
        DruidDataSourceTestConfiguration.class,
        EnableAlibabaDruidTest.class,
})
@EnableAlibabaDruid(
        filterClasses = AbstractStatementFilter.class,
        sources = {
                BEAN_FACTORY,
                SPRING_FACTORIES,
                JAVA_SERVICE_PROVIDER
        }
)
public class EnableAlibabaDruidTest extends AbstractDruidSpringTest {
}
```

### Override the data source or the customization

```java
@SpringJUnitConfig(classes = {
        DruidDataSourceTestConfiguration.class,
        MyFilter.class,
        MyDruidFilterIT.class
})
public class MyDruidFilterIT extends AbstractDruidSpringTest {

    @Override
    protected void customize(DruidDataSource dataSource) {
        // the pool is already init()-ed by the container;
        // register filters here only if you need them before the SQL matrix runs
    }
}
```

## Configuration Properties

This module binds **no** `microsphere.alibaba.druid.*` property — it is test infrastructure, not an auto-configured
component. The pool is configured by `META-INF/druid/h2.properties` (`druid.*` keys, see
[microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)):

| Property | Applies here? | Notes |
|---|---|---|
| `microsphere.alibaba.druid.enabled` | No | Only the Boot/Cloud auto-configurations read it |
| `microsphere.alibaba.druid.filter.classes` | No — unless the Boot auto-configuration is active in your test | When active, the listed filter bean types are injected into the prototype `DruidDataSource` before initialization |
| `spring.*` datasource properties | No | The test configuration ignores Boot's datasource auto-configuration and registers its own bean |

## Version Information

| Item | Value |
|---|---|
| Documentation line | `1.x` (generated from the `release-1.x` branch) |
| Latest released version | `0.1.21` (`0.1.22-SNAPSHOT` is the unpublished branch version) |
| Since (Javadoc) | `1.0.0` for `AbstractDruidSpringTest` and `DruidDataSourceTestConfiguration` |
| Java baseline | `8` — inherited from the external parent chain (`microsphere-spring-cloud-parent:0.1.26` → `microsphere-build:0.3.14`); **not declared** in this repository |
| CI JDK matrix | 8 / 11 / 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`, managed by `microsphere-alibaba-druid-parent` |
| Spring Framework APIs used | `@Bean`, `@Scope`, `BeanFactory` (Spring 5.x on this line) |

## Compatibility Range

| Dimension | Supported on the `1.x` line |
|---|---|
| Alibaba Druid | `1.2.28` |
| Java | 8 and newer |
| Spring Test | `spring-test` 5.x (as shipped by Spring Boot 2.3 – 2.6); the module is framework-version agnostic otherwise |
| JUnit | Jupiter 5 |
| Spring Boot | not required — usable in plain `@ExtendWith(SpringExtension.class)` tests; also used by Boot/Cloud tests on this line |
| Database | H2 by default (prototype bean built from `META-INF/druid/h2.properties`) |

## Differences from the `current` line

The API is identical; this artifact is simply built against JDK 8 bytecode and the Spring 5.x / Boot 2.3–2.6
(`Hoxton` – `2021.0.x`) line instead of JDK 17 and Spring 6.x / Boot 3 (`2022.0.x` – `2025.0.x`).

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [Project README](../../../README.md)
- [current-line version of this document](../../current/user-guide/microsphere-alibaba-druid-spring-test.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-spring-test.md](../developer-guide/microsphere-alibaba-druid-spring-test.md)

## 中文

- [简体中文：microsphere-alibaba-druid-spring-test](zh/microsphere-alibaba-druid-spring-test.md)
