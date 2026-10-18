# microsphere-alibaba-druid-spring-cloud（`1.x`）

[English](../microsphere-alibaba-druid-spring-cloud.md)

## 模块用途

`microsphere-alibaba-druid-spring-cloud` 是 `1.x` 文档线上运行时栈的最顶层。它没有为 SQL 拦截增加新能力，
而是让本项目在 Spring Cloud 应用中“可见”：

- `AlibabaDruidCloudAutoConfiguration` —— Spring Cloud 入口，使用与 Boot 自动配置相同的条件门控。
- `META-INF/config/default/features.yaml` —— 声明 Alibaba Druid 的相关类，使 Spring Cloud 的
  `/actuator/features` 端点把它们作为已启用特性上报。
- **本文档线特有：** 与 Boot 模块一样，它同时提供 `META-INF/spring.factories`（Boot 2.x）与
  `AutoConfiguration.imports`（Boot 2.7+），因此在整个 `Hoxton` – `2021.0.x` 区间都可用。

## Maven 坐标

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-spring-cloud</artifactId>
</dependency>
```

| 项目 | 值 |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-spring-cloud` |
| 版本 | 由 `microsphere-alibaba-druid-dependencies` BOM 管理（`1.x` 文档线为 `0.1.21`） |
| 打包方式 | `jar` |
| compile 依赖 | `microsphere-alibaba-druid-spring-boot`（非 optional，传递引入 `spring` 与 `core`） |
| optional 依赖 | `microsphere-annotation-processor`、`microsphere-spring-cloud-commons`、`com.alibaba:druid`、`spring-cloud-commons`、`spring-boot-starter`、`spring-boot-starter-actuator`、`spring-boot-configuration-processor`、`spring-boot-autoconfigure-processor` |

导入一次 BOM，之后无需再写版本号：

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
```

Gradle 写法：

```kotlin
implementation(platform("io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.1.21"))

implementation("io.github.microsphere-projects:microsphere-alibaba-druid-spring-cloud")
```

`spring-cloud-commons` 是 optional 依赖，因此需要你自己添加它（或引入会带入它的 Spring Cloud starter），
并同时添加 `com.alibaba:druid`。

## API 说明

### `io.microsphere.alibaba.druid.spring.cloud.autoconfigure.AlibabaDruidCloudAutoConfiguration`

`public class AlibabaDruidCloudAutoConfiguration`（自 `1.0.0` 起）—— 一个**空壳类**，仅带
`@ConditionalOnAlibabaDruidAvailable`（即 `@ConditionalOnAlibabaDruidEnabled` +
`@ConditionalOnClass(name = "com.alibaba.druid.pool.DruidDataSource")`）。

它有意不声明任何 `@Bean` 方法：真正有用的 Bean 来自位于本模块 compile 路径上的
[microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)。它的作用是充当 Spring
Cloud 的发现/管理入口，并让上文的能力元数据成为自动配置上下文的一部分。

### 自动配置注册文件

`src/main/resources/META-INF/spring.factories`：

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.alibaba.druid.spring.cloud.autoconfigure.AlibabaDruidCloudAutoConfiguration
```

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`：

```text
io.microsphere.alibaba.druid.spring.cloud.autoconfigure.AlibabaDruidCloudAutoConfiguration
```

| Boot 版本 | 实际读取的文件 |
|---|---|
| 2.3 – 2.6（本文档线的矩阵） | `spring.factories` |
| 2.7 | 两份都读，并去重 |
| 3.x | `AutoConfiguration.imports` |

### `META-INF/config/default/features.yaml`

```yaml
microsphere:
  spring:
    cloud:
      features:
        abstract:
          microsphere-alibaba-druid-core:
            - com.alibaba.druid.pool.DruidDataSource
            - com.alibaba.druid.filter.Filter
```

`microsphere-spring-cloud-commons` 会读取类路径上每个 jar 的 `META-INF/config/default/features.yaml`，
并把每个 `abstract` 分组转换成 Spring Cloud `FeaturesEndpoint` 使用的 `HasFeatures` / `NamedFeature` 条目。
分组键（`microsphere-alibaba-druid-core`）成为特性集名称，列出的每个类只有**确实存在于类路径**时才会被上报。

## 使用示例

### 启用特性端点

`AlibabaDruidCloudAutoConfigurationIntegrationTest`（测试源码）即按以下方式启动：

```java
@SpringBootTest(classes = {
        LoggingStatementFilter.class,
        DruidDataSourceTestConfiguration.class,
        AlibabaDruidCloudAutoConfigurationIntegrationTest.class
}, webEnvironment = NONE,
        properties = {
                "microsphere.alibaba.druid.enabled=true",
                "management.endpoints.web.exposure.include=features",
                "spring.cloud.features.enabled=true",
                "microsphere.alibaba.druid.filter.classes=io.microsphere.alibaba.druid.filter.LoggingStatementFilter"
        })
