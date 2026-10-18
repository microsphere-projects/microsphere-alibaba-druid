# microsphere-alibaba-druid-dependencies（`1.x`）

[English](../microsphere-alibaba-druid-dependencies.md)

## 模块用途

`microsphere-alibaba-druid-dependencies` 是 `1.x` 文档线上 Microsphere Alibaba Druid 的 **BOM（Bill of Materials）**。
它是一个 `pom` 制品，内容只有一个 `<dependencyManagement>` 段，用于锁定本仓库 6 个可发布模块的版本，
使应用可以不带版本号地声明它们。

## Maven 坐标

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-dependencies</artifactId>
    <version>0.1.21</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

| 项目 | 值 |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-dependencies` |
| 版本 | `${revision}` —— 已发布 `0.1.21`，`release-1.x` 分支为 `0.1.22-SNAPSHOT` |
| 打包方式 | `pom` |
| 它的父 POM | `io.github.microsphere-projects:microsphere-alibaba-druid-parent` |
| Java 代码 | 无 —— 只有元数据 |

## BOM 锁定的内容

恰好以下 6 个构件，版本全部为 `${revision}`（即 `0.1.21`）：

| artifactId | 类别 | 在你的构建中常见的作用域 |
|---|---|---|
| `microsphere-alibaba-druid-core` | 运行时 | `compile` |
| `microsphere-alibaba-druid-spring` | 运行时（`compile` 依赖 `core`） | `compile` |
| `microsphere-alibaba-druid-spring-boot` | 运行时（`compile` 依赖 `spring`） | `compile` |
| `microsphere-alibaba-druid-spring-cloud` | 运行时（`compile` 依赖 `spring-boot`） | `compile` |
| `microsphere-alibaba-druid-test` | 测试支持 | `test` |
| `microsphere-alibaba-druid-spring-test` | 测试支持（`compile` 依赖 `test`） | `test` |

其中**不包含** `microsphere-alibaba-druid-parent` 与根聚合 pom（它们是构建期的 parent，不是依赖）——
见 [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)。

## BOM 不管理的内容

BOM 没有声明任何第三方库的版本。下列构件的版本另有来源，需要你自行提供（或在继承父 POM 时由其提供）：

| 构件 | 由谁管理 | `1.x` 文档线取值 |
|---|---|---|
| `com.alibaba:druid` | `microsphere-alibaba-druid-parent` → `${druid.version}` | `1.2.28` |
| Spring Boot / Spring Cloud / Spring Framework | 由 parent 导入的 `microsphere-spring-cloud-dependencies:0.1.26` | 视激活的 profile 而定：Boot `2.3.12.RELEASE` – `2.6.15`、Cloud `Hoxton.SR12` – `2021.0.9` |
| Microsphere 库（`microsphere-java-core`、`microsphere-spring-context` 等） | 同一个 Microsphere BOM | `0.1.26` 线 |
| `com.h2database:h2`、`logback-classic`、JUnit Jupiter | 当前生效的 Boot/Cloud 线 | 随 profile 变化 |

> 说明：由于本 BOM *继承*自 `microsphere-alibaba-druid-parent`，其生效的依赖管理还包含 `com.alibaba:druid` 条目
> 以及被导入的 Microsphere BOM。这属于 Maven `import` 语义长期以来的行为，但如果你的构建依赖
> “不写版本的 `com.alibaba:druid`”，更稳妥的做法是自行声明 `<druid.version>`（或显式版本），
> 或者直接以 `microsphere-alibaba-druid-parent` 作为你的 `<parent>`。

## 使用方式

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

### Gradle（Kotlin DSL）

```kotlin
dependencies {
    implementation(platform("io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.1.21"))
    implementation("io.github.microsphere-projects:microsphere-alibaba-druid-spring-cloud")

    testImplementation(platform("io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.1.21"))
    testImplementation("io.github.microsphere-projects:microsphere-alibaba-druid-test")
}
```

Groovy DSL：

```groovy
dependencies {
    implementation platform('io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.1.21')
    implementation 'io.github.microsphere-projects:microsphere-alibaba-druid-spring-cloud'
}
```

### 与 Spring Boot 自带 BOM 混用

版本冲突时导入顺序很关键：Maven `dependencyManagement` 中**先**导入的 BOM 生效。若要在 Boot BOM 也管理 Druid
的情况下把 Druid 保持在 `1.2.28`，请把 Microsphere BOM 写在前面 —— 或者显式对齐：

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

## 选择正确的文档线

`0.1.x`（本 BOM）面向 Spring Cloud `Hoxton` – `2021.0.x` / JDK 8。若你的应用使用 Spring Boot 3 /
Spring Cloud `2022.0.x` – `2025.0.x`，请改为导入 `current` 线的 `0.2.21` —— 构件名完全一致，
只需改一个属性。

## 配置属性

无：本制品不贡献任何运行时配置。

| 属性 | 是否作用于 BOM |
|---|---|
| `microsphere.alibaba.druid.enabled` | 否 —— 在 [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md) 中绑定 |
| `microsphere.alibaba.druid.filter.classes` | 否 —— 在 [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md) 中绑定 |

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `1.x`（由 `release-1.x` 分支生成） |
| 最新已发布版本 | `0.1.21`（`0.1.22-SNAPSHOT` 是分支上未发布的开发版） |
| 被锁定的构件 | 上文列出的 6 个模块，版本均为 `${revision}` |
| 对使用者的 Java 基线含义 | `8`（这些 jar 以 source/target `8` 编译，继承自 `microsphere-build:0.3.14`，经 `microsphere-spring-cloud:0.1.26` 传入） |
| 各模块期望的 Alibaba Druid | `1.2.28` |
| Maven Central 徽章口径 | 根 README 以 `io.github.microsphere-projects/microsphere-alibaba-druid-dependencies` 作为发布标记 |

## 兼容范围

| 维度 | `1.x` 文档线取值 |
|---|---|
| Maven | 3.6+（`import` 作用域 + `${revision}` 解析） |
| Gradle | 使用 POM 的 `platform(...)` 需 6.8+（建议 Gradle 7+） |
| Java | 8 及以上 |
| Spring Boot | `2.3.12.RELEASE`、`2.4.13`、`2.6.15` |
| Spring Cloud | `Hoxton.SR12`、`2020.0.5`、`2021.0.9` |
| Alibaba Druid | `1.2.28` |

## 与 current 文档线的差异

结构与构件清单完全一致，差异只在所锁定的版本线 —— 本线为 `0.1.21`（`Hoxton` – `2021.0.x`、JDK 8），
`current` 线为 `0.2.21`（Spring Cloud `2022.0.x` – `2025.0.x`、JDK 17）。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring-cloud.md](microsphere-alibaba-druid-spring-cloud.md)
- [项目 README](../../../../README.md)
- [current 文档线的同名文档](../../../current/user-guide/zh/microsphere-alibaba-druid-dependencies.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-dependencies.md)

## English

- [English version of this document](../microsphere-alibaba-druid-dependencies.md)
