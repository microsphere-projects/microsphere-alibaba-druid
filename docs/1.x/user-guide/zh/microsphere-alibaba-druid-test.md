# microsphere-alibaba-druid-test（`1.x`）

[English](../microsphere-alibaba-druid-test.md)

## 模块用途

`microsphere-alibaba-druid-test` 是 `1.x` 文档线中与 Spring 无关的测试支持模块。它提供了一种可重复、
不依赖框架的方式，在挂着你的 `Filter` 实现的真实 `DruidDataSource` 上执行真实 SQL：

- `AbstractAlibabaDruidTest` —— JUnit 5 测试模板：构建基于 H2 的 `DruidDataSource`，创建 `users` 表，
  执行一整套固定的 `Statement` / `PreparedStatement` 调用，最后删表并关闭连接池。
- `AlibabaDruidTestUtils` —— 不可实例化的工具类，负责加载 `META-INF/druid/h2.properties` 并把 `Properties`
  转换为已配置的 `DruidDataSource`。
- `@AlibabaDruidTest` + `@AlibabaDruidRuntime` + `AlibabaDruidTestExtension` —— 声明式 JUnit 5 支持，
  把可用的 `DruidDataSource` 注入字段（实例或静态）以及测试方法参数。
- `META-INF/druid/h2.properties` —— 上述所有能力共用的默认数据源描述文件。

## Maven 坐标

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-test</artifactId>
    <scope>test</scope>
</dependency>
```

| 项目 | 值 |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-test` |
| 版本 | 由 `microsphere-alibaba-druid-dependencies` BOM 管理（`1.x` 文档线为 `0.1.21`） |
| 打包方式 | `jar`（main 源码，而非 test-jar） |
| compile 依赖 | 无 —— 全部为 optional |
| optional 依赖 | `microsphere-annotation-processor`、`junit-jupiter`、`microsphere-java-test`、`microsphere-logging-test`、`com.alibaba:druid`、`com.h2database:h2`、`logback-classic` |

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

