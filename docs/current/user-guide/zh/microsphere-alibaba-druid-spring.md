# microsphere-alibaba-druid-spring

[English](../microsphere-alibaba-druid-spring.md)

## 模块用途

`microsphere-alibaba-druid-spring` 将 Alibaba Druid 的 `Filter` 支持引入原生 Spring Framework 容器，提供：

- `@EnableAlibabaDruid` —— 一个注解即可让整个应用上下文启用 Druid `Filter`。
- `AlibabaDruidRegistrar` —— 注解背后的（包级私有）`ImportBeanDefinitionRegistrar`：负责注册从
  `META-INF/spring.factories` 与 Java `ServiceLoader` 中发现的 `Filter` Bean，以及
  `DruidDataSourceBeanPostProcessor`。
- `DruidDataSourceBeanPostProcessor` —— 一个 `BeanPostProcessor`，在每个 `DruidDataSource` Bean **初始化之前**把匹配
  的 `Filter` Bean 注入其 `getProxyFilters()`。

若在原生 Spring Framework（无 Spring Boot）环境下使用请选择本模块；使用 Boot 时更推荐
[microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)，它会自行注册同一个后置处理器。

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
| 版本 | 由 `microsphere-alibaba-druid-dependencies` BOM 管理（`current` 文档线为 `0.2.21`） |
| 打包方式 | `jar` |
| 编译依赖 | `microsphere-alibaba-druid-core`（非可选） |
| 可选依赖 | `microsphere-annotation-processor`、`microsphere-spring-context`、`com.alibaba:druid`、`spring-beans`、`spring-context`、`spring-test` |

导入一次 BOM，之后无需再写版本号：

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

由于 `com.alibaba:druid`、`spring-beans`、`spring-context`、`microsphere-spring-context` 都声明为
`<optional>true</optional>`，**必须由应用自己声明它们** —— 只有 `microsphere-alibaba-druid-core` 会被传递引入。

## API 说明

### `io.microsphere.alibaba.druid.spring.context.annotation.EnableAlibabaDruid`

`public @interface EnableAlibabaDruid`（自 `1.0.0` 起）—— `@Target({TYPE, ANNOTATION_TYPE})`、`@Retention(RUNTIME)`、
`@Documented`、`@Import(AlibabaDruidRegistrar.class)`。

| 属性 | 类型 | 默认值 | 含义 |
|---|---|---|---|
| `filterClasses` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `{Filter.class}` | 要搜索的 `Filter` 类型；匹配的实例会被注册为 Spring Bean 并加入每个 `DruidDataSource`。`Filter.class` 表示“所有 filter 子类” |
| `sources` | `io.microsphere.spring.beans.BeanSource[]` | `{BEAN_FACTORY, SPRING_FACTORIES}` | 搜索 `Filter` 实例的来源：Spring `BeanFactory`、`META-INF/spring.factories`，或 Java `ServiceLoader`（`JAVA_SERVICE_PROVIDER`） |

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

（代码片段取自 `EnableAlibabaDruid` 的 Javadoc。）

### `io.microsphere.alibaba.druid.spring.context.annotation.AlibabaDruidRegistrar`

`class AlibabaDruidRegistrar extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableAlibabaDruid>` ——
**非 public**，只能通过 `@EnableAlibabaDruid` 激活（自 `1.0.0` 起）。

`registerBeanDefinitions(...)` 按配置的 `BeanSource` 执行的动作：

| 来源 | 注册的 Bean |
|---|---|
| `SPRING_FACTORIES` | 通过 `registerSpringFactoriesBeans(registry, filterClasses)` 注册 `META-INF/spring.factories` 中类型属于 `filterClasses` 的全部条目 |
| `JAVA_SERVICE_PROVIDER` | 每个 `filterClass` 的 `ServiceLoader` 实现 —— 带 `com.alibaba.druid.filter.AutoLoad` 注解的类会被**跳过**，因为 Druid 自己就会把它们加载进 `DruidDataSource` |
| `BEAN_FACTORY` / default | `DruidDataSourceBeanPostProcessor` Bean，固定名称 `druidDataSourceBeanPostProcessor`，构造参数为 `filterClasses` |

> 关于 source 分支的说明：`registerFilterBeans` 里的 `switch` 没有 `break`，因此列出 `SPRING_FACTORIES` 时会继续执行
> `JAVA_SERVICE_PROVIDER` 与 `BEAN_FACTORY` 分支。实际效果是：无论 `sources` 取什么值，最终都会注册
> `DruidDataSourceBeanPostProcessor`，这正是 filter 注入得以生效的原因。

### `io.microsphere.alibaba.druid.spring.beans.factory.config.DruidDataSourceBeanPostProcessor`

`public class DruidDataSourceBeanPostProcessor extends GenericBeanPostProcessorAdapter<DruidDataSource> implements
BeanFactoryAware`（自 `1.0.0` 起）。

