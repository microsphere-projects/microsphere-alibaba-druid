# microsphere-alibaba-druid-spring

[中文](zh/microsphere-alibaba-druid-spring.md)

## Purpose

`microsphere-alibaba-druid-spring` brings Alibaba Druid's `Filter` support into the plain Spring Framework container.
It gives you:

- `@EnableAlibabaDruid` — a one-annotation switch that turns Druid `Filter` beans on for the whole application context.
- `AlibabaDruidRegistrar` — the (package-private) `ImportBeanDefinitionRegistrar` behind the annotation: it registers
  `Filter` beans discovered from `META-INF/spring.factories` and the Java `ServiceLoader`, plus the
  `DruidDataSourceBeanPostProcessor`.
- `DruidDataSourceBeanPostProcessor` — a `BeanPostProcessor` that injects every matching `Filter` bean into each
  `DruidDataSource` bean *before* it is initialized (`getProxyFilters()`).

Use this module when you are on Spring Framework without Spring Boot. With Boot, prefer
[microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md), which registers the same
post-processor automatically.

## Maven Coordinates

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-spring</artifactId>
</dependency>
```

| Item | Value |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-spring` |
| Version | managed by the `microsphere-alibaba-druid-dependencies` BOM (`0.2.21` on the `current` line) |
| Packaging | `jar` |
| Compile dependency | `microsphere-alibaba-druid-core` (non-optional) |
| Optional dependencies | `microsphere-annotation-processor`, `microsphere-spring-context`, `com.alibaba:druid`, `spring-beans`, `spring-context`, `spring-test` |

Import the BOM once, then omit versions:

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

Because `com.alibaba:druid`, `spring-beans`, `spring-context` and `microsphere-spring-context` are declared
`<optional>true</optional>`, **your application must declare them itself** — only `microsphere-alibaba-druid-core`
comes in transitively.

## API Reference

### `io.microsphere.alibaba.druid.spring.context.annotation.EnableAlibabaDruid`

`public @interface EnableAlibabaDruid` (since `1.0.0`) — `@Target({TYPE, ANNOTATION_TYPE})`, `@Retention(RUNTIME)`,
`@Documented`, `@Import(AlibabaDruidRegistrar.class)`.

| Attribute | Type | Default | Meaning |
|---|---|---|---|
| `filterClasses` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `{Filter.class}` | The `Filter` types to search for; matching instances are registered as Spring beans and added into every `DruidDataSource`. `Filter.class` means "all filter subtypes". |
| `sources` | `io.microsphere.spring.beans.BeanSource[]` | `{BEAN_FACTORY, SPRING_FACTORIES}` | Where to search the `Filter` instances: Spring `BeanFactory`, `META-INF/spring.factories`, or the Java `ServiceLoader` (`JAVA_SERVICE_PROVIDER`). |

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

*(Snippet taken from the `EnableAlibabaDruid` Javadoc.)*

### `io.microsphere.alibaba.druid.spring.context.annotation.AlibabaDruidRegistrar`

`class AlibabaDruidRegistrar extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableAlibabaDruid>` — **not
public**, it is activated only through `@EnableAlibabaDruid` (since `1.0.0`).

What `registerBeanDefinitions(...)` does, per configured `BeanSource`:

| Source | Registered beans |
|---|---|
| `SPRING_FACTORIES` | Every `META-INF/spring.factories` entry whose type is one of the `filterClasses`, via `registerSpringFactoriesBeans(registry, filterClasses)` |
| `JAVA_SERVICE_PROVIDER` | Every `ServiceLoader` implementation of each `filterClass` — classes annotated with `com.alibaba.druid.filter.AutoLoad` are **skipped**, because Druid already loads them into `DruidDataSource` by itself |
| `BEAN_FACTORY` / default | The `DruidDataSourceBeanPostProcessor` bean, under the fixed name `druidDataSourceBeanPostProcessor`, constructed with the `filterClasses` argument |

> Note on the source switch: the `switch` in `registerFilterBeans` has no `break` statements, so listing
> `SPRING_FACTORIES` also executes the `JAVA_SERVICE_PROVIDER` and `BEAN_FACTORY` branches. In practice any `sources`
> value ends up registering the `DruidDataSourceBeanPostProcessor`, which is what makes filter injection work.

### `io.microsphere.alibaba.druid.spring.beans.factory.config.DruidDataSourceBeanPostProcessor`

`public class DruidDataSourceBeanPostProcessor extends GenericBeanPostProcessorAdapter<DruidDataSource> implements
BeanFactoryAware` (since `1.0.0`).

