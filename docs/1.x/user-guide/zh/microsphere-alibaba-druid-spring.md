# microsphere-alibaba-druid-spring（`1.x`）

[English](../microsphere-alibaba-druid-spring.md)

## 模块用途

`microsphere-alibaba-druid-spring` 在 `1.x` 文档线上把 Alibaba Druid 的 `Filter` 能力带入普通 Spring Framework
容器，提供：

- `@EnableAlibabaDruid` —— 一个注解开关，为整个应用上下文开启 Druid `Filter` Bean。
- `AlibabaDruidRegistrar` —— 注解背后的（包级私有）`ImportBeanDefinitionRegistrar`：注册从
  `META-INF/spring.factories` 与 Java `ServiceLoader` 发现的 `Filter` Bean，并注册
  `DruidDataSourceBeanPostProcessor`。
- `DruidDataSourceBeanPostProcessor` —— 一个 `BeanPostProcessor`，在每个 `DruidDataSource` Bean 初始化*之前*
  把匹配的 `Filter` Bean 注入（`getProxyFilters()`）。

如果你只使用 Spring Framework 而没有 Spring Boot，请使用本模块。使用 Boot 时更推荐
[microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)，它会自动注册同一个
后置处理器。

## Maven 坐标

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-spring</artifactId>
</dependency>
```

| 项目 | 值 |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-spring` |
| 版本 | 由 `microsphere-alibaba-druid-dependencies` BOM 管理（`1.x` 文档线为 `0.1.21`） |
| 打包方式 | `jar` |
| compile 依赖 | `microsphere-alibaba-druid-core`（非 optional） |
| optional 依赖 | `microsphere-annotation-processor`、`microsphere-spring-context`、`com.alibaba:druid`、`spring-beans`、`spring-context`、`spring-test` |

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

implementation("io.github.microsphere-projects:microsphere-alibaba-druid-spring")
```

由于 `com.alibaba:druid`、`spring-beans`、`spring-context`、`microsphere-spring-context` 都声明为
`<optional>true</optional>`，**必须由应用自己声明它们** —— 只有 `microsphere-alibaba-druid-core` 会被传递引入。

## API 说明

### `io.microsphere.alibaba.druid.spring.context.annotation.EnableAlibabaDruid`

`public @interface EnableAlibabaDruid`（自 `1.0.0` 起）—— `@Target({TYPE, ANNOTATION_TYPE})`、
`@Retention(RUNTIME)`、`@Documented`、`@Import(AlibabaDruidRegistrar.class)`。

| 属性 | 类型 | 默认值 | 含义 |
|---|---|---|---|
| `filterClasses` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `{Filter.class}` | 要查找的 `Filter` 类型；匹配到的实例会被注册为 Spring Bean 并加入每个 `DruidDataSource`。写 `Filter.class` 表示“所有 filter 子类型”。 |
| `sources` | `io.microsphere.spring.beans.BeanSource[]` | `{BEAN_FACTORY, SPRING_FACTORIES}` | 查找 `Filter` 实例的来源：Spring `BeanFactory`、`META-INF/spring.factories`，或 Java `ServiceLoader`（`JAVA_SERVICE_PROVIDER`）。 |

```java
@Configuration
@EnableAlibabaDruid(filterClasses = LoggingStatementFilter.class)
public class AppConfig {

