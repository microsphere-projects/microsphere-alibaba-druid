# microsphere-alibaba-druid-spring-test（`1.x`）

[English](../microsphere-alibaba-druid-spring-test.md)

## 模块用途

`microsphere-alibaba-druid-spring-test` 是 `1.x` 文档线上 `microsphere-alibaba-druid-test` 的 Spring 版搭档。
它让同一套语句执行矩阵跑在**位于 Spring 上下文之内**的 `DruidDataSource` 上 —— 验证 filter 注入正是需要这种方式：

- `AbstractDruidSpringTest` —— 继承 `AbstractAlibabaDruidTest`，但从 `BeanFactory` 获取数据源，而不是在代码里构建。
- `DruidDataSourceTestConfiguration` —— 一个 `@Configuration` 风格的类，注册一个由默认 H2 属性构建的
  prototype 作用域 `DruidDataSource` Bean。

## Maven 坐标

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-spring-test</artifactId>
    <scope>test</scope>
</dependency>
```

| 项目 | 值 |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-spring-test` |
| 版本 | 由 `microsphere-alibaba-druid-dependencies` BOM 管理（`1.x` 文档线为 `0.1.21`） |
| 打包方式 | `jar`（main 源码，而非 test-jar） |
| compile 依赖 | `microsphere-alibaba-druid-test`（非 optional） |
| optional 依赖 | `junit-jupiter`、`spring-test`、`spring-context`、`microsphere-spring-test`、`microsphere-spring-context`、`com.alibaba:druid`、`com.h2database:h2`、`logback-classic` |

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
testImplementation(platform("io.github.microsphere-projects:microsphere-alibaba-druid-dependencies:0.1.21"))

testImplementation("io.github.microsphere-projects:microsphere-alibaba-druid-spring-test")
```

> 只有 `microsphere-alibaba-druid-test` 会被传递引入。由于本模块中它们都是 `optional`，
> 你必须自行添加 `spring-test`、`spring-context`、`junit-jupiter`、`com.alibaba:druid` 与 `com.h2database:h2`。

## API 说明

### `io.microsphere.alibaba.druid.test.spring.AbstractDruidSpringTest`

`public abstract class AbstractDruidSpringTest extends AbstractAlibabaDruidTest`（自 `1.0.0` 起）。

| 成员 | 签名 | 说明 |
|---|---|---|
| `beanFactory` | `@Autowired private BeanFactory beanFactory` | 由 Spring TestContext 框架注入；需要激活某个 Spring 测试扩展 |
| `buildDruidDataSource()` | `protected DruidDataSource buildDruidDataSource()` | **覆写**父类钩子：返回 `beanFactory.getBean(DruidDataSource.class)` —— 不再抛 `IOException`，并且数据源*已经*由容器初始化 |

其余部分（`@BeforeEach init()`、`@Test test()`、`Statement`/`PreparedStatement` 矩阵、`@AfterEach destroy()`、
`customize(...)` 与 `getDruidDataSource()` 钩子）都原样继承自 `AbstractAlibabaDruidTest` ——
见 [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)。

从容器取 Bean 带来两个结果：

1. `customize(DruidDataSource)` 仍会被调用，但拿到的实例是容器的 Bean，因此
   `DruidDataSourceBeanPostProcessor` 添加的 filter 对测试可见。
2. 对已初始化的 `DruidDataSource` 再次 `init()` 在 Druid 中是空操作，所以模板生命周期依然安全。

### `io.microsphere.alibaba.druid.test.spring.DruidDataSourceTestConfiguration`

`public class DruidDataSourceTestConfiguration`（自 `1.0.0` 起）—— 只有一个 Bean 定义：

```java
@Bean(initMethod = "init", destroyMethod = "close")
@Scope(SCOPE_PROTOTYPE)
public DruidDataSource dataSource() throws IOException {
    return buildDefaultDruidDataSource();
}
```

| 方面 | 取值 | 原因 |
|---|---|---|
| Bean 名 | `dataSource` | 使 `beanFactory.getBean(DruidDataSource.class)` 可按类型解析 |
| `initMethod` / `destroyMethod` | `init` / `close` | 由容器管理连接池生命周期 |
| 作用域 | `SCOPE_PROTOTYPE` | 每次 `getBean(...)` 都返回**全新**的连接池。`AbstractAlibabaDruidTest.destroy()` 在每个测试方法后关闭连接池，若是共享单例，从第二个方法起便不可用 |
| 配置来源 | `AlibabaDruidTestUtils.buildDefaultDruidDataSource()` | 即来自 `test` 模块的 `META-INF/druid/h2.properties` |

## 使用示例

### 普通 Spring 测试上下文

即 `DefaultDruidSpringTest`（本模块自身测试源码）的写法：

```java
@ExtendWith(value = SpringExtension.class)
@ContextConfiguration(classes = {
        DruidDataSourceTestConfiguration.class,
        DefaultDruidSpringTest.class
})
public class DefaultDruidSpringTest extends AbstractDruidSpringTest {
}
```

### Spring Boot 测试上下文

Boot 与 Cloud 的集成测试复用同一基类与配置
（取自 `AlibabaDruidAutoConfigurationIntegrationTest` 与 `AlibabaDruidCloudAutoConfigurationIntegrationTest`）：

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

由于该类继承 `AbstractDruidSpringTest`，继承来的 `@Test test()` 会*穿过*自动配置的
`DruidDataSourceBeanPostProcessor` 执行整套 SQL 矩阵 —— filter 注入正是由此被覆盖到。

### 验证 `@EnableAlibabaDruid`

`EnableAlibabaDruidTest`（`microsphere-alibaba-druid-spring` 测试源码）展示了注解 + 基类的组合，
并使用 `@SpringJUnitConfig` 简写代替 `@ExtendWith` + `@ContextConfiguration`：

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

### 覆写数据源或自定义逻辑

```java
@SpringJUnitConfig(classes = {
        DruidDataSourceTestConfiguration.class,
        MyFilter.class,
        MyDruidFilterIT.class
})
public class MyDruidFilterIT extends AbstractDruidSpringTest {

