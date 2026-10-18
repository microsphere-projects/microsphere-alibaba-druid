# microsphere-alibaba-druid-spring-boot (`1.x`)

[中文](zh/microsphere-alibaba-druid-spring-boot.md)

## Purpose

`microsphere-alibaba-druid-spring-boot` turns the Spring integration into a Spring Boot experience on the `1.x`
line. It gives you:

- `AlibabaDruidAutoConfiguration` — registers the `DruidDataSourceBeanPostProcessor` (bean name
  `druidDataSourceBeanPostProcessor`) and a `DataSourcePoolMetadataProvider` for Druid.
- `AlibabaDruidProperties` — the `microsphere.alibaba.druid.*` `@ConfigurationProperties` binding, including the
  nested `Filter` type with its `classes` array.
- `@ConditionalOnAlibabaDruidEnabled` / `@ConditionalOnAlibabaDruidAvailable` — the property and classpath gates.
- `DruidDataSourcePoolMetadata` — the Actuator adapter exposing Druid pool counters (`usage`, `active`, `idle`,
  `max`, `min`, `validationQuery`, `defaultAutoCommit`).

**What is specific to this line:** the module ships *two* auto-configuration discovery files, so the same jar works
with Boot 2.3 through Boot 3.x.

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
| Version | managed by the `microsphere-alibaba-druid-dependencies` BOM (`0.1.21` on the `1.x` line) |
| Packaging | `jar` |
| Compile dependency | `microsphere-alibaba-druid-spring` (non-optional, brings in `core`) |
| Optional dependencies | `microsphere-annotation-processor`, `microsphere-spring-boot-core`, `com.alibaba:druid`, `spring-boot-starter`, `spring-boot-starter-actuator`, `spring-boot-configuration-processor`, `spring-boot-autoconfigure-processor` |

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