| 成员 | 签名 | 语义 |
|---|---|---|
| `BEAN_NAME` | `public static final String BEAN_NAME = "druidDataSourceBeanPostProcessor"` | `AlibabaDruidRegistrar` 与 Spring Boot 自动配置共同使用的 Bean 名称 |
| 构造器 | `public DruidDataSourceBeanPostProcessor()` | 把**所有** `Filter` Bean 加入每个 `DruidDataSource`；等价于 `new DruidDataSourceBeanPostProcessor(new Class[]{Filter.class})` |
| 构造器 | `public DruidDataSourceBeanPostProcessor(Class<? extends Filter>[] filterBeanClasses)` | 只加入指定类型的 `Filter` Bean；在 `TRACE` 级别打印该类型列表 |
| `processBeforeInitialization` | `protected void processBeforeInitialization(DruidDataSource druidDataSource, String beanName) throws BeansException` | 在每个 `DruidDataSource` Bean 初始化前被调用，转交给 `initializeFilterBeans(...)` |
| `initializeFilterBeans` | `protected void initializeFilterBeans(DruidDataSource druidDataSource)` | 针对每个配置的类型调用 `getSortedBeans(beanFactory, filterBeanClass)` 收集 Bean，用 `AnnotationAwareOrderComparator.sort(...)` 排序，`TRACE` 级别记录日志后追加到 `druidDataSource.getProxyFilters()` |
| `setBeanFactory` | `public void setBeanFactory(BeanFactory beanFactory) throws BeansException` | 提供查找 filter 所用的 `BeanFactory` |

filter 是在 `DruidDataSource.init()` 执行**之前**追加到 `getProxyFilters()` 的，因此 Druid 会用自己的 filter 生命周期
初始化它们（`Filter.init(DataSourceProxy)`），而这正是 `AbstractStatementFilter` 所依赖的。

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

只注册后置处理器（`DruidDataSourceBeanPostProcessorTest` 的做法）无需注解：

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

### 通过 Java `ServiceLoader` 注册 filter

`src/main/resources/META-INF/services/io.microsphere.alibaba.druid.filter.AbstractStatementFilter`：

```text
io.microsphere.alibaba.druid.filter.AutoLoadFilter
io.microsphere.alibaba.druid.filter.LoadFilter
```

配套实现：

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

带 `@AutoLoad` 的 `AutoLoadFilter`（测试源码）**不会**被注册为 Spring Bean —— Druid 会自行把它加载进数据源。同一包下没有
该注解的 `LoadFilter` 则会被注册并注入。

### 手工注册后置处理器

```java
// Typically registered via @EnableAlibabaDruid; can also be registered manually:
@Bean
public DruidDataSourceBeanPostProcessor druidDataSourceBeanPostProcessor() {
    return new DruidDataSourceBeanPostProcessor(new Class[]{ LoggingStatementFilter.class });
}
```

（代码片段取自 `DruidDataSourceBeanPostProcessor` 的 Javadoc。）

## 配置属性

本模块**不**绑定任何外部化属性 —— 全部通过 `@EnableAlibabaDruid` 的属性配置。`microsphere.alibaba.druid.*` 属性定义在
core 模块，并在 Spring Boot 模块完成绑定：

| 属性 | 类型 | 默认值 | 定义位置 | 对本模块的影响 |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true`（`matchIfMissing = true`） | `PropertyConstants`（core） | 本模块不读取 —— 该开关只作用于 Spring Boot 自动配置 |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter`（spring-boot） | 作为 `DruidDataSourceBeanPostProcessor` 的 `filterBeanClasses` 构造参数 |

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `current`（release 分支） |
| 最新已发布版本 | `0.2.21`（`main` 上的 `0.2.22-SNAPSHOT` 是未发布的开发版） |
| Javadoc `@since` | `EnableAlibabaDruid`、`AlibabaDruidRegistrar`、`DruidDataSourceBeanPostProcessor` 均为 `1.0.0` |
| Java | 17+（根 `pom.xml` 中 `<java.version>17</java.version>`） |
| CI JDK 矩阵 | 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`，由 `microsphere-alibaba-druid-parent` 管理 |

## 兼容范围

| 维度 | `current` 文档线支持情况 |
|---|---|
| Alibaba Druid | `1.2.28`（由父 POM 管理） |
| Java | 17、21、25（要求 JDK 17+） |
| Spring Framework | 由引入的 `microsphere-spring-cloud-dependencies` / Boot 版本线决定，即 Spring `6.0.x` – `6.2.x` |
| Spring Boot / Spring Cloud | `3.0.13`/`2022.0.5`、`3.3.13`/`2023.0.6`、`3.4.13`/`2024.0.3`、`3.5.15`/`2025.0.3`，即 Spring Cloud `2022.0.x` – `2025.0.x`；这些版本**继承自外部父 POM 链** `microsphere-spring-cloud-parent:0.2.26`，并非本仓库声明 |
| 本模块的框架要求 | Spring Framework（`spring-beans`、`spring-context`）加 `microsphere-spring-context`；**不**需要 Spring Boot |

仍在使用 Spring Cloud `Hoxton` – `2021.0.x` / JDK 8？请查看 [1.x 用户文档](../../../1.x/user-guide/README.md)。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md)
- [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)
- [项目 README](../../../../README.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-spring.md)

## English

- [English version of this document](../microsphere-alibaba-druid-spring.md)
