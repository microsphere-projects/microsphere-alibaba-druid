# microsphere-alibaba-druid-spring-cloud (`1.x`)

[中文](zh/microsphere-alibaba-druid-spring-cloud.md)

## Purpose

`microsphere-alibaba-druid-spring-cloud` is the top layer of the runtime stack on the `1.x` line. It adds nothing new
to SQL interception; it makes the project visible to a Spring Cloud application:

- `AlibabaDruidCloudAutoConfiguration` — the Spring Cloud entry point, gated by the same conditions as the Boot
  auto-configuration.
- `META-INF/config/default/features.yaml` — declares the Alibaba Druid classes so that Spring Cloud's
  `/actuator/features` endpoint reports them as active features.
- **1.x specific:** like the Boot module, it ships both `META-INF/spring.factories` (Boot 2.x) and
  `AutoConfiguration.imports` (Boot 2.7+), so the module works across the whole `Hoxton` – `2021.0.x` range.

## Maven Coordinates

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-spring-cloud</artifactId>
</dependency>
```

| Item | Value |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-spring-cloud` |
| Version | managed by the `microsphere-alibaba-druid-dependencies` BOM (`0.1.21` on the `1.x` line) |
| Packaging | `jar` |
| Compile dependency | `microsphere-alibaba-druid-spring-boot` (non-optional, brings in `spring` and `core`) |
| Optional dependencies | `microsphere-annotation-processor`, `microsphere-spring-cloud-commons`, `com.alibaba:druid`, `spring-cloud-commons`, `spring-boot-starter`, `spring-boot-starter-actuator`, `spring-boot-configuration-processor`, `spring-boot-autoconfigure-processor` |

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

implementation("io.github.microsphere-projects:microsphere-alibaba-druid-spring-cloud")
```

`spring-cloud-commons` is optional, so add it (or a Spring Cloud starter that brings it) plus `com.alibaba:druid`
yourself.

## API Reference

### `io.microsphere.alibaba.druid.spring.cloud.autoconfigure.AlibabaDruidCloudAutoConfiguration`

`public class AlibabaDruidCloudAutoConfiguration` (since `1.0.0`) — an **empty shell** annotated
`@ConditionalOnAlibabaDruidAvailable` (`@ConditionalOnAlibabaDruidEnabled` + `@ConditionalOnClass(name = "com.alibaba.druid.pool.DruidDataSource")`).

It declares no `@Bean` methods on purpose: the useful beans come from
[microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md), which is on the compile
classpath of this module. Its role is to be the Spring Cloud discovery/management entry point and to make the
feature metadata above part of an auto-configured context.

### Auto-configuration registration files

`src/main/resources/META-INF/spring.factories`:

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.alibaba.druid.spring.cloud.autoconfigure.AlibabaDruidCloudAutoConfiguration
```

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:

```text
io.microsphere.alibaba.druid.spring.cloud.autoconfigure.AlibabaDruidCloudAutoConfiguration
```