    @Bean(initMethod = "init", destroyMethod = "close")
    public DruidDataSource dataSource() {
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl("jdbc:h2:mem:test");
        ds.setDriverClassName("org.h2.Driver");
        return ds;
    }
}
```

（*片段取自 `EnableAlibabaDruid` 的 Javadoc。*）

### `io.microsphere.alibaba.druid.spring.context.annotation.AlibabaDruidRegistrar`

`class AlibabaDruidRegistrar extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableAlibabaDruid>` ——
**非 public**，只能通过 `@EnableAlibabaDruid` 激活（自 `1.0.0` 起）。

`registerBeanDefinitions(...)` 针对每个已配置 `BeanSource` 的行为：

| 来源 | 注册的 Bean |
|---|---|
| `SPRING_FACTORIES` | 通过 `registerSpringFactoriesBeans(registry, filterClasses)` 注册 `META-INF/spring.factories` 中类型属于 `filterClasses` 的每一项 |
| `JAVA_SERVICE_PROVIDER` | 每个 `filterClass` 的 `ServiceLoader` 实现 —— 带 `com.alibaba.druid.filter.AutoLoad` 注解的类会被**跳过**，因为 Druid 自己就会把它们加载进 `DruidDataSource` |
| `BEAN_FACTORY` / default | 以固定名称 `druidDataSourceBeanPostProcessor` 注册 `DruidDataSourceBeanPostProcessor` Bean，并把 `filterClasses` 作为构造参数 |

> 关于该 switch 的说明：`registerFilterBeans` 中的 `switch` 没有 `break`，因此列出 `SPRING_FACTORIES` 时也会执行
> `JAVA_SERVICE_PROVIDER` 与 `BEAN_FACTORY` 分支。实践中任何 `sources` 取值都会注册
> `DruidDataSourceBeanPostProcessor`，而这正是 filter 注入生效的关键。

### `io.microsphere.alibaba.druid.spring.beans.factory.config.DruidDataSourceBeanPostProcessor`

`public class DruidDataSourceBeanPostProcessor extends GenericBeanPostProcessorAdapter<DruidDataSource> implements
BeanFactoryAware`（自 `1.0.0` 起）。

| 成员 | 签名 | 语义 |
|---|---|---|
| `BEAN_NAME` | `public static final String BEAN_NAME = "druidDataSourceBeanPostProcessor"` | `AlibabaDruidRegistrar` 与 Spring Boot 自动配置共同使用的 Bean 名 |
| 构造器 | `public DruidDataSourceBeanPostProcessor()` | 把*所有* `Filter` Bean 加到每个 `DruidDataSource`；等价于 `new DruidDataSourceBeanPostProcessor(new Class[]{Filter.class})` |
| 构造器 | `public DruidDataSourceBeanPostProcessor(Class<? extends Filter>[] filterBeanClasses)` | 只添加指定类型的 `Filter` Bean；在 `TRACE` 级别打印该列表 |
| `processBeforeInitialization` | `protected void processBeforeInitialization(DruidDataSource druidDataSource, String beanName) throws BeansException` | 每个 `DruidDataSource` Bean 初始化前调用；转交 `initializeFilterBeans(...)` |
| `initializeFilterBeans` | `protected void initializeFilterBeans(DruidDataSource druidDataSource)` | 对每个配置的类型调用 `getSortedBeans(beanFactory, filterBeanClass)` 收集，使用 `AnnotationAwareOrderComparator.sort(...)` 排序，在 `TRACE` 级别打日志，最后追加到 `druidDataSource.getProxyFilters()` |
| `setBeanFactory` | `public void setBeanFactory(BeanFactory beanFactory) throws BeansException` | 提供查找 filter 所用的 `BeanFactory` |

filter 是在 `DruidDataSource.init()` *之前*追加进 `getProxyFilters()` 的，因此 Druid 会按自己的 filter 生命周期
（`Filter.init(DataSourceProxy)`）初始化它们 —— 这正是 `AbstractStatementFilter` 所依赖的行为。

## 使用示例

### 把 filter 声明为 Bean 并启用注解

这就是 `EnableAlibabaDruidTest`（`microsphere-alibaba-druid-spring` 测试源码）的配置：

```java
@SpringJUnitConfig(classes = {
        LoggingStatementFilter.class,
        DruidDataSourceTestConfiguration.class,
        EnableAlibabaDruidTest.class,
})
@EnableAlibabaDruid(
        filterClasses = AbstractStatementFilter.class,
        sources = {
                BEAN_FACTORY,
                SPRING_FACTORIES,
                JAVA_SERVICE_PROVIDER
        }
)
public class EnableAlibabaDruidTest extends AbstractDruidSpringTest {
}
```

只注册后置处理器（`DruidDataSourceBeanPostProcessorTest` 的做法）不需要注解：

```java
@SpringLoggingTest
@ExtendWith(value = SpringExtension.class)
@ContextConfiguration(classes = {
        LoggingStatementFilter.class,
        DruidDataSourceTestConfiguration.class,
        DruidDataSourceBeanPostProcessor.class,
        DruidDataSourceBeanPostProcessorTest.class,
})
public class DruidDataSourceBeanPostProcessorTest extends AbstractDruidSpringTest {

}
```

### 通过 `META-INF/spring.factories` 注册 filter

key 是 filter 的父类型，value 是实现类（`microsphere-alibaba-druid-spring` 测试资源）：

```properties
io.microsphere.alibaba.druid.filter.AbstractStatementFilter=\
io.microsphere.alibaba.druid.filter.TestStatementFilter
```

> 在本文档线上 `META-INF/spring.factories` 有两种用途：上面这种是*用户*文件，由 `@EnableAlibabaDruid` 读取以发现
> filter；而 `spring-boot` / `spring-cloud` 模块自带的 `spring.factories` 则是为 Boot 2.x 注册自动配置。
> 不要把两者混淆。

### 通过 Java `ServiceLoader` 注册 filter

`src/test/resources/META-INF/services/io.microsphere.alibaba.druid.filter.AbstractStatementFilter`：

```text
io.microsphere.alibaba.druid.filter.AutoLoadFilter
io.microsphere.alibaba.druid.filter.LoadFilter
```

对应实现：

```java
@AutoLoad
public class AutoLoadFilter extends AbstractStatementFilter {
    @Override
    protected void beforeExecute(StatementProxy statement, String resourceName) throws Throwable {

    }

