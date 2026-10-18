# microsphere-alibaba-druid-dependencies

[中文](zh/microsphere-alibaba-druid-dependencies.md)

## Purpose

`microsphere-alibaba-druid-dependencies` is the **Bill of Materials (BOM)** of Microsphere Alibaba Druid: a
`<packaging>pom</packaging>` artifact whose only content is a `<dependencyManagement>` section that pins the versions of
the six published modules of this repository. Import it once in your `<dependencyManagement>` and every
`microsphere-alibaba-druid-*` dependency below it can be written without a `<version>`.

## Maven Coordinates

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-dependencies</artifactId>
    <version>0.2.21</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

| Item | Value |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-dependencies` |
| Version (`current` line) | `0.2.21` released; `0.2.22-SNAPSHOT` on `main` |
| Packaging | `pom` |
| Name in the reactor | `Microsphere :: Alibaba Druid :: Dependencies` |
| Parent | `microsphere-alibaba-druid-parent` (see [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)) |
| Runtime classes | none — metadata only |

## BOM Members

The complete list managed by this POM (all with `groupId` `io.github.microsphere-projects` and the project version):

| Artifact | What you get from it | Doc |
|---|---|---|
| `microsphere-alibaba-druid-core` | `AbstractStatementFilter`, `LoggingStatementFilter`, `PropertyConstants` | [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md) |
| `microsphere-alibaba-druid-test` | `AbstractAlibabaDruidTest`, `AlibabaDruidTestUtils`, `@AlibabaDruidTest`, `@AlibabaDruidRuntime` | [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md) |
| `microsphere-alibaba-druid-spring` | `@EnableAlibabaDruid`, `AlibabaDruidRegistrar`, `DruidDataSourceBeanPostProcessor` | [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md) |
| `microsphere-alibaba-druid-spring-test` | `AbstractDruidSpringTest`, `DruidDataSourceTestConfiguration` | [microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md) |
| `microsphere-alibaba-druid-spring-boot` | `AlibabaDruidAutoConfiguration`, `AlibabaDruidProperties`, conditions, `DruidDataSourcePoolMetadata` | [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md) |
| `microsphere-alibaba-druid-spring-cloud` | `AlibabaDruidCloudAutoConfiguration`, `features.yaml` | [microsphere-alibaba-druid-spring-cloud.md](microsphere-alibaba-druid-spring-cloud.md) |

Six entries in total — the POM itself declares nothing else.

> What the BOM does **not** list: `microsphere-alibaba-druid-parent` and `microsphere-alibaba-druid-dependencies` are
> POM artifacts, not managed dependencies, and third-party libraries are not pinned here either. `com.alibaba:druid`
> (`1.2.28`) and the Spring Boot / Spring Cloud versions come from
> [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md), which this BOM inherits from.

## Usage

### Import the BOM (Maven)

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

Then declare the modules without versions. Remember that Druid itself is an **optional** dependency of every module, so
it must be declared by you:

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

If Maven cannot resolve the parent chain behind the BOM, `com.alibaba:druid` arrives without a version — pin it
yourself, e.g. `<version>1.2.28</version>` (verify with `mvn dependency:tree`).

### Import the BOM (Gradle)

```kotlin
implementation(platform("io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.2.21"))
```

### Testing-only modules

`microsphere-alibaba-druid-test` and `microsphere-alibaba-druid-spring-test` are managed like any other artifact; use
`test` scope in your POM:

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-spring-test</artifactId>
    <scope>test</scope>
</dependency>
```

### Choosing the right line

| Documentation line | Latest version | Target |
|---|---|---|
| `current` (this guide) | `0.2.21` | JDK 17+, Spring Boot `3.0.x` – `3.5.x`, Spring Cloud `2022.0.x` – `2025.0.x` |
| `1.x` | `0.1.21` | JDK 8, Spring Boot `2.x`, Spring Cloud `Hoxton` – `2021.0.x` — see [1.x User Guide](../../1.x/user-guide/README.md) |

Do not mix lines: a `0.2.x` BOM with a `0.1.x` module (or the reverse) produces inconsistent class files and mismatched
Spring APIs.

## Configuration Properties

A BOM has no runtime behaviour and binds no properties. The properties of the artifacts it pins are:

| Property | Type | Default | Defined in |
|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true` (`matchIfMissing = true`) | `PropertyConstants` (core), bound by `AlibabaDruidProperties` (spring-boot) |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter` (spring-boot) |

## Version Information

| Item | Value |
|---|---|
| Documentation line | `current` (release branch) |
| Latest released version | `0.2.21` (`0.2.22-SNAPSHOT` is the unpublished development version on `main`) |
| Managed artifacts | the six modules listed above |
| Version expression in the source POM | `${revision}`, defined by the repository root POM |
| Java | 17+ (`<java.version>17</java.version>` in the root `pom.xml`) |
| CI JDK matrix | 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`, managed by `microsphere-alibaba-druid-parent` |

## Compatibility Range

| Dimension | Supported on the `current` line |
|---|---|
| Managed module versions | all six at `0.2.21` |
| Java | 17, 21, 25 (JDK 17+ required) |
| Alibaba Druid | `1.2.28` via the inherited parent POM |
| Spring Boot / Spring Cloud | Boot `3.0.x` – `3.5.x` with Spring Cloud `2022.0.x` – `2025.0.x` (`3.0.13`/`2022.0.5`, `3.3.13`/`2023.0.6`, `3.4.13`/`2024.0.3`, `3.5.15`/`2025.0.3`); these versions are inherited from the external parent POM chain `microsphere-spring-cloud-parent:0.2.26` and are not declared in this repository |
| Maven requirement | `import` scope for `<dependencyManagement>` (Maven 2.0.9+); Gradle needs `platform(...)` (java-platform support) |

JDK 8 / Boot 2.x users: import the `0.1.x` BOM from the [1.x User Guide](../../1.x/user-guide/README.md) instead.

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-spring-cloud.md](microsphere-alibaba-druid-spring-cloud.md)
- [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)
- [microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md)
- [Project README](../../../README.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-dependencies.md](../developer-guide/microsphere-alibaba-druid-dependencies.md)

## 中文

- [简体中文：microsphere-alibaba-druid-dependencies](zh/microsphere-alibaba-druid-dependencies.md)
