# microsphere-alibaba-druid-spring-boot

[中文](zh/microsphere-alibaba-druid-spring-boot.md)

## Purpose

`microsphere-alibaba-druid-spring-boot` turns the Spring integration of `microsphere-alibaba-druid-spring` into Spring
Boot auto-configuration. It provides:

- `AlibabaDruidAutoConfiguration` — registers a `DruidDataSourceBeanPostProcessor` (filter injection) and a
  `DataSourcePoolMetadataProvider` (Actuator pool metrics), activated by
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- `AlibabaDruidProperties` — the `microsphere.alibaba.druid.*` bound properties (`enabled`, `filter.classes`).
- `@ConditionalOnAlibabaDruidEnabled` and `@ConditionalOnAlibabaDruidAvailable` — the conditional annotations that gate
  the auto-configuration.
- `DruidDataSourcePoolMetadata` — a Spring Boot `DataSourcePoolMetadata` implementation over `DruidDataSource`, so
  `/actuator/health` and `/actuator/metrics` report Druid pool statistics.

## Maven Coordinates

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-spring-boot</artifactId>
</dependency>
```

| Item | Value |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-spring-boot` |
| Version | managed by the `microsphere-alibaba-druid-dependencies` BOM (`0.2.21` on the `current` line) |
| Packaging | `jar` |
| Compile dependency | `microsphere-alibaba-druid-spring` (non-optional, hence `core` as well) |
| Optional dependencies | `microsphere-annotation-processor`, `microsphere-spring-boot-core`, `com.alibaba:druid`, `spring-boot-starter`, `spring-boot-starter-actuator`, `spring-boot-configuration-processor`, `spring-boot-autoconfigure-processor` |

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

Spring Boot, the Actuator and `com.alibaba:druid` are all **optional** here, so add them (plus your JDBC driver) to
your application:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-druid-spring-boot</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid</artifactId>
    </dependency>

    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

The Druid version (`1.2.28`) is managed by [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md).

## API Reference

### Registration

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:

```text
io.microsphere.alibaba.druid.spring.boot.autoconfigure.AlibabaDruidAutoConfiguration
```

Boot 3 style registration only — there is no `spring.factories` `EnableAutoConfiguration` entry in this module.

### `io.microsphere.alibaba.druid.spring.boot.autoconfigure.AlibabaDruidAutoConfiguration`

`public class AlibabaDruidAutoConfiguration` (since `1.0.0`), annotated
`@ConditionalOnAlibabaDruidAvailable` and `@EnableConfigurationProperties(AlibabaDruidProperties.class)`.

| Bean method | Signature | Semantics |
|---|---|---|
| `druidDataSourceBeanPostProcessor` | `@Bean(name = BEAN_NAME) public BeanPostProcessor druidDataSourceBeanPostProcessor(AlibabaDruidProperties alibabaDruidProperties)` | Builds a `DruidDataSourceBeanPostProcessor` from `alibabaDruidProperties.getFilter().getClasses()`; the declared return type is `BeanPostProcessor` and the bean name is the constant `DruidDataSourceBeanPostProcessor.BEAN_NAME` = `druidDataSourceBeanPostProcessor` |
| `druidDataSourcePoolMetadataProvider` | `@Bean @ConditionalOnBean(DruidDataSource.class) public DataSourcePoolMetadataProvider druidDataSourcePoolMetadataProvider()` | Unwraps a `DataSource` via `DataSourceUnwrapper.unwrap(dataSource, DruidDataSourceMBean.class, DruidDataSource.class)`; returns a `DruidDataSourcePoolMetadata` when it is a Druid data source, `null` otherwise |

> Because `druidDataSourcePoolMetadataProvider` is guarded by `@ConditionalOnBean(DruidDataSource.class)`, pool metrics
> require an actual `DruidDataSource` **bean** — a data source created outside the container is not enough.

### `io.microsphere.alibaba.druid.spring.boot.AlibabaDruidProperties`

`@ConfigurationProperties(prefix = ALIBABA_DRUID_PROPERTY_NAME_PREFIX) public class AlibabaDruidProperties` — the
prefix resolves to `microsphere.alibaba.druid` (since `1.0.0`).

| Member | Type | Default | Description |
|---|---|---|---|
| `isEnabled()` / `setEnabled(boolean)` | `boolean` | `true` | The master switch mirrored by `microsphere.alibaba.druid.enabled` |
| `getFilter()` / `setFilter(Filter)` | `AlibabaDruidProperties.Filter` | `new Filter()` | Nested filter properties |
| `Filter.getClasses()` / `Filter.setClasses(...)` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `of(com.alibaba.druid.filter.Filter.class)` — i.e. `[com.alibaba.druid.filter.Filter]` | The filter bean types injected into every `DruidDataSource`; `Filter.class` means "all filter subtypes" |

