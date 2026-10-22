# 源码差异：0.1.21 vs 0.2.21（`src/main`）

[English →](../main-diff-0.2.21-vs-0.1.21.md)

本文逐模块对比 tag `0.1.21`（Java 8 / Spring Boot 2 线）与 `0.2.21`（Java 17 / Spring Boot 3 线）各 Maven 模块
`src/main` 的差异。模块按名称匹配，两个 tag 均包含相同的八个模块（`core`、`spring`、`spring-boot`、`spring-cloud`、
`spring-test`、`test`、`parent`、`dependencies`），无需忽略任何模块。表格中已排除纯粹由 Java 8 与 Java 17 语法/API
差异、以及 Java EE（`javax.*`）与 Jakarta EE（`jakarta.*`）替换带来的差异。

**对比方法**：对每个模块执行 `git diff 0.1.21 0.2.21 -- <module>/src/main`，逐文件比较 `src/main` 目录树。

## 结论概览

> **两个 tag 之间 `src/main` 下的全部 Java 源码逐字节一致**，覆盖六个含 `src/main` 代码的模块。`src/main`
> 的差异仅有资源文件。

## 模块对比

| **模块** | **src/main 差异** | **说明** |
|----------|-------------------|----------|
| `microsphere-alibaba-druid-core` | 无 | Java 源码完全一致 |
| `microsphere-alibaba-druid-spring` | 无 | Java 源码完全一致 |
| `microsphere-alibaba-druid-spring-boot` | 删除 1 个资源文件 | 见下表 |
| `microsphere-alibaba-druid-spring-cloud` | 删除 1 个资源文件 | 见下表 |
| `microsphere-alibaba-druid-spring-test` | 无 | Java 源码完全一致 |
| `microsphere-alibaba-druid-test` | 无 | Java 源码完全一致 |
| `microsphere-alibaba-druid-parent` | 无 `src` 目录 | 纯 POM 模块 |
| `microsphere-alibaba-druid-dependencies` | 无 `src` 目录 | 纯 BOM 模块 |

## 资源文件差异（src/main）

| **模块** | **文件** | **0.1.21** | **0.2.21** | **差异原因** |
|----------|----------|------------|------------|--------------|
| `spring-boot` | `src/main/resources/META-INF/spring.factories` | 存在 —— 通过 `EnableAutoConfiguration` 注册 `AlibabaDruidAutoConfiguration` | **已删除** | API 兼容：Spring Boot 3 不再从 `spring.factories` 加载自动配置。替代文件 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 在**两个 tag 中都已存在且内容一致**，因此 0.2.21 只是移除了为兼容 Spring Boot 2 使用者而保留的旧注册文件。 |
| `spring-cloud` | `src/main/resources/META-INF/spring.factories` | 存在 —— 通过 `EnableAutoConfiguration` 注册 `AlibabaDruidCloudAutoConfiguration` | **已删除** | 同上 —— 基线迁移到 Spring Boot 3 后移除旧注册方式；`AutoConfiguration.imports` 条目保持不变。 |

## 为什么 Java 源码无需随 Java 8 → 17 / javax → jakarta 迁移而修改

- **无语言级升级**：0.2.x 线没有用 record、`var`、switch 表达式等 Java 17 特性重写源码；两条发布线保持完全一致的
  源码树，可通过 `merge-main-to-branches` 工作流低成本地双向同步。
- **不触碰 Java EE / Jakarta EE 面**：代码只使用 `java.sql.DataSource`、`java.sql.Connection` 以及 JDK 自带的
  `javax.sql.*` 类型，这些类型**并未**被重命名为 `jakarta.*`，因此 Spring Boot 3 的命名空间迁移对其无影响。
- **所用 Spring API 命名空间稳定**：自动配置类依赖的 `spring-context` / `spring-boot` 注解
  （`@Configuration`、`@Bean`、`@Conditional*`、`@ConfigurationProperties`）在 Boot 2 与 Boot 3 之间全限定名未变。

## 相关的非 src 配置（仅作背景，不属于 src/main）

| **文件** | **0.1.21** | **0.2.21** | **差异原因** |
|----------|------------|------------|--------------|
| 根 `pom.xml` | `revision=0.1.21-SNAPSHOT` | `revision=0.2.21-SNAPSHOT`、`java.version=17` | 发布线版本号 / 构建基线 |
| 根 `pom.xml` 的 parent | `microsphere-spring-cloud-parent:0.1.26` | `microsphere-spring-cloud-parent:0.2.26` | 平台 BOM 升级（JDK 17 / Boot 3 线） |
| `parent/pom.xml` | `microsphere-spring-cloud.version=0.1.26` | `0.2.26` | 同一平台 BOM 升级；两条线的 `druid.version` 均为 `1.2.28` |

## 配套文档

`src/test` 的对比见独立文档：[test-diff-0.2.21-vs-0.1.21.md](../test-diff-0.2.21-vs-0.1.21.md)（中文：
[test-diff-0.2.21-vs-0.1.21.md](./test-diff-0.2.21-vs-0.1.21.md)）
