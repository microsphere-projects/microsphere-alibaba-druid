# microsphere-alibaba-druid-dependencies

[English](../microsphere-alibaba-druid-dependencies.md)

## 模块用途

`microsphere-alibaba-druid-dependencies` 是 Microsphere Alibaba Druid 的 **Bill of Materials（BOM）**：一个
`<packaging>pom</packaging>` 构件，其唯一内容是一份 `<dependencyManagement>`，用于钉住本仓库六个已发布模块的版本。在
你的 `<dependencyManagement>` 中导入一次，之后所有 `microsphere-alibaba-druid-*` 依赖都可以不写 `<version>`。

## Maven 坐标

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-dependencies</artifactId>
    <version>0.2.21</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

| 项目 | 值 |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-dependencies` |
| 版本（`current` 文档线） | 已发布 `0.2.21`；`main` 上为 `0.2.22-SNAPSHOT` |
| 打包方式 | `pom` |
| 反应堆中的名称 | `Microsphere :: Alibaba Druid :: Dependencies` |
| 父 POM | `microsphere-alibaba-druid-parent`（见 [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)） |
| 运行时代码 | 无 —— 仅元数据 |

## BOM 成员清单

本 POM 管理的完整列表（`groupId` 均为 `io.github.microsphere-projects`，版本均为项目版本）：

| 构件 | 提供的内容 | 文档 |
|---|---|---|
| `microsphere-alibaba-druid-core` | `AbstractStatementFilter`、`LoggingStatementFilter`、`PropertyConstants` | [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md) |
| `microsphere-alibaba-druid-test` | `AbstractAlibabaDruidTest`、`AlibabaDruidTestUtils`、`@AlibabaDruidTest`、`@AlibabaDruidRuntime` | [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md) |
| `microsphere-alibaba-druid-spring` | `@EnableAlibabaDruid`、`AlibabaDruidRegistrar`、`DruidDataSourceBeanPostProcessor` | [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md) |
| `microsphere-alibaba-druid-spring-test` | `AbstractDruidSpringTest`、`DruidDataSourceTestConfiguration` | [microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md) |
| `microsphere-alibaba-druid-spring-boot` | `AlibabaDruidAutoConfiguration`、`AlibabaDruidProperties`、条件注解、`DruidDataSourcePoolMetadata` | [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md) |
| `microsphere-alibaba-druid-spring-cloud` | `AlibabaDruidCloudAutoConfiguration`、`features.yaml` | [microsphere-alibaba-druid-spring-cloud.md](microsphere-alibaba-druid-spring-cloud.md) |

共六条 —— 该 POM 没有声明其他任何内容。

> BOM 中**没有**的内容：`microsphere-alibaba-druid-parent` 与 `microsphere-alibaba-druid-dependencies` 本身是 POM
> 构件，不属于被管理的依赖；三方库也不在此钉版本。`com.alibaba:druid`（`1.2.28`）以及 Spring Boot / Spring Cloud 的版本来自本
> BOM 继承的 [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)。

## 使用示例

### 导入 BOM（Maven）

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

随后声明模块时无需版本。注意 Druid 在每个模块中都是**可选**依赖，必须由你自己声明：

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

如果 Maven 无法解析 BOM 背后的父 POM 链，`com.alibaba:druid` 将不带版本到来 —— 请自行钉住，例如
`<version>1.2.28</version>`（可用 `mvn dependency:tree` 验证）。

### 导入 BOM（Gradle）

```kotlin
implementation(platform("io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.2.21"))
```

### 仅测试用的模块

`microsphere-alibaba-druid-test` 与 `microsphere-alibaba-druid-spring-test` 与其他构件一样被管理；在你的 POM 中使用
`test` 作用域：

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-spring-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 选择正确的文档线

| 文档线 | 最新版本 | 目标环境 |
|---|---|---|
| `current`（本文档） | `0.2.21` | JDK 17+、Spring Boot `3.0.x` – `3.5.x`、Spring Cloud `2022.0.x` – `2025.0.x` |
| `1.x` | `0.1.21` | JDK 8、Spring Boot `2.x`、Spring Cloud `Hoxton` – `2021.0.x` —— 见 [1.x 用户文档](../../../1.x/user-guide/README.md) |

不要混用文档线：`0.2.x` 的 BOM 搭配 `0.1.x` 的模块（或反之）会产生不一致的字节码与不匹配的 Spring API。

## 配置属性

BOM 没有运行时行为，也不绑定任何属性。它所钉住的构件涉及这些属性：

| 属性 | 类型 | 默认值 | 定义位置 |
|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true`（`matchIfMissing = true`） | `PropertyConstants`（core），由 `AlibabaDruidProperties`（spring-boot）绑定 |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter`（spring-boot） |

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `current`（release 分支） |
| 最新已发布版本 | `0.2.21`（`main` 上的 `0.2.22-SNAPSHOT` 是未发布的开发版） |
| 被管理的构件 | 上述六个模块 |
| 源码 POM 中的版本表达式 | `${revision}`，由仓库根 POM 定义 |
| Java | 17+（根 `pom.xml` 中 `<java.version>17</java.version>`） |
| CI JDK 矩阵 | 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`，由 `microsphere-alibaba-druid-parent` 管理 |

## 兼容范围

| 维度 | `current` 文档线支持情况 |
|---|---|
| 被管理的模块版本 | 六个模块统一为 `0.2.21` |
| Java | 17、21、25（要求 JDK 17+） |
| Alibaba Druid | `1.2.28`（经由继承的父 POM） |
| Spring Boot / Spring Cloud | Boot `3.0.x` – `3.5.x` 搭配 Spring Cloud `2022.0.x` – `2025.0.x`（`3.0.13`/`2022.0.5`、`3.3.13`/`2023.0.6`、`3.4.13`/`2024.0.3`、`3.5.15`/`2025.0.3`）；这些版本**继承自外部父 POM 链** `microsphere-spring-cloud-parent:0.2.26`，并非本仓库声明 |
| Maven 要求 | `<dependencyManagement>` 的 `import` 作用域（Maven 2.0.9+）；Gradle 需使用 `platform(...)`（java-platform 支持） |

仍在使用 JDK 8 / Boot 2.x？请改为导入 [1.x 用户文档](../../../1.x/user-guide/README.md) 中的 `0.1.x` BOM。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-spring-cloud.md](microsphere-alibaba-druid-spring-cloud.md)
- [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)
- [microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md)
- [项目 README](../../../../README.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-dependencies.md)

## English

- [English version of this document](../microsphere-alibaba-druid-dependencies.md)
