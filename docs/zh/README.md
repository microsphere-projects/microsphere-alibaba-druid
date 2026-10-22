# 文档主页

本目录是 [Microsphere Alibaba Druid](../../README.md) 的中文文档入口，按 Maven 模块组织，内容**仅**基于发布分支分析：

| **分支** | **文档目录** | **产物版本线** | **基线** | **Spring Cloud 兼容范围** |
|----------|--------------|----------------|----------|---------------------------|
| `release` | [`current/`](../current/user-guide/zh/README.md) | `0.2.x`（最新 `0.2.21`） | JDK 17、Druid `1.2.28` | `2022.0.x` – `2025.0.x` |
| `release-1.x` | [`1.x/`](../1.x/user-guide/zh/README.md) | `0.1.x`（最新 `0.1.21`） | JDK 8、Druid `1.2.28` | `Hoxton` – `2021.0.x` |

[English documentation home →](../README.md)

## 文档集

| **文档集** | **目标读者** | **current（0.2.x）** | **1.x（0.1.x）** |
|------------|--------------|----------------------|------------------|
| 用户文档 | 库的使用者 —— API、配置属性、代码示例、版本与兼容范围 | [用户文档](../current/user-guide/zh/README.md) | [用户文档](../1.x/user-guide/zh/README.md) |
| 开发者文档 | 维护者 —— 架构设计、源码分析、设计模式、Mermaid 流程图 | [开发者文档](../current/developer-guide/zh/README.md) | [开发者文档](../1.x/developer-guide/zh/README.md) |

## 覆盖的 Maven 模块

每个文档集均按模块提供独立文档：

| **模块** | **用途** |
|----------|----------|
| `microsphere-alibaba-druid-core` | SQL 语句拦截过滤器（`AbstractStatementFilter`、`LoggingStatementFilter`）与属性常量 |
| `microsphere-alibaba-druid-spring` | Spring Framework 集成（`@EnableAlibabaDruid`、`DruidDataSourceBeanPostProcessor`） |
| `microsphere-alibaba-druid-spring-boot` | Spring Boot 自动配置、配置属性与 Actuator 连接池元数据 |
| `microsphere-alibaba-druid-spring-cloud` | Spring Cloud 集成（通过 `features.yaml` 声明特性） |
| `microsphere-alibaba-druid-test` | 测试工具与 JUnit 5 扩展（`@AlibabaDruidTest` / `@AlibabaDruidRuntime`） |
| `microsphere-alibaba-druid-spring-test` | Spring 场景测试支撑（`AbstractDruidSpringTest`、`DruidDataSourceTestConfiguration`） |
| `microsphere-alibaba-druid-parent` | 父 POM：Druid 版本管理与 BOM 导入 |
| `microsphere-alibaba-druid-dependencies` | 模块族的依赖清单 BOM |

## 源码差异

按 Maven 模块对比各发布 tag 之间 `src/main` / `src/test` 的差异，已排除 Java 8 与 Java 17 语法/API 差异及 Java EE 与
Jakarta EE 命名空间差异：

| **Tag 组合** | **src/main** | **src/test** |
|--------------|--------------|--------------|
| `0.2.21` vs `0.1.21` | [main-diff-0.2.21-vs-0.1.21.md](../diff/source/main-diff-0.2.21-vs-0.1.21.md)（中文：[main-diff-0.2.21-vs-0.1.21.md](../diff/source/zh/main-diff-0.2.21-vs-0.1.21.md)） | [test-diff-0.2.21-vs-0.1.21.md](../diff/source/test-diff-0.2.21-vs-0.1.21.md)（中文：[test-diff-0.2.21-vs-0.1.21.md](../diff/source/zh/test-diff-0.2.21-vs-0.1.21.md)） |

## 语言约定

英文为默认语言，存放于各指南的主路径；中文镜像以相同文件名存放在每个指南目录下平行的 `zh/` 子目录中，代码块与流程图在两种语言之间保持完全一致，每篇文档的顶部与底部均提供互链。

## English

此页面为中文文档主页，英文主页见 [docs/README.md](../README.md)。
