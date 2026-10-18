# microsphere-alibaba-druid-parent

[English](../microsphere-alibaba-druid-parent.md)

## 模块用途

`microsphere-alibaba-druid-parent` 是本仓库所有模块的**父 POM**（`<packaging>pom</packaging>`），也是本项目构建所依赖的
三方版本的唯一声明位置：

- `com.alibaba:druid` → `1.2.28`（属性 `druid.version`）。
- Microsphere Spring Cloud BOM `microsphere-spring-cloud-dependencies` → `0.2.26`（属性
  `microsphere-spring-cloud.version`），以 `<scope>import</scope>` 导入，由它提供 `microsphere-*` 构件、Spring
  Framework、Spring Boot、Spring Cloud、JUnit、H2、Logback 等的版本。

大多数应用**并不**使用本 POM —— 它们导入的是
[microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)。只有当你要构建自己的
Microsphere 风格库/模块，并希望与本仓库拥有一致的依赖管理与构建配置时，才使用这个 parent。

## Maven 坐标

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-parent</artifactId>
    <version>0.2.21</version>
</parent>
```

| 项目 | 值 |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-parent` |
| 版本（`current` 文档线） | 已发布 `0.2.21`；`main` 上为 `0.2.22-SNAPSHOT` |
| 打包方式 | `pom` |
| 反应堆中的名称 | `Microsphere :: Alibaba Druid :: Parent` |
| 被谁继承 | `microsphere-alibaba-druid-core`、`-test`、`-spring`、`-spring-test`、`-spring-boot`、`-spring-cloud`、`-dependencies` |

## POM 说明

### 父 POM 链

| 层级 | POM | 声明位置 |
|---|---|---|
| 1 | `microsphere-alibaba-druid-parent` | 你的模块 |
| 2 | `microsphere-alibaba-druid`（仓库根聚合 POM，`<java.version>17</java.version>`、`<revision>0.2.22-SNAPSHOT</revision>`） | `../pom.xml` |
| 3 | `io.github.microsphere-projects:microsphere-spring-cloud-parent:0.2.26` | 根 POM 的 `<parent>` —— **外部构件，不在本仓库** |

Spring Boot / Spring Cloud 的版本 profile（`spring-cloud-2022` … `spring-cloud-2025`）来自第 3 层；本仓库从不声明它们。

### 本 POM 声明的属性

| 属性 | 值 | 用途 |
|---|---|---|
| `microsphere-spring-cloud.version` | `0.2.26` | 被导入的 `microsphere-spring-cloud-dependencies` BOM 的版本 |
| `druid.version` | `1.2.28` | `dependencyManagement` 中 `com.alibaba:druid` 的版本 |

两者都是普通 Maven 属性：若你继承了本 POM 却需要不同的 Druid 版本，在自己 POM 的 `<properties>` 中重新定义即可。

### 被管理的依赖

| 条目 | 坐标 | 类型 |
|---|---|---|
| Alibaba Druid | `com.alibaba:druid:${druid.version}`（即 `1.2.28`） | 版本管理 |
| Microsphere Spring Cloud BOM | `io.github.microsphere-projects:microsphere-spring-cloud-dependencies:${microsphere-spring-cloud.version}`（即 `0.2.26`），`<type>pom</type> <scope>import</scope>` | BOM 导入 |

本 POM **并不**管理本仓库自身模块的版本 —— 那是 `microsphere-alibaba-druid-dependencies` 的职责。

### 反应堆中的模块（根 POM）

| 模块 | 角色 |
|---|---|
| `microsphere-alibaba-druid-parent` | 本 POM |
| `microsphere-alibaba-druid-dependencies` | BOM |
| `microsphere-alibaba-druid-core` | `AbstractStatementFilter`、`LoggingStatementFilter`、`PropertyConstants` |
| `microsphere-alibaba-druid-test` | 与 Spring 无关的测试工具 |
| `microsphere-alibaba-druid-spring` | `@EnableAlibabaDruid`、`DruidDataSourceBeanPostProcessor` |
| `microsphere-alibaba-druid-spring-test` | Spring 测试支持 |
| `microsphere-alibaba-druid-spring-boot` | 自动配置、属性、连接池元数据 |
| `microsphere-alibaba-druid-spring-cloud` | Spring Cloud 自动配置、`features.yaml` |

## 使用示例

### 作为你自己库模块的 parent

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

> 该 parent 管理 Druid 与 Microsphere Spring Cloud 构件的版本，但**不**管理 `microsphere-alibaba-druid-*` 自身 —— 仍需显式版本（或导入 BOM）：
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

### 继承本 POM 的同时改钉 Druid 版本

```xml
<properties>
    <druid.version>1.2.24</druid.version>
</properties>
```

### 仅作为版本来源（应用的推荐做法）

应用通常完全跳过本 POM，改用 [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
，并保留自己的 Spring Boot parent。

## 配置属性

本模块没有运行时代码，因而完全没有配置属性；`microsphere.alibaba.druid.*` 属性属于其他模块：

| 项目 | 值 |
|---|---|
| 运行时代码 | 无 |
| 配置属性 | 无 |
| 相关属性名（仅作参照） | `microsphere.alibaba.druid.enabled`、`microsphere.alibaba.druid.filter.classes` —— 见 [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md) |

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `current`（release 分支） |
| 最新已发布版本 | `0.2.21`（`main` 上的 `0.2.22-SNAPSHOT` 是未发布的开发版） |
| 声明的 `druid.version` | `1.2.28` |
| 声明的 `microsphere-spring-cloud.version` | `0.2.26` |
| Java | 17+（根 `pom.xml` 中 `<java.version>17</java.version>`） |
| CI JDK 矩阵 | 17 / 21 / 25，每个 JDK 分别与 profile `spring-cloud-2022`、`spring-cloud-2023`、`spring-cloud-2024`、`spring-cloud-2025` 组合（`.github/workflows/maven-build.yml`） |
| 版本写法 | 源码中使用 CI 友好的 `${revision}`；发布产物携带具体发布版本号 |

## 兼容范围

| 维度 | `current` 文档线支持情况 |
|---|---|
| Alibaba Druid | 管理为 `1.2.28`；可用 `druid.version` 属性覆盖 |
| Java | 17、21、25（要求 JDK 17+） |
| Spring Boot | 按构建 profile 分别为 `3.0.13`、`3.3.13`、`3.4.13`、`3.5.15`，即 Boot `3.0.x` – `3.5.x` |
| Spring Cloud | 按构建 profile 分别为 `2022.0.5`、`2023.0.6`、`2024.0.3`、`2025.0.3`，即 Spring Cloud `2022.0.x` – `2025.0.x` |
| 上述 Boot/Cloud 版本的来源 | 外部父 POM 链 `io.github.microsphere-projects:microsphere-spring-cloud-parent:0.2.26` 及其 `microsphere-spring-cloud-dependencies` BOM —— **并非本仓库声明** |

JDK 8 / Spring Cloud `Hoxton` – `2021.0.x` 由 `1.x` 文档线（parent POM `0.1.x`）负责：见
[1.x 用户文档](../../../1.x/user-guide/README.md)。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [项目 README](../../../../README.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-parent.md)

## English

- [English version of this document](../microsphere-alibaba-druid-parent.md)
