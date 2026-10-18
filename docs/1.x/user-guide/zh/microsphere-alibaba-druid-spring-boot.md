# microsphere-alibaba-druid-spring-boot（`1.x`）

[English](../microsphere-alibaba-druid-spring-boot.md)

## 模块用途

`microsphere-alibaba-druid-spring-boot` 在 `1.x` 文档线上把 Spring 集成变成 Spring Boot 体验，提供：

- `AlibabaDruidAutoConfiguration` —— 注册 `DruidDataSourceBeanPostProcessor`（Bean 名
  `druidDataSourceBeanPostProcessor`）以及面向 Druid 的 `DataSourcePoolMetadataProvider`。
- `AlibabaDruidProperties` —— `microsphere.alibaba.druid.*` 的 `@ConfigurationProperties` 绑定，含嵌套的
  `Filter` 类型及其 `classes` 数组。
- `@ConditionalOnAlibabaDruidEnabled` / `@ConditionalOnAlibabaDruidAvailable` —— 属性开关与类路径门控。
- `DruidDataSourcePoolMetadata` —— 把 Druid 连接池计数暴露给 Actuator 的适配器
  （`usage`、`active`、`idle`、`max`、`min`、`validationQuery`、`defaultAutoCommit`）。

**本文档线的特有之处：** 该模块同时提供*两份*自动配置发现文件，因此同一个 jar 可用于 Boot 2.3 直到 Boot 3.x。

## Maven 坐标

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-spring-boot</artifactId>
</dependency>
```

| 项目 | 值 |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-spring-boot` |
| 版本 | 由 `microsphere-alibaba-druid-dependencies` BOM 管理（`1.x` 文档线为 `0.1.21`） |
| 打包方式 | `jar` |
| compile 依赖 | `microsphere-alibaba-druid-spring`（非 optional，传递引入 `core`） |
| optional 依赖 | `microsphere-annotation-processor`、`microsphere-spring-boot-core`、`com.alibaba:druid`、`spring-boot-starter`、`spring-boot-starter-actuator`、`spring-boot-configuration-processor`、`spring-boot-autoconfigure-processor` |

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

