# 源码差异：0.1.21 vs 0.2.21（`src/test`）

[English →](../test-diff-0.2.21-vs-0.1.21.md)

本文逐模块对比 tag `0.1.21`（Java 8 / Spring Boot 2 线）与 `0.2.21`（Java 17 / Spring Boot 3 线）各 Maven 模块
`src/test` 的差异。模块按名称匹配，两个 tag 均包含相同的八个模块，无需忽略任何模块。纯粹的 Java 8 与 Java 17
语法/API 差异、Java EE 与 Jakarta EE 命名空间差异已被排除（本次对比中未出现此类差异）。

**对比方法**：对每个模块执行 `git diff 0.1.21 0.2.21 -- <module>/src/test`，逐文件比较 `src/test` 目录树。

## 结论概览

> **除 `microsphere-alibaba-druid-test` 中的一个测试类做了结构调整（行为无变化）外，两个 tag 的测试源码逐字节一致。**

## 模块对比

| **模块** | **src/test 差异** | **说明** |
|----------|-------------------|----------|
| `microsphere-alibaba-druid-core` | 无 | |
| `microsphere-alibaba-druid-spring` | 无 | 其中 `src/test/resources/META-INF/spring.factories`（测试作用域的注册文件）在两条线中保持一致 |
| `microsphere-alibaba-druid-spring-boot` | 无 | |
| `microsphere-alibaba-druid-spring-cloud` | 无 | |
| `microsphere-alibaba-druid-spring-test` | 无 | |
| `microsphere-alibaba-druid-test` | 1 个文件变更 | 见下表 |
| `microsphere-alibaba-druid-parent` | 无 `src` 目录 | 纯 POM 模块 |
| `microsphere-alibaba-druid-dependencies` | 无 `src` 目录 | 纯 BOM 模块 |

## 变更的测试文件（src/test）

| **模块** | **文件** | **变更内容** | **差异原因** |
|----------|----------|--------------|--------------|
| `test` | `src/test/java/io/microsphere/alibaba/druid/test/junit/jupiter/AlibabaDruidTestExtensionTest.java` | 顶层的静态字段注入测试被移入 `@Nested` 内部类 `StaticFieldTest`（`@DisplayName("Test : Static Field Injection")`）；`assertObjects(Object...)` 辅助方法调整到类的下方。断言与逻辑不变（对两个注入字段执行 `assertNotNull`，`close(druidDataSource)` 仍调用两次）。 | 测试结构 / 功能验证：改写后把*静态字段注入*场景与既有的 `@Nested ParameterTest` 并列，保持"每种注入场景一个 `@Nested` 类"的组织方式，用于验证 `@AlibabaDruidRuntime` 对嵌套测试类中 `static` 字段的注入同样生效 —— 属于能力验证而非缺陷修复（生产代码完全一致，见 src/main 对比文档）。 |

## 已排除的差异类别

- **Java 8 与 Java 17 语法/API**：未出现 —— 两条线的测试代码均编写在 Java 8 兼容级别。
- **Java EE 与 Jakarta EE**：未出现 —— 测试仅使用 JDK `java.sql` / Druid 类型与 JUnit 5，其坐标不受命名空间迁移影响。

## 配套文档

`src/main` 的对比见独立文档：[main-diff-0.2.21-vs-0.1.21.md](../main-diff-0.2.21-vs-0.1.21.md)（中文：
[main-diff-0.2.21-vs-0.1.21.md](./main-diff-0.2.21-vs-0.1.21.md)）
