# Microsphere [Alibaba Druid](https://github.com/alibaba/druid)

> Microsphere Projects for [Alibaba Druid](https://github.com/alibaba/druid)

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-alibaba-druid)

[![Maven Build](https://github.com/microsphere-projects/microsphere-alibaba-druid/actions/workflows/maven-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-alibaba-druid/actions/workflows/maven-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-alibaba-druid/branch/dev-1.x/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-alibaba-druid)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-alibaba-druid-dependencies.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-alibaba-druid.svg)

Microsphere Alibaba Druid is a specialized integration project that
enhances [Alibaba Druid](https://github.com/alibaba/druid), a high-performance JDBC connection pool, with powerful SQL
statement interception capabilities. It enables developers to monitor, log, and control SQL execution at a granular
level across different Spring ecosystems, which supports the extension features that would be used
for [Microsphere Sentinel](https://github.com/microsphere-projects/microsphere-sentinel),[Microsphere Resilience4j](https://github.com/microsphere-projects/microsphere-resilience4j), [Microsphere Observability](https://github.com/microsphere-projects/microsphere-observability)
and so on.

## Purpose and Scope

The project extends Druid's capabilities with Spring Framework integration, auto-configuration, and enhanced monitoring
features that integrate with the broader Microsphere ecosystem:

- Interception on executing the SQL Statements
- Logging the SQL Statements

## Modules

| **Module**                                 | **Purpose**                                             |
|--------------------------------------------|---------------------------------------------------------|
| **microsphere-alibaba-druid-core**         | Foundation classes and filters for Druid integration    |
| **microsphere-alibaba-druid-spring**       | Spring Framework integration and configuration          |
| **microsphere-alibaba-druid-spring-boot**  | Spring Boot auto-configuration and properties           |
| **microsphere-alibaba-druid-spring-cloud** | Spring Cloud features and service discovery integration |
| **microsphere-alibaba-druid-test**         | Base testing utilities and infrastructure               |
| **microsphere-alibaba-druid-spring-test**  | Spring-specific testing utilities                       |
| **microsphere-alibaba-druid-dependencies** | Bill of Materials (BOM) for dependency management       |

## Getting Started

The easiest way to get started is by adding the Microsphere Alibaba Druid BOM (Bill of Materials) to your project's
pom.xml:

```xml

<dependencyManagement>
    <dependencies>
        ...
        <!-- Microsphere Alibaba Druid Dependencies -->
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-alibaba-druid-dependencies</artifactId>
            <version>${microsphere-alibaba-druid.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        ...
    </dependencies>
</dependencyManagement>
```

`${microsphere-alibaba-druid.version}` has two branches:

| **Branches** | **Purpose**                                      | **Latest Version** |
|--------------|--------------------------------------------------|--------------------|
| **main**     | Compatible with Spring Cloud 2022.0.x - 2025.0.x | `0.2.16`           |
| **1.x**      | Compatible with Spring Cloud Hoxton - 2021.0.x   | `0.1.16`           |

Then add the specific modules you need.

### Maven Dependencies

```xml

<dependencies>
    <!-- Microsphere Alibaba Druid Spring Cloud -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-druid-spring-cloud</artifactId>
    </dependency>
</dependencies>
```

### Gradle Dependencies

```kotlin
implementation(platform("io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.0.1"))
```

## Building from Source

You don't need to build from source unless you want to try out the latest code or contribute to the project.

To build the project, follow these steps:

1. Clone the repository:

```bash
git clone https://github.com/microsphere-projects/microsphere-alibaba-druid.git
```

2. Build the source:

- Linux/MacOS:

```bash
./gradlew build
```

- Windows:

```powershell
gradlew.bat build
```

## Contributing

We welcome your contributions! Please read [Code of Conduct](./CODE_OF_CONDUCT.md) before submitting a pull request.

## Reporting Issues

* Before you log a bug, please search
  the [issues](https://github.com/microsphere-projects/microsphere-alibaba-druid/issues)
  to see if someone has already reported the problem.
* If the issue doesn't already
  exist, [create a new issue](https://github.com/microsphere-projects/microsphere-alibaba-druid/issues/new).
* Please provide as much information as possible with the issue report.

## Documentation

### User Guide

[DeepWiki Host](https://deepwiki.com/microsphere-projects/microsphere-alibaba-druid)

[ZRead Host](https://zread.ai/microsphere-projects/microsphere-alibaba-druid)

### Wiki

[Github Host](https://github.com/microsphere-projects/microsphere-alibaba-druid/wiki)

### JavaDoc

- [microsphere-alibaba-druid-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-druid-core)
- [microsphere-alibaba-druid-spring](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-druid-spring)
- [microsphere-alibaba-druid-spring-boot](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-druid-spring-boot)
- [microsphere-alibaba-druid-spring-cloud](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-druid-spring-cloud)
- [microsphere-alibaba-druid-test](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-druid-test)
- [microsphere-alibaba-druid-spring-test](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-druid-spring-test)

## License

Microsphere Alibaba Druid is licensed under
the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
