# microsphere-alibaba-druid-parent

[中文](zh/microsphere-alibaba-druid-parent.md)

## Purpose

`microsphere-alibaba-druid-parent` is the **parent POM** (`<packaging>pom</packaging>`) of every module in this
repository. It is the single place where the third-party versions this project is built against are declared:

- `com.alibaba:druid` → `1.2.28` (property `druid.version`).
- The Microsphere Spring Cloud BOM `microsphere-spring-cloud-dependencies` → `0.2.26` (property
  `microsphere-spring-cloud.version`), imported with `<scope>import</scope>`, which supplies the versions of
  `microsphere-*` artifacts, Spring Framework, Spring Boot, Spring Cloud, JUnit, H2, Logback, and so on.

Most applications do **not** use this POM — they import [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
instead. Use the parent when you build your own Microsphere-flavored library/module and want exactly the same dependency
management and build setup as this project.

## Maven Coordinates

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-parent</artifactId>
    <version>0.2.21</version>
</parent>
```

| Item | Value |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-parent` |
| Version (`current` line) | `0.2.21` released; `0.2.22-SNAPSHOT` on `main` |
| Packaging | `pom` |
| Name in the reactor | `Microsphere :: Alibaba Druid :: Parent` |
| Inherited by | `microsphere-alibaba-druid-core`, `-test`, `-spring`, `-spring-test`, `-spring-boot`, `-spring-cloud`, `-dependencies` |

## POM Reference

### Parent chain

| Level | POM | Declared in |
|---|---|---|
| 1 | `microsphere-alibaba-druid-parent` | your module |
| 2 | `microsphere-alibaba-druid` (repository root aggregator, `<java.version>17</java.version>`, `<revision>0.2.22-SNAPSHOT</revision>`) | `../pom.xml` |
| 3 | `io.github.microsphere-projects:microsphere-spring-cloud-parent:0.2.26` | the root POM's `<parent>` — **external artifact, not in this repository** |

The Spring Boot / Spring Cloud version profiles (`spring-cloud-2022` … `spring-cloud-2025`) come from level 3; this
repository never states them.

### Properties declared by this POM

| Property | Value | Used for |
|---|---|---|
| `microsphere-spring-cloud.version` | `0.2.26` | Version of the imported `microsphere-spring-cloud-dependencies` BOM |
| `druid.version` | `1.2.28` | Version of `com.alibaba:druid` in `dependencyManagement` |

Both are plain Maven properties: redefine them in your own `<properties>` block if you inherit this POM and need a
different Druid release.

### Managed dependencies

| Entry | Coordinates | Kind |
|---|---|---|
| Alibaba Druid | `com.alibaba:druid:${druid.version}` (= `1.2.28`) | version management |
| Microsphere Spring Cloud BOM | `io.github.microsphere-projects:microsphere-spring-cloud-dependencies:${microsphere-spring-cloud.version}` (= `0.2.26`), `<type>pom</type> <scope>import</scope>` | BOM import |

This POM does **not** manage the versions of this repository's own modules — that is the job of
`microsphere-alibaba-druid-dependencies`.

### Modules in the reactor (root POM)

| Module | Role |
|---|---|
| `microsphere-alibaba-druid-parent` | this POM |
| `microsphere-alibaba-druid-dependencies` | the BOM |
| `microsphere-alibaba-druid-core` | `AbstractStatementFilter`, `LoggingStatementFilter`, `PropertyConstants` |
| `microsphere-alibaba-druid-test` | non-Spring test utilities |
| `microsphere-alibaba-druid-spring` | `@EnableAlibabaDruid`, `DruidDataSourceBeanPostProcessor` |
| `microsphere-alibaba-druid-spring-test` | Spring test support |
| `microsphere-alibaba-druid-spring-boot` | auto-configuration, properties, pool metadata |
| `microsphere-alibaba-druid-spring-cloud` | Spring Cloud auto-configuration, `features.yaml` |

## Usage

### As the parent of your own library module

```xml
<project>
    <parent>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-druid-parent</artifactId>
        <version>0.2.21</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>my-druid-extension</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <dependencies>
        <!-- no <version>: managed by the parent chain -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-alibaba-druid-spring-boot</artifactId>
            <version>0.2.21</version>
        </dependency>
    </dependencies>
</project>
```

> The parent manages Druid and the Microsphere Spring Cloud artifacts, but **not** `microsphere-alibaba-druid-*`
> itself — that still needs an explicit version (or an import of the BOM):
>
> ```xml
> <dependencyManagement>
>     <dependencies>
>         <dependency>
>             <groupId>io.github.microsphere-projects</groupId>
>             <artifactId>microsphere-alibaba-druid-dependencies</artifactId>
>             <version>0.2.21</version>
>             <type>pom</type>
>             <scope>import</scope>
>         </dependency>
>     </dependencies>
> </dependencyManagement>
> ```

### Pin Druid to a different release while inheriting

```xml
<properties>
    <druid.version>1.2.24</druid.version>
</properties>
```

### As a version source only (recommended for applications)

Applications normally skip this POM entirely and use
[microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md) plus their own Spring Boot parent.

## Configuration Properties

This module has no runtime classes and therefore no configuration properties at all; the `microsphere.alibaba.druid.*`
properties live in other modules:

| Item | Value |
|---|---|
| Runtime classes | none |
| Configuration properties | none |
| Managed property names (for reference) | `microsphere.alibaba.druid.enabled`, `microsphere.alibaba.druid.filter.classes` — see [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md) |

## Version Information

| Item | Value |
|---|---|
| Documentation line | `current` (release branch) |
| Latest released version | `0.2.21` (`0.2.22-SNAPSHOT` is the unpublished development version on `main`) |
| Declared `druid.version` | `1.2.28` |
| Declared `microsphere-spring-cloud.version` | `0.2.26` |
| Java | 17+ (`<java.version>17</java.version>` in the root `pom.xml`) |
| CI JDK matrix | 17 / 21 / 25, each combined with the profiles `spring-cloud-2022`, `spring-cloud-2023`, `spring-cloud-2024`, `spring-cloud-2025` (`.github/workflows/maven-build.yml`) |
| Versioning style | CI-friendly `${revision}` in the sources; published artifacts carry the concrete release version |

## Compatibility Range

| Dimension | Supported on the `current` line |
|---|---|
| Alibaba Druid | managed at `1.2.28`; override with the `druid.version` property |
| Java | 17, 21, 25 (JDK 17+ required) |
| Spring Boot | `3.0.13`, `3.3.13`, `3.4.13`, `3.5.15` per build profile — i.e. Boot `3.0.x` – `3.5.x` |
| Spring Cloud | `2022.0.5`, `2023.0.6`, `2024.0.3`, `2025.0.3` per build profile — i.e. Spring Cloud `2022.0.x` – `2025.0.x` |
| Origin of those Boot/Cloud versions | the external parent POM chain `io.github.microsphere-projects:microsphere-spring-cloud-parent:0.2.26` and its `microsphere-spring-cloud-dependencies` BOM — **not declared in this repository** |

JDK 8 / Spring Cloud `Hoxton` – `2021.0.x` are served by the `1.x` line (parent POM `0.1.x`): see the
[1.x User Guide](../../1.x/user-guide/README.md).

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [Project README](../../../README.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-parent.md](../developer-guide/microsphere-alibaba-druid-parent.md)

## 中文

- [简体中文：microsphere-alibaba-druid-parent](zh/microsphere-alibaba-druid-parent.md)