| Boot version | File actually read |
|---|---|
| 2.3 – 2.6 (this line's matrix) | `spring.factories` |
| 2.7 | both, de-duplicated |
| 3.x | `AutoConfiguration.imports` |

### `META-INF/config/default/features.yaml`

```yaml
microsphere:
  spring:
    cloud:
      features:
        abstract:
          microsphere-alibaba-druid-core:
            - com.alibaba.druid.pool.DruidDataSource
            - com.alibaba.druid.filter.Filter
```

`microsphere-spring-cloud-commons` reads `META-INF/config/default/features.yaml` from every jar on the classpath and
turns each `abstract` group into `HasFeatures` / `NamedFeature` entries for the Spring Cloud `FeaturesEndpoint`.
The group key (`microsphere-alibaba-druid-core`) becomes the feature-set name, and each listed class is published
only when it is actually present on the classpath.

## Usage

### Boot the feature endpoint

As exercised by `AlibabaDruidCloudAutoConfigurationIntegrationTest` (test sources):

```java
@SpringBootTest(classes = {
        LoggingStatementFilter.class,
        DruidDataSourceTestConfiguration.class,
        AlibabaDruidCloudAutoConfigurationIntegrationTest.class
}, webEnvironment = NONE,
        properties = {
                "microsphere.alibaba.druid.enabled=true",
                "management.endpoints.web.exposure.include=features",
                "spring.cloud.features.enabled=true",
                "microsphere.alibaba.druid.filter.classes=io.microsphere.alibaba.druid.filter.LoggingStatementFilter"
        })
@EnableAutoConfiguration
public class AlibabaDruidCloudAutoConfigurationIntegrationTest extends AbstractDruidSpringTest {
}
```

In your application the equivalent is:

```properties
management.endpoints.web.exposure.include=health,features
spring.cloud.features.enabled=true
```

### What `/actuator/features` then reports

Verified by the same integration test — the `HasFeatures` bean is registered under the name
`microsphere-alibaba-druid-core.features` and carries two named features:

```java
HasFeatures hadFeatures = this.hasFeaturesMap.get("microsphere-alibaba-druid-core.features");
List<NamedFeature> namedFeatures = hadFeatures.getNamedFeatures();
assertEquals(2, namedFeatures.size());

// index 0 -> name "microsphere-alibaba-druid-core:DruidDataSource", type DruidDataSource.class
// index 1 -> name "microsphere-alibaba-druid-core:LoggingStatementFilter", type LoggingStatementFilter.class
```

Naming rule: `<group>:<simple class name>`. The second entry is a *concrete* filter bean found in the context;
if you register no `Filter` bean, only the declared abstract classes that are on the classpath are reported.

### Plain Spring Cloud without filters

Nothing else is required — the module needs no `@Import`. Drop the dependency in, keep a `DruidDataSource` bean, and
the Boot-layer `DruidDataSourceBeanPostProcessor` plus the feature metadata are active.

### Verifying the auto-configuration backs off

`AlibabaDruidCloudAutoConfigurationTest` extends the shared `AutoConfigurationTest` base and asserts both gates:

```java
class AlibabaDruidCloudAutoConfigurationTest extends AutoConfigurationTest<AlibabaDruidCloudAutoConfiguration> {

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
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

This module declares no new properties; it reuses the Boot ones and Spring Cloud's endpoint switch:

| Property | Type | Default | Defined by | Effect |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true` (`matchIfMissing = true`) | `PropertyConstants` (core) / `AlibabaDruidProperties` | Gates `AlibabaDruidCloudAutoConfiguration` via `@ConditionalOnAlibabaDruidAvailable` |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter` | Filter types injected into every `DruidDataSource` (from the Boot layer) |
| `spring.cloud.features.enabled` | `boolean` | Spring Cloud default | Spring Cloud Commons | Enables `FeaturesEndpoint` so the `features.yaml` entries are published |
| `management.endpoints.web.exposure.include` | `String` (list) | — | Spring Boot Actuator | Must include `features` to expose `/actuator/features` |

## Version Information

| Item | Value |
|---|---|
| Documentation line | `1.x` (generated from the `release-1.x` branch) |
| Latest released version | `0.1.21` (`0.1.22-SNAPSHOT` is the unpublished branch version) |
| Since (Javadoc) | `1.0.0` for `AlibabaDruidCloudAutoConfiguration` |
| Java baseline | `8` — inherited from the external parent chain (`microsphere-spring-cloud-parent:0.1.26` → `microsphere-build:0.3.14`); **not declared** in this repository |
| CI JDK matrix | 8 / 11 / 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`, managed by `microsphere-alibaba-druid-parent` |
| Auto-configuration registration | `spring.factories` + `AutoConfiguration.imports` |

## Compatibility Range

| Dimension | Supported on the `1.x` line |
|---|---|
| Alibaba Druid | `1.2.28` (as managed by the parent POM) |
| Java | 8 and newer |
| Spring Cloud | `Hoxton.SR12`, `2020.0.5`, `2021.0.9` (profiles `spring-cloud-hoxton` / `spring-cloud-2020` / `spring-cloud-2021`) |
| Spring Boot | `2.3.12.RELEASE`, `2.4.13`, `2.6.15` |
| Spring Cloud Commons | required for `FeaturesEndpoint` / `HasFeatures`; it is an optional dependency, so declare it if you use the features endpoint |
| Microsphere Spring Cloud | `microsphere-spring-cloud-commons:0.1.26` (from the imported BOM) — required for the `features.yaml` mechanism |

## Differences from the `current` line

Same Java API and same `features.yaml`; on this line the auto-configuration is also registered through
`META-INF/spring.factories` for Boot 2.x discovery, the artifact targets JDK 8, and the Spring Cloud matrix is
`Hoxton` – `2021.0.x` instead of `2022.0.x` – `2025.0.x`.

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
- [Project README](../../../README.md)
- [current-line version of this document](../../current/user-guide/microsphere-alibaba-druid-spring-cloud.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-spring-cloud.md](../developer-guide/microsphere-alibaba-druid-spring-cloud.md)

## 中文

- [简体中文：microsphere-alibaba-druid-spring-cloud](zh/microsphere-alibaba-druid-spring-cloud.md)
