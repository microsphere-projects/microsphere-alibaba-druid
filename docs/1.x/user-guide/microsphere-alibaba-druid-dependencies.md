# microsphere-alibaba-druid-dependencies (`1.x`)

[中文](zh/microsphere-alibaba-druid-dependencies.md)

## Purpose

`microsphere-alibaba-druid-dependencies` is the **Bill of Materials (BOM)** of Microsphere Alibaba Druid on the `1.x`
line. It is a `pom` artifact whose only content is a `<dependencyManagement>` section pinning the versions of the six
publishable modules of this repository, so an application can declare them without versions.

## Maven Coordinates

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-dependencies</artifactId>
    <version>0.1.21</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

| Item | Value |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-dependencies` |
| Version | `${revision}` — `0.1.21` released, `0.1.22-SNAPSHOT` on the `release-1.x` branch |
| Packaging | `pom` |
| Its own parent | `io.github.microsphere-projects:microsphere-alibaba-druid-parent` |
| Java code | none — metadata only |

## What the BOM Pins

Exactly these six artifacts, all at `${revision}` (i.e. `0.1.21`):

| artifactId | Kind | Typical scope in your build |
|---|---|---|
| `microsphere-alibaba-druid-core` | runtime | `compile` |
| `microsphere-alibaba-druid-spring` | runtime (`compile` on `core`) | `compile` |
| `microsphere-alibaba-druid-spring-boot` | runtime (`compile` on `spring`) | `compile` |
| `microsphere-alibaba-druid-spring-cloud` | runtime (`compile` on `spring-boot`) | `compile` |
| `microsphere-alibaba-druid-test` | test support | `test` |
| `microsphere-alibaba-druid-spring-test` | test support (`compile` on `test`) | `test` |

What it does **not** list: `microsphere-alibaba-druid-parent` and the root aggregator pom (they are build-time
parents, not dependencies) — see [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md).

## What the BOM Does Not Manage

The BOM declares nothing about third-party libraries. The versions of these come from elsewhere and must be provided
by you (or by the parent POM if you inherit from it):

| Artifact | Managed by | Value on the `1.x` line |
|---|---|---|
| `com.alibaba:druid` | `microsphere-alibaba-druid-parent` → `${druid.version}` | `1.2.28` |
| Spring Boot / Spring Cloud / Spring Framework | `microsphere-spring-cloud-dependencies:0.1.26`, imported by the parent | Boot `2.3.12.RELEASE` – `2.6.15`, Cloud `Hoxton.SR12` – `2021.0.9` depending on the active profile |
| Microsphere libraries (`microsphere-java-core`, `microsphere-spring-context`, …) | same Microsphere BOM | `0.1.26` line |
| `com.h2database:h2`, `logback-classic`, JUnit Jupiter | the Boot/Cloud line in effect | varies per profile |

> Note: because this BOM *inherits* from `microsphere-alibaba-druid-parent`, its effective dependency management also
> carries the `com.alibaba:druid` entry and the imported Microsphere BOM. That behavior has been part of Maven's
> `import` semantics for years, but if your build depends on a version-less `com.alibaba:druid`, the robust choices
> are either to declare `<druid.version>` (or an explicit version) yourself, or to use
> `microsphere-alibaba-druid-parent` as your `<parent>`.

## Usage

### Maven

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

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-druid-spring-cloud</artifactId>
    </dependency>

    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-druid-test</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid</artifactId>
    </dependency>

    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation(platform("io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.1.21"))
    implementation("io.github.microsphere-projects:microsphere-alibaba-druid-spring-cloud")

    testImplementation(platform("io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.1.21"))
    testImplementation("io.github.microsphere-projects:microsphere-alibaba-druid-test")
}
```

Groovy DSL:

```groovy
dependencies {
    implementation platform('io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.1.21')
    implementation 'io.github.microsphere-projects:microsphere-alibaba-druid-spring-cloud'
}
```

### Mixing with Spring Boot's own BOM

Import order matters for a version clash: the **first** imported BOM wins in Maven `dependencyManagement`. To keep
Druid at `1.2.28` while a Boot BOM also manages it, list the Microsphere BOM first — or align it explicitly:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-alibaba-druid-dependencies</artifactId>
            <version>0.1.21</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>2.6.15</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## Selecting the Right Line

`0.1.x` (this BOM) targets Spring Cloud `Hoxton` – `2021.0.x` / JDK 8. If your application uses Spring Boot 3 / Spring
Cloud `2022.0.x` – `2025.0.x`, import `0.2.21` from the `current` line instead — the artifact names are identical, so
it is a one-property change.

## Configuration Properties

None: this artifact contributes no runtime configuration.

| Property | Applies to the BOM? |
|---|---|
| `microsphere.alibaba.druid.enabled` | No — bound in [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md) |
| `microsphere.alibaba.druid.filter.classes` | No — bound in [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md) |

## Version Information

| Item | Value |
|---|---|
| Documentation line | `1.x` (generated from the `release-1.x` branch) |
| Latest released version | `0.1.21` (`0.1.22-SNAPSHOT` is the unpublished branch version) |
| Artifacts pinned | the 6 modules listed above, all at `${revision}` |
| Java baseline implied for consumers | `8` (the jars are compiled with source/target `8`, inherited from `microsphere-build:0.3.14` via `microsphere-spring-cloud:0.1.26`) |
| Alibaba Druid the modules expect | `1.2.28` |
| Maven Central badge scope | the root README publishes `io.github.microsphere-projects/microsphere-alibaba-druid-dependencies` as the release marker |

## Compatibility Range

| Dimension | Value on the `1.x` line |
|---|---|
| Maven | 3.6+ (`import` scope + `${revision}` resolution) |
| Gradle | 6.8+ for `platform(...)` with a POM (Gradle 7+ recommended) |
| Java | 8 and newer |
| Spring Boot | `2.3.12.RELEASE`, `2.4.13`, `2.6.15` |
| Spring Cloud | `Hoxton.SR12`, `2020.0.5`, `2021.0.9` |
| Alibaba Druid | `1.2.28` |

## Differences from the `current` line

Identical structure and identical artifact list; the difference is only the version line it pins — `0.1.21`
(`Hoxton` – `2021.0.x`, JDK 8) here versus `0.2.21` (Spring Cloud `2022.0.x` – `2025.0.x`, JDK 17) on `current`.

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring-cloud.md](microsphere-alibaba-druid-spring-cloud.md)
- [Project README](../../../README.md)
- [current-line version of this document](../../current/user-guide/microsphere-alibaba-druid-dependencies.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-dependencies.md](../developer-guide/microsphere-alibaba-druid-dependencies.md)

## 中文

- [简体中文：microsphere-alibaba-druid-dependencies](zh/microsphere-alibaba-druid-dependencies.md)