testImplementation("io.github.microsphere-projects:microsphere-alibaba-druid-test")
```

> 由于 JUnit 5、Druid 与 H2 都是 `optional`，你必须自行添加它们（`junit-jupiter`、`com.alibaba:druid`、
> `com.h2database:h2`）。本模块**不**compile 依赖 `microsphere-alibaba-druid-core`，这正是 `core` 可以以
> `test` 作用域反向依赖它的原因。

## API 说明

### `io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils`

`public abstract class AlibabaDruidTestUtils`，带 `private` 构造器（自 `1.0.0` 起）—— 不可实例化。

| 常量 / 方法 | 签名 | 说明 |
|---|---|---|
| `H2_PROPERTIES_RESOURCE` | `public static final String = "META-INF/druid/h2.properties"` | 随模块提供的描述文件 |
| `DEFAULT_PROPERTIES_RESOURCE` | `public static final String = H2_PROPERTIES_RESOURCE` | 别名，作为注解默认值 |
| `PACKAGE_NAME_PREFIX` | `public static final String = "com.alibaba.druid."` | 扩展类用它识别 Druid 类型 |
| `getDefaultProperties` | `public static Properties getDefaultProperties() throws IOException` | 加载 `DEFAULT_PROPERTIES_RESOURCE` |
| `loadProperties` | `public static Properties loadProperties(String resourceName) throws IOException` | 加载任意类路径 properties 资源 |
| `loadProperties` | `public static Properties loadProperties(String[] keyValuePairs) throws IOException` | 由 `"key = value"` 字符串构造 `Properties` |
| `buildDefaultDruidDataSource` | `public static DruidDataSource buildDefaultDruidDataSource() throws IOException` | 即 `buildDruidDataSource(getDefaultProperties())`；**未** 初始化 —— 需自行调用 `init()` |
| `buildDruidDataSource` | `public static DruidDataSource buildDruidDataSource(Properties properties)` | 封装 `new DruidDataSource()` + `configFromProperties(properties)` |

随模块提供的 `META-INF/druid/h2.properties` 及其被 `AlibabaDruidTestUtilsTest` 验证的内容：

```properties
druid.name=h2
druid.url=jdbc:h2:mem:test_mem
druid.username=sa
druid.password=
druid.driverClassName=org.h2.Driver
```

```java
Properties properties = getDefaultProperties();
assertEquals("h2", properties.getProperty("druid.name"));
assertEquals("jdbc:h2:mem:test_mem", properties.getProperty("druid.url"));
assertEquals("sa", properties.getProperty("druid.username"));
assertEquals("", properties.getProperty("druid.password"));
assertEquals("org.h2.Driver", properties.getProperty("druid.driverClassName"));
```

所有键都使用 `druid.` 前缀，这是 `DruidDataSource.configFromProperties(Properties)` 接受的格式。

### `io.microsphere.alibaba.druid.test.AbstractAlibabaDruidTest`

`public abstract class AbstractAlibabaDruidTest`（自 `1.0.0` 起）。生命周期：

| 钩子 | 注解 | 行为 |
|---|---|---|
| `init()` | `@BeforeEach` | `buildDruidDataSource()` → `customize(dataSource)` → `dataSource.init()` → 创建表 `users (id INT, name VARCHAR(50))` |
| `buildDruidDataSource()` | —— | `protected DruidDataSource buildDruidDataSource() throws IOException`，默认返回 `buildDefaultDruidDataSource()`；可覆写以提供自己的数据源 |
| `customize(DruidDataSource)` | —— | `protected void customize(DruidDataSource dataSource)` —— 默认空实现；添加 filter 的位置 |
| `getDruidDataSource()` | —— | `protected DruidDataSource getDruidDataSource()` —— 当前连接池 |
| `test()` | `@Test` | 依次执行 `testExecuteStatement()` 与 `testExecutePreparedStatement()`，子类因此免费获得端到端拦截测试 |
| `testExecuteStatement()` | —— | `INSERT`（四种 `executeUpdate` 重载）、`addBatch` + `executeBatch`、`executeQuery`、`UPDATE`、`DELETE`（全部 `execute` 重载） |
| `testExecutePreparedStatement()` | —— | `prepareStatement(sql)` + `executeUpdate`、`prepareStatement(sql, TYPE_FORWARD_ONLY)` + `executeQuery`、`prepareStatement(sql, TYPE_FORWARD_ONLY, CONCUR_UPDATABLE)` + `execute` |
| `executeStatement(ThrowableConsumer<Statement>)` / `executePreparedStatement(String, ThrowableConsumer<PreparedStatement>)` / `executeConnection(ThrowableConsumer<Connection>)` | —— | 资源管理辅助方法；全部在 `finally` 中关闭，异常声明为 `throws Throwable` |
| `destroy()` | `@AfterEach` | `DROP TABLE users`，随后 `dataSource.close()` |

最简用法（即 `DefaultAlibabaDruidTest`）：

```java
public class DefaultAlibabaDruidTest extends AbstractAlibabaDruidTest {
}
```

为被测 filter 挂载数据源：

```java
public abstract class AbstractFilterTest<F extends Filter> extends AbstractAlibabaDruidTest {

    @Override
    protected void customize(DruidDataSource dataSource) {
        dataSource.getProxyFilters().add(createFilter());
    }

