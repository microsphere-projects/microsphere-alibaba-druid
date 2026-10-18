# microsphere-alibaba-druid-spring-test

[English](../microsphere-alibaba-druid-spring-test.md)

## 模块用途

`microsphere-alibaba-druid-spring-test` 把 `microsphere-alibaba-druid-test` 的普通测试工具箱适配到基于 Spring 的测试，
提供：

- `AbstractDruidSpringTest` —— 与 `AbstractAlibabaDruidTest` 相同的 SQL 驱动测试基类，但它所操作的 `DruidDataSource`
  来自 Spring `BeanFactory`，而不再由属性文件构建。
- `DruidDataSourceTestConfiguration` —— 现成的配置类，为测试上下文贡献一个原型作用域、基于 H2 的 `DruidDataSource`
  Bean（`init` / `close` 生命周期）。

`spring`、`spring-boot`、`spring-cloud` 三个模块自身的测试源码用的正是它，因此照抄它们的测试是编写自己测试的最快路径。

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
| 版本 | 由 `microsphere-alibaba-druid-dependencies` BOM 管理（`current` 文档线为 `0.2.21`） |
| 打包方式 | `jar` |
| 编译依赖 | `microsphere-alibaba-druid-test`（非可选，因而测试中可直接使用 `@AlibabaDruidTest` 等） |
| 可选依赖 | `org.junit.jupiter:junit-jupiter`、`spring-test`、`spring-context`、`microsphere-spring-test`、`microsphere-spring-context`、`com.alibaba:druid`、`com.h2database:h2`、`ch.qos.logback:logback-classic` |
| 注意 | 它**不**依赖 `microsphere-alibaba-druid-core` 与 `microsphere-alibaba-druid-spring` |

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

Spring、`spring-test`、Druid、H2 都是可选依赖，需要使用方在测试模块中自行声明 —— 通常会与
`microsphere-alibaba-druid-spring`（以获取 `@EnableAlibabaDruid` / `DruidDataSourceBeanPostProcessor`）搭配。

## API 说明

### `io.microsphere.alibaba.druid.test.spring.AbstractDruidSpringTest`

`public abstract class AbstractDruidSpringTest extends AbstractAlibabaDruidTest`（自 `1.0.0` 起）。

| 成员 | 签名 | 语义 |
|---|---|---|
| `beanFactory` | `@Autowired private BeanFactory beanFactory` | 由 Spring 测试上下文注入 |
| `buildDruidDataSource` | `protected DruidDataSource buildDruidDataSource()` | 覆写父类实现：返回 `beanFactory.getBean(DruidDataSource.class)`，而不是从 `META-INF/druid/h2.properties` 构建 |

其余全部原样继承自 `AbstractAlibabaDruidTest` —— `init()` / `test()` / `destroy()`、`customize(DruidDataSource)` 钩子，
以及 `executeStatement` / `executePreparedStatement` / `executeConnection` 辅助方法。生命周期详见
[microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)。

由于数据源现在来自容器，filter 必须交由 Spring 挂载（`@EnableAlibabaDruid` 或手工注册
`DruidDataSourceBeanPostProcessor`），而不再适合用 `customize(...)`。

### `io.microsphere.alibaba.druid.test.spring.DruidDataSourceTestConfiguration`

`public class DruidDataSourceTestConfiguration`（自 `1.0.0` 起）—— 作为 `@ContextConfiguration(classes = ...)` 的参与者
使用，它自身并未标注 `@Configuration`。

| Bean 方法 | 声明 | 说明 |
|---|---|---|
| `dataSource` | `@Bean(initMethod = "init", destroyMethod = "close") @Scope(SCOPE_PROTOTYPE) public DruidDataSource dataSource() throws IOException` | 返回 `AlibabaDruidTestUtils.buildDefaultDruidDataSource()`；Spring 会把 `init()` 与 `close()` 作为生命周期回调执行 |

`SCOPE_PROTOTYPE` 是有意为之：`AbstractAlibabaDruidTest#destroy()` 会在每个测试方法后关闭连接池，而 `init()` 会在下一个
测试前索取一个新的 —— 若是单例 Bean，第二个测试拿到的将是已关闭的数据源。

## 使用示例

### 最小 Spring 测试

本模块测试源码中的 `DefaultDruidSpringTest`：

```java
@ExtendWith(value = SpringExtension.class)
@ContextConfiguration(classes = {
        DruidDataSourceTestConfiguration.class,
        DefaultDruidSpringTest.class
})
public class DefaultDruidSpringTest extends AbstractDruidSpringTest {
}
```

直接运行它，就会对一个 Spring 管理、基于 H2 的 `DruidDataSource` 执行 `AbstractAlibabaDruidTest` 完整的 DML/DQL 负载。

### 为测试加上 SQL 拦截

来自 `spring` 模块的 `EnableAlibabaDruidTest`（注意：属性名是 `filterClasses`，filter 搜索范围为 `BeanSource` 取值）：

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

也可以显式注册后置处理器，正如 `DruidDataSourceBeanPostProcessorTest` 的做法：

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

这两个类都是 `microsphere-alibaba-druid-spring` 的测试源码；Boot 场景的等价写法是
`AlibabaDruidAutoConfigurationIntegrationTest`（见
[microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)）。

### 改为贡献你自己的数据源

注册任意 `DruidDataSource` Bean（你自己的 `@Configuration`、`spring.datasource.*` 自动配置、嵌入式数据库），然后继承
`AbstractDruidSpringTest` 即可 —— 基类只向 `BeanFactory` 索要 `DruidDataSource.class`，因此测试配置类不是必需的。

## 配置属性

本模块不绑定任何自有属性：

| 属性 | 类型 | 默认值 | 与本模块的关系 |
|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true`（`matchIfMissing = true`） | 仅当自动配置也在类路径上时（Boot 测试） |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | 仅 Boot 测试；原生 Spring 测试中等价写法是 `@EnableAlibabaDruid(filterClasses = ...)` |
| — | — | — | 数据源本身经由 `DruidDataSourceTestConfiguration` 来自 `META-INF/druid/h2.properties` |

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `current`（release 分支） |
| 最新已发布版本 | `0.2.21`（`main` 上的 `0.2.22-SNAPSHOT` 是未发布的开发版） |
| Javadoc `@since` | `AbstractDruidSpringTest`、`DruidDataSourceTestConfiguration` 均为 `1.0.0` |
| Java | 17+（根 `pom.xml` 中 `<java.version>17</java.version>`） |
| CI JDK 矩阵 | 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`，由 `microsphere-alibaba-druid-parent` 管理 |

## 兼容范围

| 维度 | `current` 文档线支持情况 |
|---|---|
| Alibaba Druid | `1.2.28`（由父 POM 管理） |
| Java | 17、21、25（要求 JDK 17+） |
| JUnit | JUnit Jupiter 5 配合 `spring-test`（`SpringExtension`、`@SpringJUnitConfig`） |
| Spring Framework | 由 `microsphere-spring-cloud-dependencies` `0.2.26` 提供的版本线（Spring `6.0.x` – `6.2.x`） |
| 数据库 | 默认 H2 内存库（`jdbc:h2:mem:test_mem`）；任何可按 Druid 配置的数据源均可 |

仍在使用 Spring Cloud `Hoxton` – `2021.0.x` / JDK 8？请查看 [1.x 用户文档](../../../1.x/user-guide/README.md)。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
- [项目 README](../../../../README.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-spring-test.md)

## English

- [English version of this document](../microsphere-alibaba-druid-spring-test.md)
