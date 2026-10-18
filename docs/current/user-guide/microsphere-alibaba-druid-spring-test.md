# microsphere-alibaba-druid-spring-test

[中文](zh/microsphere-alibaba-druid-spring-test.md)

## Purpose

`microsphere-alibaba-druid-spring-test` adapts the plain test toolkit of `microsphere-alibaba-druid-test` to
Spring-based tests. It provides:

- `AbstractDruidSpringTest` — the same SQL-driving test base class as `AbstractAlibabaDruidTest`, but the
  `DruidDataSource` it exercises is looked up from the Spring `BeanFactory` instead of being built from properties.
- `DruidDataSourceTestConfiguration` — a ready-made `@Configuration`-style class that contributes a prototype-scoped,
  H2-backed `DruidDataSource` bean (`init` / `close` lifecycle) for test contexts.

This is what the `spring`, `spring-boot` and `spring-cloud` modules use in their own test sources, so copying their
tests is the fastest way to write yours.

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
| Version | managed by the `microsphere-alibaba-druid-dependencies` BOM (`0.2.21` on the `current` line) |
| Packaging | `jar` |
| Compile dependency | `microsphere-alibaba-druid-test` (non-optional, hence `@AlibabaDruidTest` and friends are available in tests) |
| Optional dependencies | `org.junit.jupiter:junit-jupiter`, `spring-test`, `spring-context`, `microsphere-spring-test`, `microsphere-spring-context`, `com.alibaba:druid`, `com.h2database:h2`, `ch.qos.logback:logback-classic` |
| Note | it does **not** depend on `microsphere-alibaba-druid-core` or `microsphere-alibaba-druid-spring` |

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

Spring, `spring-test`, Druid and H2 are optional, so a consuming test module declares them itself — typically alongside
`microsphere-alibaba-druid-spring` (to get `@EnableAlibabaDruid` / `DruidDataSourceBeanPostProcessor`).

## API Reference

### `io.microsphere.alibaba.druid.test.spring.AbstractDruidSpringTest`

`public abstract class AbstractDruidSpringTest extends AbstractAlibabaDruidTest` (since `1.0.0`).

| Member | Signature | Semantics |
|---|---|---|
| `beanFactory` | `@Autowired private BeanFactory beanFactory` | Injected by the Spring test context |
| `buildDruidDataSource` | `protected DruidDataSource buildDruidDataSource()` | Overrides the base implementation: returns `beanFactory.getBean(DruidDataSource.class)` instead of building one from `META-INF/druid/h2.properties` |

Everything else is inherited unchanged from `AbstractAlibabaDruidTest` — `init()` / `test()` / `destroy()`, the
`customize(DruidDataSource)` hook and the `executeStatement` / `executePreparedStatement` / `executeConnection` helpers.
See [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md) for that lifecycle.

Because the data source now comes from the container, filters must be attached by Spring (via
`@EnableAlibabaDruid` or a manually registered `DruidDataSourceBeanPostProcessor`) rather than by `customize(...)`.

### `io.microsphere.alibaba.druid.test.spring.DruidDataSourceTestConfiguration`

`public class DruidDataSourceTestConfiguration` (since `1.0.0`) — used as a `@ContextConfiguration(classes = ...)`
participant, not annotated with `@Configuration` itself.

| Bean method | Declaration | Notes |
|---|---|---|
| `dataSource` | `@Bean(initMethod = "init", destroyMethod = "close") @Scope(SCOPE_PROTOTYPE) public DruidDataSource dataSource() throws IOException` | Returns `AlibabaDruidTestUtils.buildDefaultDruidDataSource()`; Spring invokes `init()` and `close()` as lifecycle callbacks |

The `SCOPE_PROTOTYPE` scope is deliberate: `AbstractAlibabaDruidTest#destroy()` closes the pool after every test
method, and `init()` requests a fresh one before the next — with a singleton bean the second test would receive a closed
data source.

## Usage

### The minimal Spring test

`DefaultDruidSpringTest` (test sources of this module):

```java
@ExtendWith(value = SpringExtension.class)
@ContextConfiguration(classes = {
        DruidDataSourceTestConfiguration.class,
        DefaultDruidSpringTest.class
})
public class DefaultDruidSpringTest extends AbstractDruidSpringTest {
}
```

Running it already executes the full DML/DQL workload of `AbstractAlibabaDruidTest` against a Spring-managed,
H2-backed `DruidDataSource`.

### Add SQL interception to the test

From `EnableAlibabaDruidTest` in the `spring` module (note: the attribute is `filterClasses`, and the filter search
scopes are `BeanSource` values):

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

Or register the post-processor explicitly, as `DruidDataSourceBeanPostProcessorTest` does:

```java
@SpringLoggingTest
@ExtendWith(value = SpringExtension.class)
@ContextConfiguration(classes = {
        LoggingStatementFilter.class,
        DruidDataSourceTestConfiguration.class,
        DruidDataSourceBeanPostProcessor.class,
        DruidDataSourceBeanPostProcessorTest.class,
})
public class DruidDataSourceBeanPostProcessorTest extends AbstractDruidSpringTest {

}
```

Both classes are test sources of `microsphere-alibaba-druid-spring`; the Boot equivalent is
`AlibabaDruidAutoConfigurationIntegrationTest` (see
[microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)).

### Contribute your own data source instead

Register any `DruidDataSource` bean (a `@Configuration` of your own, `spring.datasource.*` auto-configuration, an
embedded database) and extend `AbstractDruidSpringTest` — the base class only asks the `BeanFactory` for
`DruidDataSource.class`, so the test configuration class is optional.

## Configuration Properties

This module binds no properties of its own:

| Property | Type | Default | Relevance here |
|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true` (`matchIfMissing = true`) | Only when the auto-configuration is also on the class path (Boot tests) |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | Only in Boot tests; in plain Spring tests the equivalent is `@EnableAlibabaDruid(filterClasses = ...)` |
| — | — | — | The data source itself comes from `META-INF/druid/h2.properties` via `DruidDataSourceTestConfiguration` |

## Version Information

| Item | Value |
|---|---|
| Documentation line | `current` (release branch) |
| Latest released version | `0.2.21` (`0.2.22-SNAPSHOT` is the unpublished development version on `main`) |
| Since (Javadoc) | `1.0.0` for `AbstractDruidSpringTest`, `DruidDataSourceTestConfiguration` |
| Java | 17+ (`<java.version>17</java.version>` in the root `pom.xml`) |
| CI JDK matrix | 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`, managed by `microsphere-alibaba-druid-parent` |

## Compatibility Range

| Dimension | Supported on the `current` line |
|---|---|
| Alibaba Druid | `1.2.28` (as managed by the parent POM) |
| Java | 17, 21, 25 (JDK 17+ required) |
| JUnit | JUnit Jupiter 5 with `spring-test` (`SpringExtension`, `@SpringJUnitConfig`) |
| Spring Framework | the version line provided by `microsphere-spring-cloud-dependencies` `0.2.26` (Spring `6.0.x` – `6.2.x`) |
| Database | H2 in-memory by default (`jdbc:h2:mem:test_mem`); any Druid-configurable data source works |

Spring Cloud `Hoxton` – `2021.0.x` / JDK 8 users: see the [1.x User Guide](../../1.x/user-guide/README.md).

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
- [Project README](../../../README.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-spring-test.md](../developer-guide/microsphere-alibaba-druid-spring-test.md)

## 中文

- [简体中文：microsphere-alibaba-druid-spring-test](zh/microsphere-alibaba-druid-spring-test.md)
