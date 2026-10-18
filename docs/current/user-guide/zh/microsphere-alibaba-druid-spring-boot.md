# microsphere-alibaba-druid-spring-boot

[English](../microsphere-alibaba-druid-spring-boot.md)

## 模块用途

`microsphere-alibaba-druid-spring-boot` 把 `microsphere-alibaba-druid-spring` 的 Spring 集成变成 Spring Boot
自动配置，提供：

- `AlibabaDruidAutoConfiguration` —— 注册 `DruidDataSourceBeanPostProcessor`（filter 注入）与
  `DataSourcePoolMetadataProvider`（Actuator 连接池指标），由
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 激活。
- `AlibabaDruidProperties` —— 绑定 `microsphere.alibaba.druid.*` 的属性（`enabled`、`filter.classes`）。
- `@ConditionalOnAlibabaDruidEnabled` 与 `@ConditionalOnAlibabaDruidAvailable` —— 用于门控自动配置的条件注解。
- `DruidDataSourcePoolMetadata` —— 基于 `DruidDataSource` 的 Spring Boot `DataSourcePoolMetadata` 实现，使
  `/actuator/health`、`/actuator/metrics` 能上报 Druid 连接池数据。

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
| 版本 | 由 `microsphere-alibaba-druid-dependencies` BOM 管理（`current` 文档线为 `0.2.21`） |
| 打包方式 | `jar` |
| 编译依赖 | `microsphere-alibaba-druid-spring`（非可选，因而也带入 `core`） |
| 可选依赖 | `microsphere-annotation-processor`、`microsphere-spring-boot-core`、`com.alibaba:druid`、`spring-boot-starter`、`spring-boot-starter-actuator`、`spring-boot-configuration-processor`、`spring-boot-autoconfigure-processor` |

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

这里的 Spring Boot、Actuator、`com.alibaba:druid` 全部是**可选**依赖，因此需要由应用自行添加（以及 JDBC 驱动）：

```xml
<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-druid-spring-boot</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid</artifactId>
    </dependency>

    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

Druid 的版本（`1.2.28`）由 [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md) 管理。

## API 说明

### 注册方式

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`：

```text
io.microsphere.alibaba.druid.spring.boot.autoconfigure.AlibabaDruidAutoConfiguration
```

只有 Boot 3 风格的注册 —— 本模块没有 `spring.factories` 的 `EnableAutoConfiguration` 条目。

### `io.microsphere.alibaba.druid.spring.boot.autoconfigure.AlibabaDruidAutoConfiguration`

`public class AlibabaDruidAutoConfiguration`（自 `1.0.0` 起），标注 `@ConditionalOnAlibabaDruidAvailable` 与
`@EnableConfigurationProperties(AlibabaDruidProperties.class)`。

| Bean 方法 | 签名 | 语义 |
|---|---|---|
| `druidDataSourceBeanPostProcessor` | `@Bean(name = BEAN_NAME) public BeanPostProcessor druidDataSourceBeanPostProcessor(AlibabaDruidProperties alibabaDruidProperties)` | 用 `alibabaDruidProperties.getFilter().getClasses()` 构造 `DruidDataSourceBeanPostProcessor`；声明返回类型为 `BeanPostProcessor`，Bean 名称为常量 `DruidDataSourceBeanPostProcessor.BEAN_NAME` = `druidDataSourceBeanPostProcessor` |
| `druidDataSourcePoolMetadataProvider` | `@Bean @ConditionalOnBean(DruidDataSource.class) public DataSourcePoolMetadataProvider druidDataSourcePoolMetadataProvider()` | 通过 `DataSourceUnwrapper.unwrap(dataSource, DruidDataSourceMBean.class, DruidDataSource.class)` 解包 `DataSource`；是 Druid 数据源时返回 `DruidDataSourcePoolMetadata`，否则返回 `null` |

> 由于 `druidDataSourcePoolMetadataProvider` 受 `@ConditionalOnBean(DruidDataSource.class)` 约束，连接池指标要求容器中
> 存在真正的 `DruidDataSource` **Bean** —— 在容器外创建的数据源并不够。

### `io.microsphere.alibaba.druid.spring.boot.AlibabaDruidProperties`

`@ConfigurationProperties(prefix = ALIBABA_DRUID_PROPERTY_NAME_PREFIX) public class AlibabaDruidProperties` —— 该
prefix 解析为 `microsphere.alibaba.druid`（自 `1.0.0` 起）。

| 成员 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `isEnabled()` / `setEnabled(boolean)` | `boolean` | `true` | 与 `microsphere.alibaba.druid.enabled` 对应的总开关 |
| `getFilter()` / `setFilter(Filter)` | `AlibabaDruidProperties.Filter` | `new Filter()` | 嵌套的 filter 属性 |
| `Filter.getClasses()` / `Filter.setClasses(...)` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `of(com.alibaba.druid.filter.Filter.class)`，即 `[com.alibaba.druid.filter.Filter]` | 会被注入每个 `DruidDataSource` 的 filter Bean 类型；`Filter.class` 表示“所有 filter 子类” |