implementation("io.github.microsphere-projects:microsphere-alibaba-druid-spring-boot")
```

Spring Boot 本身是 optional 依赖，因此需要你自己添加 `spring-boot-starter`（若需要连接池指标再加
`spring-boot-starter-actuator`）以及 `com.alibaba:druid`。

## 双重自动配置注册（仅 1.x）

`src/main/resources/META-INF/spring.factories` —— Boot 2.x 的注册方式：

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.alibaba.druid.spring.boot.autoconfigure.AlibabaDruidAutoConfiguration
```

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` —— 同样保留：

```text
io.microsphere.alibaba.druid.spring.boot.autoconfigure.AlibabaDruidAutoConfiguration
```

| Boot 版本 | 实际读取的文件 | 效果 |
|---|---|---|
| 2.3 – 2.6（本文档线的矩阵） | `spring.factories` | `AutoConfiguration.imports` 不生效 |
| 2.7 | 两份都读，由 `AutoConfigurationImportSelector` 去重 | 只注册一次 |
| 3.x | `AutoConfiguration.imports` | `spring.factories` 中的 `EnableAutoConfiguration` 键被忽略 |

无需任何额外配置：由于存在 `spring.factories`，Boot 2.3–2.6 应用会自动发现该自动配置，
与 `current` 文档线仅靠 `AutoConfiguration.imports` 的效果一致。

## API 说明

### `io.microsphere.alibaba.druid.spring.boot.autoconfigure.AlibabaDruidAutoConfiguration`

`public class AlibabaDruidAutoConfiguration`（自 `1.0.0` 起），带
`@ConditionalOnAlibabaDruidAvailable` 与 `@EnableConfigurationProperties(AlibabaDruidProperties.class)`。

| Bean 方法 | 签名 | 说明 |
|---|---|---|
| `druidDataSourceBeanPostProcessor` | `@Bean(name = BEAN_NAME) public BeanPostProcessor druidDataSourceBeanPostProcessor(AlibabaDruidProperties alibabaDruidProperties)` | 返回 `new DruidDataSourceBeanPostProcessor(alibabaDruidProperties.getFilter().getClasses())`。声明返回类型为 `BeanPostProcessor`，Bean 名取自常量 `DruidDataSourceBeanPostProcessor.BEAN_NAME`。 |
| `druidDataSourcePoolMetadataProvider` | `@Bean @ConditionalOnBean(DruidDataSource.class) public DataSourcePoolMetadataProvider druidDataSourcePoolMetadataProvider()` | Lambda：`DataSourceUnwrapper.unwrap(dataSource, DruidDataSourceMBean.class, DruidDataSource.class)`；解包成功返回 `new DruidDataSourcePoolMetadata(druidDataSource)`，否则返回 `null`。 |

### `io.microsphere.alibaba.druid.spring.boot.AlibabaDruidProperties`

`@ConfigurationProperties(prefix = ALIBABA_DRUID_PROPERTY_NAME_PREFIX) public class AlibabaDruidProperties`
（自 `1.0.0` 起）—— 前缀 `microsphere.alibaba.druid`。

| 成员 | 类型 | 默认值 | 属性 |
|---|---|---|---|
| `isEnabled()` / `setEnabled(boolean)` | `boolean` | `true` | `microsphere.alibaba.druid.enabled` |
| `getFilter()` / `setFilter(Filter)` | `AlibabaDruidProperties.Filter` | `new Filter()` | ——（嵌套组） |
| `Filter.getClasses()` / `Filter.setClasses(...)` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `of(com.alibaba.druid.filter.Filter.class)` | `microsphere.alibaba.druid.filter.classes` |

`Filter` 是嵌套的 `public static class AlibabaDruidProperties.Filter`；默认值 `{ Filter.class }` 表示
“注入上下文中找到的每一个 `Filter` Bean”。

### `io.microsphere.alibaba.druid.spring.boot.condition.ConditionalOnAlibabaDruidEnabled`

`public @interface ConditionalOnAlibabaDruidEnabled`（自 `1.0.0` 起）—— `@Target(TYPE)`、`@Retention(RUNTIME)`，
元注解 `@ConditionalOnProperty(name = ALIBABA_DRUID_ENABLED_PROPERTY_NAME, matchIfMissing = true)`。
该模块的 Javadoc 里写过 `"microsphere.spring.boot.alibaba.druid.enabled"` 这个名字，实际使用的常量是
`microsphere.alibaba.druid.enabled` —— 请以常量为准。

### `io.microsphere.alibaba.druid.spring.boot.condition.ConditionalOnAlibabaDruidAvailable`

`public @interface ConditionalOnAlibabaDruidAvailable`（自 `1.0.0` 起）—— 组合门控
`@ConditionalOnAlibabaDruidEnabled` + `@ConditionalOnClass(name = "com.alibaba.druid.pool.DruidDataSource")`。
Boot 与 Cloud 两个自动配置都复用它，因此把 `com.alibaba:druid` 移出类路径（或设置
`microsphere.alibaba.druid.enabled=false`）即可零代码地关闭全部能力。

### `io.microsphere.alibaba.druid.spring.boot.metadata.DruidDataSourcePoolMetadata`

`public class DruidDataSourcePoolMetadata implements DataSourcePoolMetadata`（自 `1.0.0` 起）。

| 方法 | Druid 数据来源 | 返回值 |
|---|---|---|
| `getUsage()` | `getActiveCount()` / `getMaxActive()` | 无活跃连接时为 `0.0f`；`active == maxActive` 时为 `1.0f`；否则 `active / (maxActive * 1.0f)` |
| `getActive()` | `getActiveCount()` | 使用中的连接数 |
| `getIdle()` | `getMaxIdle()` | 最大空闲连接数 |
| `getMax()` | `getMaxActive()` | 最大活跃连接数 |
| `getMin()` | `getMinIdle()` | 最小空闲连接数 |
| `getValidationQuery()` | `getValidationQuery()` | 可能为 `null` |
| `getDefaultAutoCommit()` | `isDefaultAutoCommit()` | 自动提交标志 |

## 使用示例

### 在 `application.properties` 中启用

`AlibabaDruidAutoConfigurationIntegrationTest`（测试源码）即以 `@EnableAutoConfiguration` 启动，配置如下：

```java
@SpringBootTest(
        classes = {
                LoggingStatementFilter.class,
                DruidDataSourceTestConfiguration.class,
                AlibabaDruidAutoConfigurationIntegrationTest.class
        },
        webEnvironment = NONE,
        properties = {
                "microsphere.alibaba.druid.enabled=true",
                "microsphere.alibaba.druid.filter.classes=io.microsphere.alibaba.druid.filter.LoggingStatementFilter"
        })
@EnableAutoConfiguration
public class AlibabaDruidAutoConfigurationIntegrationTest extends AbstractDruidSpringTest {
}
```

随后断言绑定结果与 metadata provider：

```java
Class<? extends com.alibaba.druid.filter.Filter>[] classes = alibabaDruidProperties.getFilter().getClasses();
assertArrayEquals(of(LoggingStatementFilter.class), classes);

