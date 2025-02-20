# Microsphere Alibaba Druid

[![Maven Build](https://github.com/microsphere-projects/microsphere-alibaba-druid/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-alibaba-druid/actions/workflows/gradle-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-alibaba-druid/branch/dev-1.x/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-alibaba-druid)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-alibaba-druid-dependencies.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-alibaba-druid.svg)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/microsphere-projects/microsphere-alibaba-druid.svg)](http://isitmaintained.com/project/microsphere-projects/microsphere-alibaba-druid "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/microsphere-projects/microsphere-alibaba-druid.svg)](http://isitmaintained.com/project/microsphere-projects/microsphere-alibaba-druid "Percentage of issues still open")

## 1. Introduction

Microsphere Alibaba Druid is a Java ECO project of [Microsphere](https://github.com/orgs/microsphere-projects) ,
which integrates [Alibaba Druid](https://github.com/alibaba/druid) and supports the extension features that would be
used for [Microsphere Sentinel](https://github.com/microsphere-projects/microsphere-sentinel),
[Microsphere Resilience4j](https://github.com/microsphere-projects/microsphere-resilience4j),
[Microsphere Observability](https://github.com/microsphere-projects/microsphere-observability) and so on.

## 2. Features

Microsphere Alibaba Druid supports the following features:

- Interception on executing the SQL Statements
- Logging the SQL Statements

## 3. Usage

> ${microsphere-alibaba-druid.version} : The latest version of Microsphere Alibaba Druid

### 3.1. Dependency 

#### 3.1.1 Maven

```xml
<dependencyManagement>
    <dependencies>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-druid-dependencies</artifactId>
        <version>${microsphere-alibaba-druid.version}</version>
    </dependency>
</dependencyManagement>
```

#### 3.1.2 Gradle Dependency

```kotlin
implementation("io.github.microsphere-projects:microsphere-alibaba-druid:${microsphere-alibaba-druid.version}")
```

### 3.2. Demo

See [Demo](microsphere-alibaba-druid-core/src/main/java/io/microsphere/druid/filter/LoggingStatementFilter.java):

More: [Wiki](https://github.com/microsphere-projects/microsphere-alibaba-druid/wiki)

## 4. Contributing

We welcome all kinds of contributions, such as:
- Submitting [issues](https://github.com/microsphere-projects/microsphere-alibaba-druid/issues)
- Submitting [pull requests](https://github.com/microsphere-projects/microsphere-alibaba-druid/pulls)
- Editing [Wiki](https://github.com/microsphere-projects/microsphere-alibaba-druid/wiki) 


## 5. License

Microsphere Alibaba Druid is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
