# Microsphere Alibaba Druid — User Guide

[中文](zh/README.md)

This guide is written for **users** of Microsphere Alibaba Druid on the `current` documentation line (the `release`
branch), i.e. the `0.2.x` artifacts. It tells you what to put in your `pom.xml`, which classes and annotations to use,
and which properties to set. Design rationale and internal machinery are covered by the
[Developer Guide](../developer-guide/README.md).

Microsphere Alibaba Druid builds on [Alibaba Druid](https://github.com/alibaba/druid), a high-performance JDBC
connection pool, and adds SQL **statement interception** support (`AbstractStatementFilter`), Spring / Spring Boot /
Spring Cloud integration, connection-pool metrics for the Actuator, and a set of testing utilities.

## Documents in this guide

| Document | Module | What it gives you |
|---|---|---|
| [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md) | `microsphere-alibaba-druid-core` | `AbstractStatementFilter` / `LoggingStatementFilter` and the `microsphere.alibaba.druid` property-name constants |
| [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md) | `microsphere-alibaba-druid-spring` | `@EnableAlibabaDruid` and `DruidDataSourceBeanPostProcessor`: inject `Filter` beans into every `DruidDataSource` |
| [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md) | `microsphere-alibaba-druid-spring-boot` | Auto-configuration, `AlibabaDruidProperties` and `DruidDataSourcePoolMetadata` (Actuator pool metrics) |
| [microsphere-alibaba-druid-spring-cloud.md](microsphere-alibaba-druid-spring-cloud.md) | `microsphere-alibaba-druid-spring-cloud` | Spring Cloud auto-configuration and the `/actuator/features` entries declared in `features.yaml` |
| [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md) | `microsphere-alibaba-druid-test` | `AbstractAlibabaDruidTest`, `AlibabaDruidTestUtils`, `@AlibabaDruidTest`, `@AlibabaDruidRuntime` |
| [microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md) | `microsphere-alibaba-druid-spring-test` | `AbstractDruidSpringTest` and `DruidDataSourceTestConfiguration` for Spring-based tests |
| [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md) | `microsphere-alibaba-druid-parent` | Parent POM: Druid `1.2.28` version management plus the Microsphere Spring Cloud BOM import |
| [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md) | `microsphere-alibaba-druid-dependencies` | Bill of Materials (BOM): the list of artifacts whose versions it pins |

## Quick Start

### 1. Import the BOM

`microsphere-alibaba-druid-dependencies` is the Bill of Materials of this project. Importing it means you never write a
version for a Microsphere Alibaba Druid artifact again.

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

With Gradle:

```kotlin
implementation(platform("io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.2.21"))
```

> The BOM pins only the modules of this repository. Druid itself (`com.alibaba:druid`) and the Spring Boot / Spring
> Cloud versions come from `microsphere-alibaba-druid-parent`, so declare `com.alibaba:druid` and the H2/MySQL driver
> you use as normal dependencies in your application. See
> [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md) and
> [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md).

### 2. Add one module

A Spring Boot application normally needs only the Spring Boot module, which transitively brings in `core`, `spring`
and (for Spring Cloud users) nothing else:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-druid-spring-boot</artifactId>
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

The modules declare Druid / Spring / Spring Boot as **optional** dependencies, so they are *not* transitively brought
in — you add them yourself.

### 3. Minimal Spring Boot application

```java
@SpringBootApplication
public class DruidDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DruidDemoApplication.class, args);
    }
}
```

`src/main/resources/application.properties`:

```properties
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.url=jdbc:h2:mem:test_mem
spring.datasource.driver-class-name=org.h2.Driver

microsphere.alibaba.druid.enabled=true
microsphere.alibaba.druid.filter.classes=io.microsphere.alibaba.druid.filter.LoggingStatementFilter

management.endpoints.web.exposure.include=health,features
```

That is enough: `AlibabaDruidAutoConfiguration` registers a `DruidDataSourceBeanPostProcessor` that injects every
matching `Filter` bean into each `DruidDataSource` bean before it is initialized, and a
`DataSourcePoolMetadataProvider` that exposes the Druid pool to Spring Boot Actuator metrics.

### 4. Add your own SQL interception logic

Extend `AbstractStatementFilter` (see
[microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)), declare it as a Spring bean, and list it in
`microsphere.alibaba.druid.filter.classes` — or use `@EnableAlibabaDruid` in a plain Spring application (see
[microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)).

## Configuration Properties Summary

| Property | Type | Default | Defined by | Consumed by |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true` | `PropertyConstants` (core) | `@ConditionalOnAlibabaDruidEnabled`, `AlibabaDruidProperties` |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter` (spring-boot) | `AlibabaDruidAutoConfiguration` |

## Module Structure

```mermaid
graph TD
    parent["microsphere-alibaba-druid-parent"] --> core["microsphere-alibaba-druid-core"]
    parent --> test["microsphere-alibaba-druid-test"]
    parent --> dependencies["microsphere-alibaba-druid-dependencies (BOM)"]
    core --> spring["microsphere-alibaba-druid-spring"]
    spring --> boot["microsphere-alibaba-druid-spring-boot"]
    boot --> cloud["microsphere-alibaba-druid-spring-cloud"]
    test --> springtest["microsphere-alibaba-druid-spring-test"]
```

Compile-scope chain of the runtime modules: `core` → `spring` → `spring-boot` → `spring-cloud`. The testing modules are
`test` → `spring-test`.

## Version Information

| Item | Value |
|---|---|
| Documentation line | `current` (generated from the `release` branch) |
| Latest released version | `0.2.21` |
| `main` working tree version | `0.2.22-SNAPSHOT` (development, not published) |
| Java | `17` or above (`<java.version>17</java.version>` in the root `pom.xml`) |
| CI JDK matrix | `17`, `21`, `25` (`.github/workflows/maven-build.yml`) |
| Alibaba Druid | `1.2.28`, managed by `microsphere-alibaba-druid-parent` |
| groupId of every artifact | `io.github.microsphere-projects` |
| Auto-configuration registration | Boot 3 style `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |

## Compatibility Range

The `current` line is compatible with **Spring Cloud `2022.0.x` – `2025.0.x`** (and therefore Spring Boot `3.0.x` –
`3.5.x`). The build is verified against four profiles:

| Profile | Spring Boot | Spring Cloud |
|---|---|---|
| `spring-cloud-2022` | `3.0.13` | `2022.0.5` |
| `spring-cloud-2023` | `3.3.13` | `2023.0.6` |
| `spring-cloud-2024` | `3.4.13` | `2024.0.3` |
| `spring-cloud-2025` | `3.5.15` | `2025.0.3` |

> These concrete Boot/Cloud versions are **inherited from the external parent POM chain**
> `io.github.microsphere-projects:microsphere-spring-cloud-parent:0.2.26`; they are not declared in this repository.

Using JDK 8 or Spring Cloud `Hoxton` – `2021.0.x`? Use the `1.x` line instead: [1.x User Guide](../../1.x/user-guide/README.md).

## Related Documents

- [Project README (repository root)](../../../README.md)
- [Developer Guide](../developer-guide/README.md) — architecture and internals of the same modules

## 中文

- [简体中文版用户文档](zh/README.md)