### `io.microsphere.alibaba.druid.spring.boot.condition.ConditionalOnAlibabaDruidEnabled`

`public @interface ConditionalOnAlibabaDruidEnabled`（自 `1.0.0` 起）—— `@Target(TYPE)`、`@Retention(RUNTIME)`、
`@Documented`，元注解为：

```java
@ConditionalOnProperty(name = ALIBABA_DRUID_ENABLED_PROPERTY_NAME, matchIfMissing = true)
```

因此当 `microsphere.alibaba.druid.enabled` 为 `true` **或未配置**时自动配置生效，配置为
`microsphere.alibaba.druid.enabled=false` 时关闭。（`ALIBABA_DRUID_ENABLED_PROPERTY_NAME` 即
`microsphere.alibaba.druid.enabled`；该类 Javadoc 中提到的旧名称
`microsphere.spring.boot.alibaba.druid.enabled` 并未被代码使用。）

### `io.microsphere.alibaba.druid.spring.boot.condition.ConditionalOnAlibabaDruidAvailable`

`public @interface ConditionalOnAlibabaDruidAvailable`（自 `1.0.0` 起）—— `@Target(TYPE)`、`@Retention(RUNTIME)`、
`@Documented`，是 `@ConditionalOnAlibabaDruidEnabled` 与下者的组合：

```java
@ConditionalOnClass(name = {
        "com.alibaba.druid.pool.DruidDataSource"                   // Alibaba Druid API
})
```

两个条件必须同时满足。`@ConditionalOnClass` 使用的是 `name`（字符串形式），因此在运行时缺少 Druid 时也不会报错。

### `io.microsphere.alibaba.druid.spring.boot.metadata.DruidDataSourcePoolMetadata`

`public class DruidDataSourcePoolMetadata implements DataSourcePoolMetadata`（自 `1.0.0` 起），以
`DruidDataSource` 为构造参数。

| 方法 | Druid 数据来源 | 说明 |
|---|---|---|
| `Float getUsage()` | `getActiveCount()` / `getMaxActive()` | 无活跃连接时为 `0.0f`；`active == maxActive` 时为 `1.0f`；否则为 `active / (maxActive * 1.0f)`；不会返回 `null` |
| `Integer getActive()` | `getActiveCount()` | 活跃（使用中）连接数 |
| `Integer getIdle()` | `getMaxIdle()` | 是**最大**空闲连接数，而非当前空闲连接数 |
| `Integer getMax()` | `getMaxActive()` | 最大活跃连接数 |
| `Integer getMin()` | `getMinIdle()` | 最小空闲连接数 |
| `String getValidationQuery()` | `getValidationQuery()` | 未配置校验 SQL 时返回 `null` |
| `Boolean getDefaultAutoCommit()` | `isDefaultAutoCommit()` | 默认自动提交标记 |

## 使用示例

### 自动配置的实际效果

本模块的集成测试 `AlibabaDruidAutoConfigurationIntegrationTest`（测试源码）展示了完整配置 —— 一个
`DruidDataSource` Bean 加一个 filter Bean，属性指向该 filter 类型：

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

    @Autowired
    private AlibabaDruidProperties alibabaDruidProperties;

    @Autowired
    private LoggingStatementFilter loggingStatementFilter;

    @Autowired
    private DataSourcePoolMetadataProvider dataSourcePoolMetadataProvider;

    @Autowired
    private DataSource dataSource;

    @Test
    public void test() throws Throwable {
        super.test();
        assertNotNull(alibabaDruidProperties);
        assertNotNull(loggingStatementFilter);

        AlibabaDruidProperties.Filter filter = alibabaDruidProperties.getFilter();
        assertNotNull(filter);

        Class<? extends com.alibaba.druid.filter.Filter>[] classes = filter.getClasses();
        assertNotNull(classes);
        assertArrayEquals(of(LoggingStatementFilter.class), classes);

        DataSourcePoolMetadata dataSourcePoolMetadata = dataSourcePoolMetadataProvider.getDataSourcePoolMetadata(dataSource);
        assertInstanceOf(DruidDataSourcePoolMetadata.class, dataSourcePoolMetadata);
    }
}
```

在应用中，等价写法只有：

```properties
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.url=jdbc:h2:mem:test_mem
spring.datasource.driver-class-name=org.h2.Driver

microsphere.alibaba.druid.enabled=true
microsphere.alibaba.druid.filter.classes=io.microsphere.alibaba.druid.filter.LoggingStatementFilter