@EnableAutoConfiguration
public class AlibabaDruidCloudAutoConfigurationIntegrationTest extends AbstractDruidSpringTest {
}
```

在你的应用中等价写法是：

```properties
management.endpoints.web.exposure.include=health,features
spring.cloud.features.enabled=true
```

### `/actuator/features` 上报的内容

同一个集成测试已验证 —— `HasFeatures` Bean 以名称 `microsphere-alibaba-druid-core.features` 注册，
并携带两个具名特性：

```java
HasFeatures hadFeatures = this.hasFeaturesMap.get("microsphere-alibaba-druid-core.features");
List<NamedFeature> namedFeatures = hadFeatures.getNamedFeatures();
assertEquals(2, namedFeatures.size());

// index 0 -> name "microsphere-alibaba-druid-core:DruidDataSource", type DruidDataSource.class
// index 1 -> name "microsphere-alibaba-druid-core:LoggingStatementFilter", type LoggingStatementFilter.class
```

命名规则为 `<分组>:<简单类名>`。第二项是上下文中实际存在的*具体* filter Bean；若你没有注册任何
`Filter` Bean，则只会上报已声明且位于类路径上的抽象类。

### 不注册 filter 的普通 Spring Cloud 场景

不需要任何其他配置 —— 本模块无需 `@Import`。加入依赖、保留一个 `DruidDataSource` Bean，
Boot 层的 `DruidDataSourceBeanPostProcessor` 与特性元数据即自动生效。

### 验证自动配置会退避

`AlibabaDruidCloudAutoConfigurationTest` 继承共享基类 `AutoConfigurationTest`，同时验证两道门控：

```java
class AlibabaDruidCloudAutoConfigurationTest extends AutoConfigurationTest<AlibabaDruidCloudAutoConfiguration> {

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues) {
        globalDisabledPropertyValues.add("microsphere.alibaba.druid.enabled=false");
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses) {
        globalMissingClasses.add(DruidDataSource.class);
    }
}
```

## 配置属性

本模块未新增属性，它复用 Boot 层的属性以及 Spring Cloud 的端点开关：

| 属性 | 类型 | 默认值 | 定义位置 | 作用 |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true`（`matchIfMissing = true`） | `PropertyConstants`（core）/ `AlibabaDruidProperties` | 经 `@ConditionalOnAlibabaDruidAvailable` 门控 `AlibabaDruidCloudAutoConfiguration` |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter` | 决定注入每个 `DruidDataSource` 的 filter 类型（来自 Boot 层） |
| `spring.cloud.features.enabled` | `boolean` | Spring Cloud 默认值 | Spring Cloud Commons | 启用 `FeaturesEndpoint`，从而发布 `features.yaml` 中的条目 |
| `management.endpoints.web.exposure.include` | `String`（列表） | — | Spring Boot Actuator | 需要包含 `features` 才能暴露 `/actuator/features` |

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `1.x`（由 `release-1.x` 分支生成） |
| 最新已发布版本 | `0.1.21`（`0.1.22-SNAPSHOT` 是分支上未发布的开发版） |
| Javadoc `@since` | `AlibabaDruidCloudAutoConfiguration` 为 `1.0.0` |
| Java 基线 | `8` —— 继承自外部父 POM 链（`microsphere-spring-cloud-parent:0.1.26` → `microsphere-build:0.3.14`），**本仓库未声明** |
| CI JDK 矩阵 | 8 / 11 / 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`，由 `microsphere-alibaba-druid-parent` 管理 |
| 自动配置注册方式 | `spring.factories` + `AutoConfiguration.imports` |

## 兼容范围

| 维度 | `1.x` 文档线支持情况 |
|---|---|
| Alibaba Druid | `1.2.28`（由父 POM 管理） |
| Java | 8 及以上 |
| Spring Cloud | `Hoxton.SR12`、`2020.0.5`、`2021.0.9`（profile `spring-cloud-hoxton` / `spring-cloud-2020` / `spring-cloud-2021`） |
| Spring Boot | `2.3.12.RELEASE`、`2.4.13`、`2.6.15` |
| Spring Cloud Commons | 使用 `FeaturesEndpoint` / `HasFeatures` 时需要；它是 optional 依赖，若要用 features 端点请自行声明 |
| Microsphere Spring Cloud | `microsphere-spring-cloud-commons:0.1.26`（来自导入的 BOM）—— `features.yaml` 机制所必需 |

## 与 current 文档线的差异

Java API 与 `features.yaml` 完全相同；本文档线上该自动配置额外通过 `META-INF/spring.factories` 注册以供
Boot 2.x 发现，制品面向 JDK 8，Spring Cloud 矩阵是 `Hoxton` – `2021.0.x` 而非 `2022.0.x` – `2025.0.x`。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
- [项目 README](../../../../README.md)
- [current 文档线的同名文档](../../../current/user-guide/zh/microsphere-alibaba-druid-spring-cloud.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-spring-cloud.md)

## English

- [English version of this document](../microsphere-alibaba-druid-spring-cloud.md)
