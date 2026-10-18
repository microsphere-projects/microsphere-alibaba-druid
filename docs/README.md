# Documentation

Documentation for [Microsphere Alibaba Druid](../README.md), written per Maven module and analysed **only** from the
release branches:

| **Branch** | **Docs Directory** | **Artifact Line** | **Baseline** | **Spring Cloud Compatibility** |
|------------|--------------------|-------------------|--------------|--------------------------------|
| `release` | [`current/`](./current/user-guide/README.md) | `0.2.x` (latest `0.2.21`) | JDK 17, Druid `1.2.28` | `2022.0.x` – `2025.0.x` |
| `release-1.x` | [`1.x/`](./1.x/user-guide/README.md) | `0.1.x` (latest `0.1.21`) | JDK 8, Druid `1.2.28` | `Hoxton` – `2021.0.x` |

[中文文档主页 →](./zh/README.md)

## Documentation Sets

| **Set** | **Audience** | **current (0.2.x)** | **1.x (0.1.x)** |
|---------|--------------|---------------------|-----------------|
| User Guide | Consumers of the library — APIs, configuration properties, code examples, version & compatibility | [User Guide](./current/user-guide/README.md) / [用户文档](./current/user-guide/zh/README.md) | [User Guide](./1.x/user-guide/README.md) / [用户文档](./1.x/user-guide/zh/README.md) |
| Developer Guide | Maintainers — architecture, source analysis, design patterns, Mermaid flow diagrams | [Developer Guide](./current/developer-guide/README.md) / [开发者文档](./current/developer-guide/zh/README.md) | [Developer Guide](./1.x/developer-guide/README.md) / [开发者文档](./1.x/developer-guide/zh/README.md) |

## Modules Documented

Each guide set contains one document per Maven module:

| **Module** | **Purpose** |
|------------|-------------|
| `microsphere-alibaba-druid-core` | SQL statement interception filters (`AbstractStatementFilter`, `LoggingStatementFilter`) and property constants |
| `microsphere-alibaba-druid-spring` | Spring Framework integration (`@EnableAlibabaDruid`, `DruidDataSourceBeanPostProcessor`) |
| `microsphere-alibaba-druid-spring-boot` | Spring Boot auto-configuration, properties and Actuator pool metadata |
| `microsphere-alibaba-druid-spring-cloud` | Spring Cloud integration (feature declarations via `features.yaml`) |
| `microsphere-alibaba-druid-test` | Test utilities and the JUnit 5 extension (`@AlibabaDruidTest` / `@AlibabaDruidRuntime`) |
| `microsphere-alibaba-druid-spring-test` | Spring-aware test support (`AbstractDruidSpringTest`, `DruidDataSourceTestConfiguration`) |
| `microsphere-alibaba-druid-parent` | Parent POM: Druid version management and BOM import |
| `microsphere-alibaba-druid-dependencies` | Bill of Materials (BOM) for the module family |

## Languages

English is the default language and lives at the primary paths. Chinese mirrors are stored in a parallel `zh/`
directory inside every guide set, with identical file names; code blocks and diagrams are kept identical between the
two languages. Each document links to its counterpart at the top and bottom.

## English

You are reading the English documentation home. The Chinese home is [docs/zh/README.md](./zh/README.md).