    protected abstract F createFilter();
}
```

（*这是 `microsphere-alibaba-druid-core` 测试源码中的 `AbstractFilterTest`；
`LoggingStatementFilterTest` 继承它并返回 `new LoggingStatementFilter()`。*）

### `io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidTest`

`@Target(TYPE) @Retention(RUNTIME) @ExtendWith(AlibabaDruidTestExtension.class) @Inherited public @interface
AlibabaDruidTest`（自 `1.0.0` 起）。

| 属性 | 类型 | 默认值 | 含义 |
|---|---|---|---|
| `propertiesResource` | `String` | `AlibabaDruidTestUtils.DEFAULT_PROPERTIES_RESOURCE` | 用于配置连接池的类路径 properties 资源 |
| `properties` | `String[]` | `{}` | `"key = value"` 形式的条目，**覆盖**资源内容，例如 `"druid.url=jdbc:mysql://localhost:3306/test"` |
| `filters` | `String[]` | `{}` | 传给 `DruidDataSource.setFilters(String)` 的 filter 名称（Druid 内置如 `log`、`stat`、`wall`） |

说明：`filters` 在注解上声明；扩展在构建数据源时会把 `properties` 覆盖项合并到 `propertiesResource` 之上，
再依次调用 `configFromProperties(...)` 与 `init()`。

### `io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidRuntime`

`@Target({FIELD, PARAMETER}) @Retention(RUNTIME) public @interface AlibabaDruidRuntime`（自 `1.0.0` 起）——
标记注入点。目标类型必须是 Druid 候选类型：可赋值为 `DataSource`，或类名以 `com.alibaba.druid.` 开头
（候选列表由 `findAllClasses(DruidDataSource.class, AlibabaDruidTestExtension::isAlibabaDruidClass)` 一次性计算）。

### `io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidTestExtension`

包级私有的 `class AlibabaDruidTestExtension implements BeforeAllCallback, AfterAllCallback,
TestInstancePostProcessor, AfterEachCallback, ParameterResolver`（自 `1.0.0` 起）—— 只能通过
`@AlibabaDruidTest` 激活。

| 回调 | 效果 |
|---|---|
| `beforeAll` | 注入 `@AlibabaDruidRuntime` **静态**字段（每个测试类一个连接池） |
| `postProcessTestInstance` | 注入 `@AlibabaDruidRuntime` **实例**字段（每个方法一个连接池） |
| `afterEach` | 取出并关闭方法级连接池 |
| `afterAll` | 取出并移除类级连接池，然后关闭 |
| `supportsParameter` / `resolveParameter` | 解析带 `@AlibabaDruidRuntime` 的测试方法参数 |

连接池保存在 `context.getStore(StoreScope.EXTENSION_CONTEXT, namespace)` 得到的 JUnit `Store` 中，
命名空间为 `METHOD` 与 `CLASS`；存储键形如 `"<方法签名或测试类名>:<组件类名>"`。`close(...)`
对 `null` 安全且幂等（会检查 `isClosed()`）。

## 使用示例

### 注入实例字段、静态字段与方法参数

对应 `AlibabaDruidTestExtensionTest`（`microsphere-alibaba-druid-test` 测试源码）在 `1.x` 上的写法：

```java
@AlibabaDruidTest
class AlibabaDruidTestExtensionTest {

    @AlibabaDruidRuntime
    private static DruidDataSource druidDataSource;

    @AlibabaDruidRuntime
    private static DataSource dataSource;

    @Test
    @DisplayName("Test : Static Field Injection")
    void test() {
        assertObjects(druidDataSource, dataSource);
        close(druidDataSource);
        close(druidDataSource);
    }

    @Nested
    @DisplayName("Test : Parameter Injection")
    class ParameterTest {

        @Test
        void test(@AlibabaDruidRuntime DruidDataSource druidDataSource, @AlibabaDruidRuntime DataSource dataSource) {
            assertObjects(druidDataSource, dataSource);
        }
    }
}
```

### 在单元测试里使用被注入的连接池

`DruidDataSourcePoolMetadataTest`（spring-boot 测试源码）是典型消费者：

```java
@AlibabaDruidTest
class DruidDataSourcePoolMetadataTest {

    @AlibabaDruidRuntime
    private DruidDataSource druidDataSource;

