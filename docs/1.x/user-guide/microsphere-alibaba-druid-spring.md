# microsphere-alibaba-druid-spring (`1.x`)

[中文](zh/microsphere-alibaba-druid-spring.md)

## Purpose

`microsphere-alibaba-druid-spring` brings Alibaba Druid's `Filter` support into the plain Spring Framework container
on the `1.x` line. It gives you:

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
| Version | managed by the `microsphere-alibaba-druid-dependencies` BOM (`0.1.21` on the `1.x` line) |
| Packaging | `jar` |
| Compile dependency | `microsphere-alibaba-druid-core` (non-optional) |
| Optional dependencies | `microsphere-annotation-processor`, `microsphere-spring-context`, `com.alibaba:druid`, `spring-beans`, `spring-context`, `spring-test` |

Import the BOM once, then omit versions:

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

With Gradle:

```kotlin
implementation(platform("io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.1.21"))

implementation("io.github.microsphere-projects:microsphere-alibaba-druid-spring")
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

> On this line, `META-INF/spring.factories` plays two roles: the one above is a *user* file that `@EnableAlibabaDruid`
> reads to discover filters, while the `spring-boot` / `spring-cloud` modules also ship their *own*
> `spring.factories` that registers the auto-configurations for Boot 2.x. Do not confuse the two.

### Register filters through the Java `ServiceLoader`

`src/test/resources/META-INF/services/io.microsphere.alibaba.druid.filter.AbstractStatementFilter`:

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
| Documentation line | `1.x` (generated from the `release-1.x` branch) |
| Latest released version | `0.1.21` (`0.1.22-SNAPSHOT` is the unpublished branch version) |
| Since (Javadoc) | `1.0.0` for `EnableAlibabaDruid`, `AlibabaDruidRegistrar`, `DruidDataSourceBeanPostProcessor` |
| Java baseline | `8` — inherited from the external parent chain (`microsphere-spring-cloud-parent:0.1.26` → `microsphere-build:0.3.14`); **not declared** in this repository |
| CI JDK matrix | 8 / 11 / 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`, managed by `microsphere-alibaba-druid-parent` |

## Compatibility Range

| Dimension | Supported on the `1.x` line |
|---|---|
| Alibaba Druid | `1.2.28` (as managed by the parent POM) |
| Java | 8 and newer (JDK 8 source level; the jars also run on 11/17/21/25) |
| Spring Framework | the version provided by the imported `microsphere-spring-cloud-dependencies:0.1.26`, i.e. Spring `5.x` (as shipped by Spring Boot 2.3 – 2.6) |
| Spring Boot / Spring Cloud | `2.3.12.RELEASE`/`Hoxton.SR12`, `2.4.13`/`2020.0.5`, `2.6.15`/`2021.0.9` — i.e. Spring Cloud `Hoxton` – `2021.0.x`; inherited from the external parent POM chain `microsphere-spring-cloud-parent:0.1.26`, not declared in this repository |
| Framework requirement of this module | Spring Framework (`spring-beans`, `spring-context`) plus `microsphere-spring-context`; Spring Boot is **not** required |

## Differences from the `current` line

The Java API is identical; only the platform differs — this artifact is built for JDK 8 and the Boot 2.3–2.6 /
Cloud `Hoxton`–`2021.0.x` matrix, and the modules it feeds register their auto-configurations through
`spring.factories` in addition to `AutoConfiguration.imports`.

## See Also

- [User Guide index](README.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md)
- [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)
- [Project README](../../../README.md)
- [current-line version of this document](../../current/user-guide/microsphere-alibaba-druid-spring.md)
- Developer Guide: [../developer-guide/microsphere-alibaba-druid-spring.md](../developer-guide/microsphere-alibaba-druid-spring.md)

## 中文

- [简体中文：microsphere-alibaba-druid-spring](zh/microsphere-alibaba-druid-spring.md)