| Member | Signature | Semantics |
|---|---|---|
| `BEAN_NAME` | `public static final String BEAN_NAME = "druidDataSourceBeanPostProcessor"` | The bean name used by `AlibabaDruidRegistrar` and by the Spring Boot auto-configuration |
| constructor | `public DruidDataSourceBeanPostProcessor()` | Adds *all* `Filter` beans to every `DruidDataSource`; equivalent to `new DruidDataSourceBeanPostProcessor(new Class[]{Filter.class})` |
| constructor | `public DruidDataSourceBeanPostProcessor(Class<? extends Filter>[] filterBeanClasses)` | Adds only the `Filter` beans of the given types; logs the list at `TRACE` |
| `processBeforeInitialization` | `protected void processBeforeInitialization(DruidDataSource druidDataSource, String beanName) throws BeansException` | Called for each `DruidDataSource` bean before initialization; delegates to `initializeFilterBeans(...)` |
| `initializeFilterBeans` | `protected void initializeFilterBeans(DruidDataSource druidDataSource)` | Collects `getSortedBeans(beanFactory, filterBeanClass)` for every configured class, sorts them with `AnnotationAwareOrderComparator.sort(...)`, logs at `TRACE` and appends them to `druidDataSource.getProxyFilters()` |
| `setBeanFactory` | `public void setBeanFactory(BeanFactory beanFactory) throws BeansException` | Supplies the `BeanFactory` used for the filter lookup |

Filters are appended to `getProxyFilters()` **before** `DruidDataSource.init()` runs, so Druid initializes them through
its own filter lifecycle (`Filter.init(DataSourceProxy)`), which is what `AbstractStatementFilter` relies on.

## Usage

### Declare filters as beans and enable the annotation

This is the setup of `EnableAlibabaDruidTest` (`microsphere-alibaba-druid-spring` test sources):

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

Registering the post-processor on its own (as `DruidDataSourceBeanPostProcessorTest` does) needs no annotation:

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

### Register filters through `META-INF/spring.factories`

The key is the filter supertype, the values are the implementation classes
(`microsphere-alibaba-druid-spring` test resources):

```properties
io.microsphere.alibaba.druid.filter.AbstractStatementFilter=\
io.microsphere.alibaba.druid.filter.TestStatementFilter
```

### Register filters through the Java `ServiceLoader`

`src/main/resources/META-INF/services/io.microsphere.alibaba.druid.filter.AbstractStatementFilter`:

```text
io.microsphere.alibaba.druid.filter.AutoLoadFilter
io.microsphere.alibaba.druid.filter.LoadFilter
```

with:

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

`AutoLoadFilter` (test source) is *not* registered as a Spring bean because of `@AutoLoad` — Druid loads it into the
data source on its own. `LoadFilter` (same package, no annotation) is registered and injected.

### Register the post-processor manually

```java
// Typically registered via @EnableAlibabaDruid; can also be registered manually:
@Bean
public DruidDataSourceBeanPostProcessor druidDataSourceBeanPostProcessor() {
    return new DruidDataSourceBeanPostProcessor(new Class[]{ LoggingStatementFilter.class });
}
```

*(Snippet taken from the `DruidDataSourceBeanPostProcessor` Javadoc.)*

## Configuration Properties

This module binds **no** externalized properties — everything is configured through the `@EnableAlibabaDruid`
attributes. The `microsphere.alibaba.druid.*` properties are defined in `core` and bound in the Spring Boot module:

| Property | Type | Default | Defined by | Effect on this module |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true` (`matchIfMissing = true`) | `PropertyConstants` (core) | Not read here — only the Spring Boot auto-configuration is switched by it |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter` (spring-boot) | Becomes the `filterBeanClasses` constructor argument of `DruidDataSourceBeanPostProcessor` |

## Version Information

| Item | Value |
|---|---|
| Documentation line | `current` (release branch) |
| Latest released version | `0.2.21` (`0.2.22-SNAPSHOT` is the unpublished development version on `main`) |
| Since (Javadoc) | `1.0.0` for `EnableAlibabaDruid`, `AlibabaDruidRegistrar`, `DruidDataSourceBeanPostProcessor` |
| Java | 17+ (`<java.version>17</java.version>` in the root `pom.xml`) |
| CI JDK matrix | 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`, managed by `microsphere-alibaba-druid-parent` |

## Compatibility Range

| Dimension | Supported on the `current` line |
|---|---|
| Alibaba Druid | `1.2.28` (as managed by the parent POM) |
| Java | 17, 21, 25 (JDK 17+ required) |
| Spring Framework | whatever the imported `microsphere-spring-cloud-dependencies` / Boot line provides, i.e. Spring `6.0.x` – `6.2.x` |
| Spring Boot / Spring Cloud | `3.0.13`/`2022.0.5`, `3.3.13`/`2023.0.6`, `3.4.13`/`2024.0.3`, `3.5.15`/`2025.0.3` — i.e. Spring Cloud `2022.0.x` – `2025.0.x`; these versions are inherited from the external parent POM chain `microsphere-spring-cloud-parent:0.2.26` and are not declared in this repository |
| Framework requirement of this module | Spring Framework (`spring-beans`, `spring-context`) plus `microsphere-spring-context`; Spring Boot is **not** required |

Spring Cloud `Hoxton` – `2021.0.x` / JDK 8 users: see the [1.x User Guide](../../1.x/user-guide/README.md).

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md)
- [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)
- [Project README](../../../README.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-spring.md](../developer-guide/microsphere-alibaba-druid-spring.md)

## 中文

- [简体中文：microsphere-alibaba-druid-spring](zh/microsphere-alibaba-druid-spring.md)