### `io.microsphere.alibaba.druid.spring.boot.condition.ConditionalOnAlibabaDruidEnabled`

`public @interface ConditionalOnAlibabaDruidEnabled` (since `1.0.0`) — `@Target(TYPE)`, `@Retention(RUNTIME)`,
`@Documented`, meta-annotated:

```java
@ConditionalOnProperty(name = ALIBABA_DRUID_ENABLED_PROPERTY_NAME, matchIfMissing = true)
```

so the auto-configuration is active when `microsphere.alibaba.druid.enabled` is `true` **or absent**, and is switched
off by `microsphere.alibaba.druid.enabled=false`. (`ALIBABA_DRUID_ENABLED_PROPERTY_NAME` is
`microsphere.alibaba.druid.enabled`; the class Javadoc mentions an older `microsphere.spring.boot.alibaba.druid.enabled`
name, which the code does not use.)

### `io.microsphere.alibaba.druid.spring.boot.condition.ConditionalOnAlibabaDruidAvailable`

`public @interface ConditionalOnAlibabaDruidAvailable` (since `1.0.0`) — `@Target(TYPE)`, `@Retention(RUNTIME)`,
`@Documented`, a composite of `@ConditionalOnAlibabaDruidEnabled` and:

```java
@ConditionalOnClass(name = {
        "com.alibaba.druid.pool.DruidDataSource"                   // Alibaba Druid API
})
```

Both conditions must hold. `@ConditionalOnClass` uses `name` (string form), so it also works when Druid is missing at
runtime.

### `io.microsphere.alibaba.druid.spring.boot.metadata.DruidDataSourcePoolMetadata`

`public class DruidDataSourcePoolMetadata implements DataSourcePoolMetadata` (since `1.0.0`), constructed with a
`DruidDataSource`.

| Method | Druid source | Notes |
|---|---|---|
| `Float getUsage()` | `getActiveCount()` / `getMaxActive()` | `0.0f` when no connection is active, `1.0f` when `active == maxActive`, otherwise `active / (maxActive * 1.0f)`; never `null` |
| `Integer getActive()` | `getActiveCount()` | Active (in-use) connections |
| `Integer getIdle()` | `getMaxIdle()` | *Maximum* idle connections, not the current idle count |
| `Integer getMax()` | `getMaxActive()` | Maximum active connections |
| `Integer getMin()` | `getMinIdle()` | Minimum idle connections |
| `String getValidationQuery()` | `getValidationQuery()` | `null` when no validation query is configured |
| `Boolean getDefaultAutoCommit()` | `isDefaultAutoCommit()` | Default auto-commit flag |

## Usage

### Auto-configuration in action

The integration test of this module (`AlibabaDruidAutoConfigurationIntegrationTest`, test sources) shows the whole
setup — a `DruidDataSource` bean plus a filter bean, with properties pointing at that filter type:

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

    @Autowired
    private AlibabaDruidProperties alibabaDruidProperties;

    @Autowired
    private LoggingStatementFilter loggingStatementFilter;

    @Autowired
    private DataSourcePoolMetadataProvider dataSourcePoolMetadataProvider;

    @Autowired
    private DataSource dataSource;

    @Test
    public void test() throws Throwable {
        super.test();
        assertNotNull(alibabaDruidProperties);
        assertNotNull(loggingStatementFilter);

        AlibabaDruidProperties.Filter filter = alibabaDruidProperties.getFilter();
        assertNotNull(filter);

        Class<? extends com.alibaba.druid.filter.Filter>[] classes = filter.getClasses();
        assertNotNull(classes);
        assertArrayEquals(of(LoggingStatementFilter.class), classes);

        DataSourcePoolMetadata dataSourcePoolMetadata = dataSourcePoolMetadataProvider.getDataSourcePoolMetadata(dataSource);
        assertInstanceOf(DruidDataSourcePoolMetadata.class, dataSourcePoolMetadata);
    }
}
```

In an application the equivalent is just:

```properties
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.url=jdbc:h2:mem:test_mem
spring.datasource.driver-class-name=org.h2.Driver

microsphere.alibaba.druid.enabled=true
microsphere.alibaba.druid.filter.classes=io.microsphere.alibaba.druid.filter.LoggingStatementFilter

