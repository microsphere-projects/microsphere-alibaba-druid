# microsphere-alibaba-druid-spring-cloud

[中文](zh/microsphere-alibaba-druid-spring-cloud.md)

## Purpose

`microsphere-alibaba-druid-spring-cloud` is the thin Spring Cloud layer on top of
`microsphere-alibaba-druid-spring-boot`. It provides:

- `AlibabaDruidCloudAutoConfiguration` — a Spring Cloud auto-configuration, gated by the same
  `@ConditionalOnAlibabaDruidAvailable` condition as the Boot one.
- `META-INF/config/default/features.yaml` — the *abstract feature* declarations that make Microsphere Alibaba Druid
  visible on the Spring Cloud `/actuator/features` endpoint.

Declare a `DruidDataSource` and a `Filter` bean, and `/actuator/features` reports Druid as an active feature of your
application — no code required.

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
| Version | managed by the `microsphere-alibaba-druid-dependencies` BOM (`0.2.21` on the `current` line) |
| Packaging | `jar` |
| Compile dependency | `microsphere-alibaba-druid-spring-boot` (non-optional, hence `spring` and `core` as well) |
| Optional dependencies | `microsphere-annotation-processor`, `microsphere-spring-cloud-commons`, `com.alibaba:druid`, `spring-cloud-commons`, `spring-boot-starter`, `spring-boot-starter-actuator`, `spring-boot-configuration-processor`, `spring-boot-autoconfigure-processor` |

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

`spring-cloud-commons` (which contains `FeaturesEndpoint`, `HasFeatures`, `NamedFeature`) and the Actuator starter are
**optional**, so a Spring Cloud application brings them in itself — normally via
`spring-cloud-starter-bootstrap` / its Actuator and `spring-cloud-commons` dependencies.

## API Reference

### `io.microsphere.alibaba.druid.spring.cloud.autoconfigure.AlibabaDruidCloudAutoConfiguration`

`@ConditionalOnAlibabaDruidAvailable public class AlibabaDruidCloudAutoConfiguration` (since `1.0.0`).

| Aspect | Detail |
|---|---|
| Registration | `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` contains `io.microsphere.alibaba.druid.spring.cloud.autoconfigure.AlibabaDruidCloudAutoConfiguration` |
| Conditions | `microsphere.alibaba.druid.enabled` is `true` or absent (`matchIfMissing = true`) **and** `com.alibaba.druid.pool.DruidDataSource` is on the classpath |
| Declared beans | none — the class is a marker auto-configuration; the `DruidDataSourceBeanPostProcessor` and `DataSourcePoolMetadataProvider` come from `AlibabaDruidAutoConfiguration` in the Boot module |
| Effect | Activates the module for Spring Cloud feature reporting (`features.yaml` below) and exposes `AlibabaDruidProperties` through the inherited Boot configuration |

### `META-INF/config/default/features.yaml`

The file shipped in `src/main/resources`:

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

| Key | Value | Meaning |
|---|---|---|
| `microsphere.spring.cloud.features.abstract.microsphere-alibaba-druid-core` | `com.alibaba.druid.pool.DruidDataSource` | An *abstract feature* entry: when a `DruidDataSource` bean exists, `/actuator/features` reports `microsphere-alibaba-druid-core:DruidDataSource` |
| same list | `com.alibaba.druid.filter.Filter` | The `Filter` entry is abstract: it is resolved to the concrete registered filter type, e.g. `microsphere-alibaba-druid-core:LoggingStatementFilter` |

> The list key is `microsphere-alibaba-druid-core` even though the file lives in the `spring-cloud` module — the feature
> name is a logical group label, not the artifact that ships the file.

## Usage

### Properties

```properties
microsphere.alibaba.druid.enabled=true
spring.cloud.features.enabled=true
management.endpoints.web.exposure.include=features
microsphere.alibaba.druid.filter.classes=io.microsphere.alibaba.druid.filter.LoggingStatementFilter
```

### Verifying the reported features