DataSourcePoolMetadata dataSourcePoolMetadata = dataSourcePoolMetadataProvider.getDataSourcePoolMetadata(dataSource);
assertInstanceOf(DruidDataSourcePoolMetadata.class, dataSourcePoolMetadata);
```

### 配置多个 filter

```properties
microsphere.alibaba.druid.enabled=true
microsphere.alibaba.druid.filter.classes=\
io.microsphere.alibaba.druid.filter.LoggingStatementFilter,\
com.acme.druid.MetricsStatementFilter
```

列出的每个类型都会在 `BeanFactory` 中查找；匹配到的 Bean 按 `AnnotationAwareOrderComparator` 排序，
并在 `init()` 之前追加到每个 `DruidDataSource`。

### Actuator 连接池指标

`spring-boot-starter-actuator` 是 optional 依赖，需显式添加，然后暴露端点：

```properties
management.endpoints.web.exposure.include=health,metrics
```

Druid 数据源的连接池 gauge 正是由 `DruidDataSourcePoolMetadata` 提供。默认 H2 测试数据源下的取值由
`DruidDataSourcePoolMetadataTest` 验证：

```java
assertNotNull(this.metadata.getUsage());
assertEquals(valueOf(0), this.metadata.getActive());
assertEquals(valueOf(8), this.metadata.getIdle());
assertEquals(valueOf(8), this.metadata.getMax());
assertEquals(valueOf(0), this.metadata.getMin());
assertNull(this.metadata.getValidationQuery());
assertTrue(this.metadata.getDefaultAutoCommit());
```

### 关闭自动配置

设置 `microsphere.alibaba.druid.enabled=false` 会让 `@ConditionalOnAlibabaDruidAvailable`（进而两个自动配置）退避。
`AlibabaDruidAutoConfigurationTest` 借助共享基类 `AutoConfigurationTest` 同时验证了属性开关与类路径门控：

```java
class AlibabaDruidAutoConfigurationTest extends AutoConfigurationTest<AlibabaDruidAutoConfiguration> {

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
        autoConfiguredClasses.add(DruidDataSourceBeanPostProcessor.class);
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

| 属性 | 类型 | 默认值 | 绑定位置 | 作用 |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true` | `AlibabaDruidProperties.enabled`、`@ConditionalOnAlibabaDruidEnabled` | `AlibabaDruidAutoConfiguration` 与 `AlibabaDruidCloudAutoConfiguration` 的总开关（`matchIfMissing = true`） |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter.classes` | 决定哪些 filter Bean 类型会被注入每个 `DruidDataSource` |

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `1.x`（由 `release-1.x` 分支生成） |
| 最新已发布版本 | `0.1.21`（`0.1.22-SNAPSHOT` 是分支上未发布的开发版） |
| Javadoc `@since` | `AlibabaDruidAutoConfiguration`、`AlibabaDruidProperties`、两个条件注解、`DruidDataSourcePoolMetadata` 均为 `1.0.0` |
| Java 基线 | `8` —— 继承自外部父 POM 链（`microsphere-spring-cloud-parent:0.1.26` → `microsphere-build:0.3.14`），**本仓库未声明** |
| CI JDK 矩阵 | 8 / 11 / 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`，由 `microsphere-alibaba-druid-parent` 管理 |
| 自动配置注册方式 | `spring.factories` + `AutoConfiguration.imports`（见上文） |

## 兼容范围

| 维度 | `1.x` 文档线支持情况 |
|---|---|
| Alibaba Druid | `1.2.28`（由父 POM 管理） |
| Java | 8 及以上 |
| Spring Boot | `2.3.12.RELEASE`、`2.4.13`、`2.6.15`（profile `spring-cloud-hoxton` / `spring-cloud-2020` / `spring-cloud-2021`） |
| Spring Cloud | `Hoxton.SR12` – `2021.0.9` |
| Spring Boot Actuator | 可选 —— 只有在需要 `DruidDataSourcePoolMetadata` 指标时才需要 |
| 类路径上的 Alibaba Druid | 必需，否则 `@ConditionalOnClass` 门控不通过 |

上述 Boot/Cloud 版本全部继承自 `microsphere-spring-cloud-parent:0.1.26`，本仓库并未声明。

## 与 current 文档线的差异

Java API 相同；本文档线上该自动配置*额外*通过 `META-INF/spring.factories` 注册，以便被 Spring Boot 2.3–2.6 发现，
制品面向 JDK 8 构建，且验证矩阵是 Boot 2 的 `Hoxton` – `2021.0.x` 而非 Boot 3 的 `2022.0.x` – `2025.0.x`。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-cloud.md](microsphere-alibaba-druid-spring-cloud.md)
- [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)
- [项目 README](../../../../README.md)
- [current 文档线的同名文档](../../../current/user-guide/zh/microsphere-alibaba-druid-spring-boot.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-spring-boot.md)

## English

- [English version of this document](../microsphere-alibaba-druid-spring-boot.md)