    @Override
    protected void customize(DruidDataSource dataSource) {
        // the pool is already init()-ed by the container;
        // register filters here only if you need them before the SQL matrix runs
    }
}
```

## 配置属性

本模块**不**绑定任何 `microsphere.alibaba.druid.*` 属性 —— 它是测试基础设施，不是自动配置组件。
连接池由 `META-INF/druid/h2.properties`（`druid.*` 键）配置，见
[microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)：

| 属性 | 是否作用于本模块 | 说明 |
|---|---|---|
| `microsphere.alibaba.druid.enabled` | 否 | 只有 Boot/Cloud 自动配置读取它 |
| `microsphere.alibaba.druid.filter.classes` | 否 —— 除非测试中 Boot 自动配置处于生效状态 | 生效时，列出的 filter Bean 类型会在初始化前注入那个 prototype `DruidDataSource` |
| `spring.*` 数据源属性 | 否 | 测试配置绕过 Boot 的数据源自动配置，自行注册 Bean |

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `1.x`（由 `release-1.x` 分支生成） |
| 最新已发布版本 | `0.1.21`（`0.1.22-SNAPSHOT` 是分支上未发布的开发版） |
| Javadoc `@since` | `AbstractDruidSpringTest`、`DruidDataSourceTestConfiguration` 均为 `1.0.0` |
| Java 基线 | `8` —— 继承自外部父 POM 链（`microsphere-spring-cloud-parent:0.1.26` → `microsphere-build:0.3.14`），**本仓库未声明** |
| CI JDK 矩阵 | 8 / 11 / 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`，由 `microsphere-alibaba-druid-parent` 管理 |
| 所用 Spring Framework API | `@Bean`、`@Scope`、`BeanFactory`（本文档线为 Spring 5.x） |

## 兼容范围

| 维度 | `1.x` 文档线支持情况 |
|---|---|
| Alibaba Druid | `1.2.28` |
| Java | 8 及以上 |
| Spring Test | `spring-test` 5.x（由 Spring Boot 2.3 – 2.6 提供）；除此之外本模块与框架版本无关 |
| JUnit | Jupiter 5 |
| Spring Boot | 不要求 —— 可在纯 `@ExtendWith(SpringExtension.class)` 测试中使用；本文档线的 Boot/Cloud 测试同样使用它 |
| 数据库 | 默认 H2（由 `META-INF/druid/h2.properties` 构建的 prototype Bean） |

## 与 current 文档线的差异

API 完全一致；本制品只是面向 JDK 8 字节码与 Spring 5.x / Boot 2.3–2.6（`Hoxton` – `2021.0.x`）构建，
而非 JDK 17 与 Spring 6.x / Boot 3（`2022.0.x` – `2025.0.x`）。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [项目 README](../../../../README.md)
- [current 文档线的同名文档](../../../current/user-guide/zh/microsphere-alibaba-druid-spring-test.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-spring-test.md)

## English

- [English version of this document](../microsphere-alibaba-druid-spring-test.md)