management.endpoints.web.exposure.include=health,metrics
```

> `microsphere.alibaba.druid.filter.classes` accepts class **names**; the binder converts them to `Class` values. A name
> that does not resolve to a `Filter` subtype fails the binding, and a class listed here but not registered as a bean
> injects nothing.

### Reading the bound properties

Adapted from `AlibabaDruidPropertiesTest` (test sources); `isNotEmpty` and `ofArray` come from
`io.microsphere.util.ArrayUtils`:

```java
AlibabaDruidProperties properties = new AlibabaDruidProperties();
assertTrue(properties.isEnabled());

properties.setEnabled(false);
assertFalse(properties.isEnabled());

assertNotNull(properties.getFilter());

AlibabaDruidProperties.Filter filter = new AlibabaDruidProperties.Filter();
assertTrue(isNotEmpty(filter.getClasses()));

filter.setClasses(ofArray(com.alibaba.druid.filter.Filter.class));
assertArrayEquals(ofArray(com.alibaba.druid.filter.Filter.class), filter.getClasses());
```

### Pool metadata

From `DruidDataSourcePoolMetadataTest` (test sources), which uses the JUnit 5 annotations of
[microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md):

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

    @Test
    void test() {
        assertNotNull(this.metadata.getUsage());
        assertEquals(valueOf(0), this.metadata.getActive());
        assertEquals(valueOf(8), this.metadata.getIdle());
        assertEquals(valueOf(8), this.metadata.getMax());
        assertEquals(valueOf(0), this.metadata.getMin());
        assertNull(this.metadata.getValidationQuery());
        assertTrue(this.metadata.getDefaultAutoCommit());
    }
}
```

The values (`maxActive = 8`, `maxIdle = 8`, `minIdle = 0`) are Druid defaults, not something this module configures.

### Verifying the auto-configuration in your own tests

`AlibabaDruidAutoConfigurationTest` (test sources) extends `AutoConfigurationTest` from `microsphere-spring-boot-test`
and declares the expected outcomes:

```java
@SpringBootTest(
        classes = {
                AlibabaDruidAutoConfigurationTest.class
        },
        webEnvironment = NONE
)
class AlibabaDruidAutoConfigurationTest extends AutoConfigurationTest<AlibabaDruidAutoConfiguration> {

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
        autoConfiguredClasses.add(DruidDataSourceBeanPostProcessor.class);
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues) {
        globalDisabledPropertyValues.add("microsphere.alibaba.druid.enabled=false");
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses) {
        globalMissingClasses.add(DruidDataSource.class);
    }
}
```

## Configuration Properties

| Property | Type | Default | Bound by | Consumed by |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true` (`matchIfMissing = true`) | `AlibabaDruidProperties.enabled`, `PropertyConstants` (core) | `@ConditionalOnAlibabaDruidEnabled`, `@ConditionalOnAlibabaDruidAvailable` |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter.classes` | `AlibabaDruidAutoConfiguration.druidDataSourceBeanPostProcessor(...)` |

## Version Information

| Item | Value |
|---|---|
| Documentation line | `current` (release branch) |
| Latest released version | `0.2.21` (`0.2.22-SNAPSHOT` is the unpublished development version on `main`) |
| Since (Javadoc) | `1.0.0` for `AlibabaDruidProperties`, `AlibabaDruidAutoConfiguration`, both conditions, `DruidDataSourcePoolMetadata` |
| Java | 17+ (`<java.version>17</java.version>` in the root `pom.xml`) |
| CI JDK matrix | 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`, managed by `microsphere-alibaba-druid-parent` |

## Compatibility Range

| Dimension | Supported on the `current` line |
|---|---|
| Alibaba Druid | `1.2.28` (as managed by the parent POM) |
| Java | 17, 21, 25 (JDK 17+ required) |
| Spring Boot | `3.0.x` – `3.5.x`; verified against `3.0.13`, `3.3.13`, `3.4.13`, `3.5.15` — versions inherited from the external parent POM chain `microsphere-spring-cloud-parent:0.2.26`, not declared in this repository |
| Spring Cloud | `2022.0.x` – `2025.0.x` (`2022.0.5`, `2023.0.6`, `2024.0.3`, `2025.0.3`) |
| Auto-configuration style | Boot 3 `AutoConfiguration.imports`; Boot 2 `spring.factories` is not supported on this line |

Boot 2.x / JDK 8 users: see the [1.x User Guide](../../1.x/user-guide/README.md).

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-cloud.md](microsphere-alibaba-druid-spring-cloud.md)
- [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)
- [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)
- [Project README](../../../README.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-spring-boot.md](../developer-guide/microsphere-alibaba-druid-spring-boot.md)

## 中文

- [简体中文：microsphere-alibaba-druid-spring-boot](zh/microsphere-alibaba-druid-spring-boot.md)