`AlibabaDruidCloudAutoConfigurationIntegrationTest` (test sources) shows what the module produces. It injects all
`HasFeatures` beans plus the `FeaturesEndpoint`:

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

    @Autowired
    private Map<String, HasFeatures> hasFeaturesMap;

    @Autowired
    private FeaturesEndpoint featuresEndpoint;

    @Test
    public void test() {
        assertTrue(this.hasFeaturesMap.size() > 0);
        HasFeatures hadFeatures = this.hasFeaturesMap.get("microsphere-alibaba-druid-core.features");
        assertNotNull(hadFeatures);

        List<NamedFeature> namedFeatures = hadFeatures.getNamedFeatures();
        assertEquals(2, namedFeatures.size());

        assertNamedFeature(namedFeatures, 0, "microsphere-alibaba-druid-core:DruidDataSource", DruidDataSource.class);
        assertNamedFeature(namedFeatures, 1, "microsphere-alibaba-druid-core:LoggingStatementFilter", LoggingStatementFilter.class);

        assertNotNull(featuresEndpoint.features());
    }

    private void assertNamedFeature(List<NamedFeature> namedFeatures, int index, String name, Class<?> type) {
        NamedFeature namedFeature = namedFeatures.get(index);
        assertEquals(name, namedFeature.getName());
        assertEquals(type, namedFeature.getType());
    }
}
```

Key facts you can rely on:

- The `HasFeatures` bean is named `microsphere-alibaba-druid-core.features`.
- It contributes exactly the two declared features, in declaration order: `...:DruidDataSource` then the resolved
  concrete filter `...:LoggingStatementFilter`.
- `featuresEndpoint.features()` must be called for the endpoint payload; the actuator exposure is configured with
  `management.endpoints.web.exposure.include=features`, and the endpoint itself is enabled by
  `spring.cloud.features.enabled=true`.

### Unit-testing the auto-configuration

`AlibabaDruidCloudAutoConfigurationTest` (test sources) extends `AutoConfigurationTest` from
`microsphere-spring-boot-test`:

```java
@SpringBootTest(
        classes = {
                AlibabaDruidCloudAutoConfigurationTest.class
        },
        webEnvironment = NONE
)
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

`configureAutoConfiguredClasses` stays empty on purpose: this auto-configuration registers no beans of its own, while
the disabled property and the missing `DruidDataSource` class are the two ways it backs off.

## Configuration Properties

This module declares no new properties; it reuses the Boot ones and the Spring Cloud feature switch:

| Property | Type | Default | Defined by | Effect |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true` (`matchIfMissing = true`) | `PropertyConstants` (core), bound by `AlibabaDruidProperties` | Switches `AlibabaDruidCloudAutoConfiguration` on/off |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter` (spring-boot) | Determines which filter beans exist, hence which concrete `Filter` feature is reported |
| `spring.cloud.features.enabled` | `boolean` | as defined by Spring Cloud `spring-cloud-commons` | Spring Cloud, not this project | Enables `FeaturesEndpoint` |
| `management.endpoints.web.exposure.include` | `String` (comma-separated) | — | Spring Boot Actuator | Must contain `features` to expose `/actuator/features` |

## Version Information

| Item | Value |
|---|---|
| Documentation line | `current` (release branch) |
| Latest released version | `0.2.21` (`0.2.22-SNAPSHOT` is the unpublished development version on `main`) |
| Since (Javadoc) | `1.0.0` for `AlibabaDruidCloudAutoConfiguration` |
| Java | 17+ (`<java.version>17</java.version>` in the root `pom.xml`) |
| CI JDK matrix | 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`, managed by `microsphere-alibaba-druid-parent` |

## Compatibility Range

| Dimension | Supported on the `current` line |
|---|---|
| Alibaba Druid | `1.2.28` (as managed by the parent POM) |
| Java | 17, 21, 25 (JDK 17+ required) |
| Spring Cloud | `2022.0.x` – `2025.0.x` — verified against `2022.0.5`, `2023.0.6`, `2024.0.3`, `2025.0.3` (build profiles `spring-cloud-2022` … `spring-cloud-2025`); these versions are inherited from the external parent POM chain `microsphere-spring-cloud-parent:0.2.26` and are not declared in this repository |
| Spring Boot | `3.0.x` – `3.5.x` (`3.0.13`, `3.3.13`, `3.4.13`, `3.5.15`) |
| Feature reporting | Requires `spring-cloud-commons` (`FeaturesEndpoint` / `HasFeatures`) and the Actuator on the classpath |

Spring Cloud `Hoxton` – `2021.0.x` / JDK 8 users: see the [1.x User Guide](../../1.x/user-guide/README.md).

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
- [Project README](../../../README.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-spring-cloud.md](../developer-guide/microsphere-alibaba-druid-spring-cloud.md)

## 中文

- [简体中文：microsphere-alibaba-druid-spring-cloud](zh/microsphere-alibaba-druid-spring-cloud.md)