    @Override
    protected void afterExecute(StatementProxy statement, String resourceName, Object result, Throwable failure) {

    }
}
```

带 `@AutoLoad` 的 `AutoLoadFilter`（测试源码）**不会**被注册为 Spring Bean —— Druid 会自行把它加载进数据源。
同一包下没有该注解的 `LoadFilter` 则会被注册并注入。

### 手工注册后置处理器

```java
// Typically registered via @EnableAlibabaDruid; can also be registered manually:
@Bean
public DruidDataSourceBeanPostProcessor druidDataSourceBeanPostProcessor() {
    return new DruidDataSourceBeanPostProcessor(new Class[]{ LoggingStatementFilter.class });
}
```

（*片段取自 `DruidDataSourceBeanPostProcessor` 的 Javadoc。*）

## 配置属性

本模块**不**绑定任何外部化属性 —— 全部通过 `@EnableAlibabaDruid` 的属性配置。
`microsphere.alibaba.druid.*` 属性在 `core` 中定义、在 Spring Boot 模块中绑定：

| 属性 | 类型 | 默认值 | 定义位置 | 对本模块的影响 |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true`（`matchIfMissing = true`） | `PropertyConstants`（core） | 本模块不读取 —— 只有 Spring Boot 自动配置受其开关控制 |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter`（spring-boot） | 成为 `DruidDataSourceBeanPostProcessor` 的 `filterBeanClasses` 构造参数 |

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `1.x`（由 `release-1.x` 分支生成） |
| 最新已发布版本 | `0.1.21`（`0.1.22-SNAPSHOT` 是分支上未发布的开发版） |
| Javadoc `@since` | `EnableAlibabaDruid`、`AlibabaDruidRegistrar`、`DruidDataSourceBeanPostProcessor` 均为 `1.0.0` |
| Java 基线 | `8` —— 继承自外部父 POM 链（`microsphere-spring-cloud-parent:0.1.26` → `microsphere-build:0.3.14`），**本仓库未声明** |
| CI JDK 矩阵 | 8 / 11 / 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`，由 `microsphere-alibaba-druid-parent` 管理 |

## 兼容范围

| 维度 | `1.x` 文档线支持情况 |
|---|---|
| Alibaba Druid | `1.2.28`（由父 POM 管理） |
| Java | 8 及以上（JDK 8 源码级别；jar 同样可在 11/17/21/25 上运行） |
| Spring Framework | 由导入的 `microsphere-spring-cloud-dependencies:0.1.26` 提供，即 Spring `5.x`（Spring Boot 2.3 – 2.6 所附带的版本） |
| Spring Boot / Spring Cloud | `2.3.12.RELEASE`/`Hoxton.SR12`、`2.4.13`/`2020.0.5`、`2.6.15`/`2021.0.9`，即 Spring Cloud `Hoxton` – `2021.0.x`；继承自外部父 POM 链 `microsphere-spring-cloud-parent:0.1.26`，并非本仓库声明 |
| 本模块的框架要求 | Spring Framework（`spring-beans`、`spring-context`）以及 `microsphere-spring-context`；**不**要求 Spring Boot |

## 与 current 文档线的差异

Java API 完全一致，差异只在平台：本制品面向 JDK 8 与 Boot 2.3–2.6 / Cloud `Hoxton`–`2021.0.x` 矩阵构建，
并且其下游模块除 `AutoConfiguration.imports` 外还通过 `spring.factories` 注册自动配置。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md)
- [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)
- [项目 README](../../../../README.md)
- [current 文档线的同名文档](../../../current/user-guide/zh/microsphere-alibaba-druid-spring.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-spring.md)

## English

- [English version of this document](../microsphere-alibaba-druid-spring.md)