    @Test
    void test() {
        DruidDataSourcePoolMetadata metadata = new DruidDataSourcePoolMetadata(druidDataSource);
        assertNotNull(metadata.getUsage());
    }
}
```

### 指向另一种数据库

```java
@AlibabaDruidTest(
        propertiesResource = "META-INF/druid/mysql.properties",
        properties = {
                "druid.url=jdbc:mysql://localhost:3306/test",
                "druid.username=root",
                "druid.driverClassName=com.mysql.jdbc.Driver"
        }
)
class MysqlFilterTest extends AbstractAlibabaDruidTest {
}
```

`properties` 优先于资源文件，因此只覆盖某一个键时无需再写第二份文件。

### 完全不需要 Spring

本模块被 `core` 直接使用、被 `spring-test` 间接使用。若测试不需要容器，
请优先使用 `@AlibabaDruidTest` / `AbstractAlibabaDruidTest`，而不是
[microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md)。

## 配置属性

本模块不读取应用属性，只通过 `DruidDataSource.configFromProperties(Properties)` 认识的 `druid.*` 键来配置：

| 键（来自 `META-INF/druid/h2.properties`） | 值 | 作用 |
|---|---|---|
| `druid.name` | `h2` | 连接池名称 |
| `druid.url` | `jdbc:h2:mem:test_mem` | 内存版 H2 数据库 |
| `druid.username` | `sa` | 用户 |
| `druid.password` | *（空）* | 密码 |
| `druid.driverClassName` | `org.h2.Driver` | JDBC 驱动 |

Boot 模块的 `microsphere.alibaba.druid.*` 属性不适用于本测试模块。

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `1.x`（由 `release-1.x` 分支生成） |
| 最新已发布版本 | `0.1.21`（`0.1.22-SNAPSHOT` 是分支上未发布的开发版） |
| Javadoc `@since` | `AbstractAlibabaDruidTest`、`AlibabaDruidTestUtils`、`@AlibabaDruidTest`、`@AlibabaDruidRuntime`、`AlibabaDruidTestExtension` 均为 `1.0.0` |
| Java 基线 | `8` —— 继承自外部父 POM 链（`microsphere-spring-cloud-parent:0.1.26` → `microsphere-build:0.3.14`），**本仓库未声明** |
| CI JDK 矩阵 | 8 / 11 / 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`，由 `microsphere-alibaba-druid-parent` 管理 |
| JUnit | Jupiter 5.x，具体版本由生效的 Boot 线决定（`Hoxton.SR12` → Boot `2.3.12.RELEASE`，`2021.0.9` → Boot `2.6.15`） |

## 兼容范围

| 维度 | `1.x` 文档线支持情况 |
|---|---|
| Alibaba Druid | `1.2.28` |
| Java | 8 及以上 |
| JUnit | Jupiter 5（所用 `Extension` API —— `StoreScope.EXTENSION_CONTEXT`、`Namespace` —— 要求提供 `StoreScope` 的 JUnit 5 版本） |
| Spring | 不需要 —— 本模块不依赖 Spring |
| 数据库 | 只要把驱动放进测试类路径即可；默认使用 H2 |

## 与 current 文档线的差异

公开 API 完全一致。本模块只有一个测试类在 `release-1.x` 上组织方式不同：
`AlibabaDruidTestExtensionTest` 把静态字段注入用例放在顶层类中，而不是放进 `@Nested StaticFieldTest`
内部类 —— 这只是内部编排，不影响你使用的 API。此外本制品面向 JDK 8 与 Boot 2 矩阵（`Hoxton` – `2021.0.x`）。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-parent.md](microsphere-alibaba-druid-parent.md)
- [项目 README](../../../../README.md)
- [current 文档线的同名文档](../../../current/user-guide/zh/microsphere-alibaba-druid-test.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-test.md)

## English

- [English version of this document](../microsphere-alibaba-druid-test.md)