implementation("io.github.microsphere-projects:microsphere-alibaba-druid-spring-boot")
```

Spring Boot itself is optional, so add `spring-boot-starter` (and `spring-boot-starter-actuator` if you want the pool
metrics) plus `com.alibaba:druid` yourself.

## Dual Auto-Configuration Registration (1.x only)

`src/main/resources/META-INF/spring.factories` — the Boot 2.x registration style:

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.alibaba.druid.spring.boot.autoconfigure.AlibabaDruidAutoConfiguration
```

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` — kept as well:

```text
io.microsphere.alibaba.druid.spring.boot.autoconfigure.AlibabaDruidAutoConfiguration
```

| Boot version | File actually read | Effect |
|---|---|---|
| 2.3 – 2.6 (this line's matrix) | `spring.factories` | `AutoConfiguration.imports` is inert |
| 2.7 | both, de-duplicated by `AutoConfigurationImportSelector` | registered once |
| 3.x | `AutoConfiguration.imports` | the `EnableAutoConfiguration` key in `spring.factories` is ignored |

Nothing to configure: with `spring.factories` present, a Boot 2.3–2.6 application gets the auto-configuration picked
up automatically, exactly like the `current` line does with `AutoConfiguration.imports` alone.

## API Reference

### `io.microsphere.alibaba.druid.spring.boot.autoconfigure.AlibabaDruidAutoConfiguration`

`public class AlibabaDruidAutoConfiguration` (since `1.0.0`), annotated
`@ConditionalOnAlibabaDruidAvailable` and `@EnableConfigurationProperties(AlibabaDruidProperties.class)`.

| Bean method | Signature | Notes |
|---|---|---|
| `druidDataSourceBeanPostProcessor` | `@Bean(name = BEAN_NAME) public BeanPostProcessor druidDataSourceBeanPostProcessor(AlibabaDruidProperties alibabaDruidProperties)` | Returns `new DruidDataSourceBeanPostProcessor(alibabaDruidProperties.getFilter().getClasses())`. Declared return type is `BeanPostProcessor`, bean name is the constant `DruidDataSourceBeanPostProcessor.BEAN_NAME`. |
| `druidDataSourcePoolMetadataProvider` | `@Bean @ConditionalOnBean(DruidDataSource.class) public DataSourcePoolMetadataProvider druidDataSourcePoolMetadataProvider()` | Lambda: `DataSourceUnwrapper.unwrap(dataSource, DruidDataSourceMBean.class, DruidDataSource.class)`; returns `new DruidDataSourcePoolMetadata(druidDataSource)` when unwrapping succeeds, otherwise `null`. |

### `io.microsphere.alibaba.druid.spring.boot.AlibabaDruidProperties`

`@ConfigurationProperties(prefix = ALIBABA_DRUID_PROPERTY_NAME_PREFIX) public class AlibabaDruidProperties`
(since `1.0.0`) — prefix `microsphere.alibaba.druid`.

| Member | Type | Default | Property |
|---|---|---|---|
| `isEnabled()` / `setEnabled(boolean)` | `boolean` | `true` | `microsphere.alibaba.druid.enabled` |
| `getFilter()` / `setFilter(Filter)` | `AlibabaDruidProperties.Filter` | `new Filter()` | — (nested group) |
| `Filter.getClasses()` / `Filter.setClasses(...)` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `of(com.alibaba.druid.filter.Filter.class)` | `microsphere.alibaba.druid.filter.classes` |

`Filter` is the nested `public static class AlibabaDruidProperties.Filter`; its default `{ Filter.class }` means
"inject every `Filter` bean found in the context".

### `io.microsphere.alibaba.druid.spring.boot.condition.ConditionalOnAlibabaDruidEnabled`

`public @interface ConditionalOnAlibabaDruidEnabled` (since `1.0.0`) — `@Target(TYPE)`, `@Retention(RUNTIME)`,
meta-annotated `@ConditionalOnProperty(name = ALIBABA_DRUID_ENABLED_PROPERTY_NAME, matchIfMissing = true)`.
The module's Javadoc mentions the name `"microsphere.spring.boot.alibaba.druid.enabled"`; the actual constant is
`microsphere.alibaba.druid.enabled` — trust the constant.

### `io.microsphere.alibaba.druid.spring.boot.condition.ConditionalOnAlibabaDruidAvailable`

`public @interface ConditionalOnAlibabaDruidAvailable` (since `1.0.0`) — the composite gate
`@ConditionalOnAlibabaDruidEnabled` + `@ConditionalOnClass(name = "com.alibaba.druid.pool.DruidDataSource")`. Both
the Boot and the Cloud auto-configurations use it, which is why removing `com.alibaba:druid` from the classpath (or
setting `microsphere.alibaba.druid.enabled=false`) disables everything with no code change.

### `io.microsphere.alibaba.druid.spring.boot.metadata.DruidDataSourcePoolMetadata`

`public class DruidDataSourcePoolMetadata implements DataSourcePoolMetadata` (since `1.0.0`).

| Method | Druid source | Return |
|---|---|---|
| `getUsage()` | `getActiveCount()` / `getMaxActive()` | `0.0f` when no connection is active, `1.0f` when `active == maxActive`, otherwise `active / (maxActive * 1.0f)` |
| `getActive()` | `getActiveCount()` | in-use connections |
| `getIdle()` | `getMaxIdle()` | maximum idle connections |
| `getMax()` | `getMaxActive()` | maximum active connections |
| `getMin()` | `getMinIdle()` | minimum idle connections |
| `getValidationQuery()` | `getValidationQuery()` | may be `null` |
| `getDefaultAutoCommit()` | `isDefaultAutoCommit()` | auto-commit flag |

## Usage

### Enable it from `application.properties`

As exercised by `AlibabaDruidAutoConfigurationIntegrationTest` (test sources), which boots
`@EnableAutoConfiguration` with:

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

and then asserts the bound properties and the metadata provider:

```java
Class<? extends com.alibaba.druid.filter.Filter>[] classes = alibabaDruidProperties.getFilter().getClasses();
assertArrayEquals(of(LoggingStatementFilter.class), classes);