management.endpoints.web.exposure.include=health,metrics
```

> `microsphere.alibaba.druid.filter.classes` 接受的是类**名称**；binder 会将其转换为 `Class` 值。若名称无法解析为
> `Filter` 子类，绑定会失败；若列出的类没有注册为 Bean，则什么都不会被注入。

### 读取绑定后的属性

改写自 `AlibabaDruidPropertiesTest`（测试源码）；`isNotEmpty` 与 `ofArray` 来自 `io.microsphere.util.ArrayUtils`：

```java
AlibabaDruidProperties properties = new AlibabaDruidProperties();
assertTrue(properties.isEnabled());

properties.setEnabled(false);
assertFalse(properties.isEnabled());

assertNotNull(properties.getFilter());

AlibabaDruidProperties.Filter filter = new AlibabaDruidProperties.Filter();
assertTrue(isNotEmpty(filter.getClasses()));

filter.setClasses(ofArray(com.alibaba.druid.filter.Filter.class));
assertArrayEquals(ofArray(com.alibaba.druid.filter.Filter.class), filter.getClasses());
```

### 连接池元数据

来自 `DruidDataSourcePoolMetadataTest`（测试源码），它使用了
[microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md) 中的 JUnit 5 注解：

```java
@AlibabaDruidTest
class DruidDataSourcePoolMetadataTest {

    @AlibabaDruidRuntime
    private DruidDataSource druidDataSource;

    private DruidDataSourcePoolMetadata metadata;

    @BeforeEach
    void setUp() {
        this.metadata = new DruidDataSourcePoolMetadata(this.druidDataSource);
    }

    @Test
    void test() {
        assertNotNull(this.metadata.getUsage());
        assertEquals(valueOf(0), this.metadata.getActive());
        assertEquals(valueOf(8), this.metadata.getIdle());
        assertEquals(valueOf(8), this.metadata.getMax());
        assertEquals(valueOf(0), this.metadata.getMin());
        assertNull(this.metadata.getValidationQuery());
        assertTrue(this.metadata.getDefaultAutoCommit());
    }
}
```

其中 `maxActive = 8`、`maxIdle = 8`、`minIdle = 0` 是 Druid 默认值，并非本模块设置。

### 在自己的测试中校验自动配置

`AlibabaDruidAutoConfigurationTest`（测试源码）继承 `microsphere-spring-boot-test` 的 `AutoConfigurationTest`，并声明
期望结果：

```java
@SpringBootTest(
        classes = {
                AlibabaDruidAutoConfigurationTest.class
        },
        webEnvironment = NONE
)
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

| 属性 | 类型 | 默认值 | 绑定位置 | 消费方 |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true`（`matchIfMissing = true`） | `AlibabaDruidProperties.enabled`、`PropertyConstants`（core） | `@ConditionalOnAlibabaDruidEnabled`、`@ConditionalOnAlibabaDruidAvailable` |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter.classes` | `AlibabaDruidAutoConfiguration.druidDataSourceBeanPostProcessor(...)` |

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `current`（release 分支） |
| 最新已发布版本 | `0.2.21`（`main` 上的 `0.2.22-SNAPSHOT` 是未发布的开发版） |
| Javadoc `@since` | `AlibabaDruidProperties`、`AlibabaDruidAutoConfiguration`、两个条件注解、`DruidDataSourcePoolMetadata` 均为 `1.0.0` |
| Java | 17+（根 `pom.xml` 中 `<java.version>17</java.version>`） |
| CI JDK 矩阵 | 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`，由 `microsphere-alibaba-druid-parent` 管理 |

## 兼容范围

| 维度 | `current` 文档线支持情况 |
|---|---|
| Alibaba Druid | `1.2.28`（由父 POM 管理） |
| Java | 17、21、25（要求 JDK 17+） |
| Spring Boot | `3.0.x` – `3.5.x`；验证版本为 `3.0.13`、`3.3.13`、`3.4.13`、`3.5.15` —— 这些版本**继承自外部父 POM 链** `microsphere-spring-cloud-parent:0.2.26`，并非本仓库声明 |
| Spring Cloud | `2022.0.x` – `2025.0.x`（`2022.0.5`、`2023.0.6`、`2024.0.3`、`2025.0.3`） |
| 自动配置注册方式 | Boot 3 的 `AutoConfiguration.imports`；本条文档线不支持 Boot 2 的 `spring.factories` |

仍在使用 Boot 2.x / JDK 8？请查看 [1.x 用户文档](../../../1.x/user-guide/README.md)。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-cloud.md](microsphere-alibaba-druid-spring-cloud.md)
- [microsphere-alibaba-druid-test.md](microsphere-alibaba-druid-test.md)
- [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)
- [项目 README](../../../../README.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-spring-boot.md)

## English

- [English version of this document](../microsphere-alibaba-druid-spring-boot.md)