DataSourcePoolMetadata dataSourcePoolMetadata = dataSourcePoolMetadataProvider.getDataSourcePoolMetadata(dataSource);
assertInstanceOf(DruidDataSourcePoolMetadata.class, dataSourcePoolMetadata);
```

### Configure several filters

```properties
microsphere.alibaba.druid.enabled=true
microsphere.alibaba.druid.filter.classes=\
io.microsphere.alibaba.druid.filter.LoggingStatementFilter,\
com.acme.druid.MetricsStatementFilter
```

Every listed type is searched in the `BeanFactory`; matching beans are ordered with
`AnnotationAwareOrderComparator` and appended to each `DruidDataSource` before `init()`.

### Actuator pool metrics

`spring-boot-starter-actuator` is an optional dependency, so add it explicitly, then expose the endpoint:

```properties
management.endpoints.web.exposure.include=health,metrics
```

`DruidDataSourcePoolMetadata` is what feeds `management.endpoints...` pool gauges for a Druid data source. Its numbers
against the default H2 test data source are verified by `DruidDataSourcePoolMetadataTest`:

```java
assertNotNull(this.metadata.getUsage());
assertEquals(valueOf(0), this.metadata.getActive());
assertEquals(valueOf(8), this.metadata.getIdle());
assertEquals(valueOf(8), this.metadata.getMax());
assertEquals(valueOf(0), this.metadata.getMin());
assertNull(this.metadata.getValidationQuery());
assertTrue(this.metadata.getDefaultAutoCommit());
```

### Switching the auto-configuration off

Setting `microsphere.alibaba.druid.enabled=false` makes `@ConditionalOnAlibabaDruidAvailable` (hence both
auto-configurations) back off. `AlibabaDruidAutoConfigurationTest` exercises exactly this, together with the
classpath gate, via the shared `AutoConfigurationTest` base class:

```java
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

| Property | Type | Default | Bound by | Effect |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true` | `AlibabaDruidProperties.enabled`, `@ConditionalOnAlibabaDruidEnabled` | Master switch of `AlibabaDruidAutoConfiguration` and `AlibabaDruidCloudAutoConfiguration` (`matchIfMissing = true`) |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter.classes` | Which filter bean types are injected into every `DruidDataSource` |

## Version Information

| Item | Value |
|---|---|
| Documentation line | `1.x` (generated from the `release-1.x` branch) |
| Latest released version | `0.1.21` (`0.1.22-SNAPSHOT` is the unpublished branch version) |
| Since (Javadoc) | `1.0.0` for `AlibabaDruidAutoConfiguration`, `AlibabaDruidProperties`, both conditions, `DruidDataSourcePoolMetadata` |
| Java baseline | `8` — inherited from the external parent chain (`microsphere-spring-cloud-parent:0.1.26` → `microsphere-build:0.3.14`); **not declared** in this repository |
| CI JDK matrix | 8 / 11 / 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`, managed by `microsphere-alibaba-druid-parent` |
| Auto-configuration registration | `spring.factories` + `AutoConfiguration.imports` (see above) |

## Compatibility Range

| Dimension | Supported on the `1.x` line |
|---|---|
| Alibaba Druid | `1.2.28` (as managed by the parent POM) |
| Java | 8 and newer |
| Spring Boot | `2.3.12.RELEASE`, `2.4.13`, `2.6.15` (profiles `spring-cloud-hoxton` / `spring-cloud-2020` / `spring-cloud-2021`) |
| Spring Cloud | `Hoxton.SR12` – `2021.0.9` |
| Spring Boot Actuator | optional — required only for `DruidDataSourcePoolMetadata` metrics |
| Alibaba Druid on the classpath | required for the `@ConditionalOnClass` gate to pass |

All Boot/Cloud versions above are inherited from `microsphere-spring-cloud-parent:0.1.26` and are not declared in this
repository.

## Differences from the `current` line

Same Java API; on this line the auto-configuration is *additionally* registered through `META-INF/spring.factories`
so it is discovered by Spring Boot 2.3–2.6, the artifact targets JDK 8, and the verified matrix is the Boot 2 one
(`Hoxton` – `2021.0.x`) instead of Boot 3 (`2022.0.x` – `2025.0.x`).

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-cloud.md](microsphere-alibaba-druid-spring-cloud.md)
- [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)
- [Project README](../../../README.md)
- [current-line version of this document](../../current/user-guide/microsphere-alibaba-druid-spring-boot.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-spring-boot.md](../developer-guide/microsphere-alibaba-druid-spring-boot.md)

## 中文

- [简体中文：microsphere-alibaba-druid-spring-boot](zh/microsphere-alibaba-druid-spring-boot.md)
