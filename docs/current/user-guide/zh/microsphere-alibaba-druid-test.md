# microsphere-alibaba-druid-test

[English](../microsphere-alibaba-druid-test.md)

## 模块用途

`microsphere-alibaba-druid-test` 是本项目中与 Spring 无关的测试工具箱：它提供基于 H2 的可用 `DruidDataSource`，并驱动
SQL 执行，从而验证你自己的 `Filter` 实现：

- `AbstractAlibabaDruidTest` —— JUnit 5 基类：构建并初始化 `DruidDataSource`、创建 `users` 表、跑一遍 `Statement` 与
  `PreparedStatement` 流量，最后删表并关闭连接池。
- `AlibabaDruidTestUtils` —— 静态工具，加载 `META-INF/druid/h2.properties` 并由 `Properties` 构建 `DruidDataSource`。
- `@AlibabaDruidTest` + `@AlibabaDruidRuntime` + `AlibabaDruidTestExtension` —— JUnit Jupiter 扩展：构建已初始化的
  `DruidDataSource`，并注入测试字段（静态与实例）或方法参数。

尽管名字里有 "druid"，本模块**并不依赖 `microsphere-alibaba-druid-core`** —— 它只需要 Druid 与 JUnit 5，这也正是 `core`
能以 `test` 作用域使用它的原因。

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
| 版本 | 由 `microsphere-alibaba-druid-dependencies` BOM 管理（`current` 文档线为 `0.2.21`） |
| 打包方式 | `jar` |
| 编译依赖 | 本仓库构件一个都没有 —— 下列依赖全部为可选 |
| 可选依赖 | `microsphere-annotation-processor`、`org.junit.jupiter:junit-jupiter`、`microsphere-java-test`、`microsphere-logging-test`、`com.alibaba:druid`、`com.h2database:h2`、`ch.qos.logback:logback-classic` |

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

所有依赖（包括 `junit-jupiter`、`druid`、`h2`）都声明为 `<optional>true</optional>`，因此使用本模块的测试工程仍需自行声明
Druid、H2 与 JUnit 5 引擎。

## API 说明

### `io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils`

`public abstract class AlibabaDruidTestUtils`（自 `1.0.0` 起）—— 私有构造器，只提供静态方法。

| 成员 | 签名 | 语义 |
|---|---|---|
| `H2_PROPERTIES_RESOURCE` | `public static final String = "META-INF/druid/h2.properties"` | 随包发布的 H2 数据源属性资源 |
| `DEFAULT_PROPERTIES_RESOURCE` | `public static final String = H2_PROPERTIES_RESOURCE` | 未指定时使用的默认资源 |
| `PACKAGE_NAME_PREFIX` | `public static final String = "com.alibaba.druid."` | 用于识别 Druid 类型的包名前缀 |
| `getDefaultProperties` | `public static @Nonnull Properties getDefaultProperties() throws IOException` | 加载 `DEFAULT_PROPERTIES_RESOURCE` |
| `loadProperties` | `public static @Nonnull Properties loadProperties(String resourceName) throws IOException` | 从类路径加载任意属性资源 |
| `loadProperties` | `public static @Nonnull Properties loadProperties(String[] keyValuePairs) throws IOException` | 由内联的 `"key = value"` 条目（按行分隔符拼接）构造 `Properties` |
| `properties` | `static @Nonnull Properties properties(String content) throws IOException` | 包级私有的解析辅助方法 |
| `buildDefaultDruidDataSource` | `public static @Nonnull DruidDataSource buildDefaultDruidDataSource() throws IOException` | 即 `buildDruidDataSource(getDefaultProperties())` |
| `buildDruidDataSource` | `public static @Nonnull DruidDataSource buildDruidDataSource(Properties properties)` | `new DruidDataSource()` + `configFromProperties(properties)`；**不会**初始化 —— 需自行调用 `init()` |

随包发布的 `META-INF/druid/h2.properties`（main 资源）：

```properties
druid.name=h2
druid.url=jdbc:h2:mem:test_mem
druid.username=sa
druid.password=
druid.driverClassName=org.h2.Driver
```

`AlibabaDruidTestUtilsTest`（测试源码）对上述内容的断言：

```java
Properties properties = getDefaultProperties();
assertEquals("h2", properties.getProperty("druid.name"));
assertEquals("jdbc:h2:mem:test_mem", properties.getProperty("druid.url"));
assertEquals("sa", properties.getProperty("druid.username"));
assertEquals("", properties.getProperty("druid.password"));
assertEquals("org.h2.Driver", properties.getProperty("druid.driverClassName"));
```

### `io.microsphere.alibaba.druid.test.AbstractAlibabaDruidTest`

`public abstract class AbstractAlibabaDruidTest`（自 `1.0.0` 起）。

生命周期：

| 阶段 | 方法 | 行为 |
|---|---|---|
| `@BeforeEach` | `public void init() throws Throwable` | `buildDruidDataSource()` → `customize(dataSource)` → `dataSource.init()` → `initData()`（`CREATE TABLE users (id INT, name VARCHAR(50))`） |
| `@Test` | `public void test() throws Throwable` | 依次执行 `testExecuteStatement()` 与 `testExecutePreparedStatement()` |
| `@AfterEach` | `public void destroy() throws Throwable` | `destroyData()`（`DROP TABLE users`），随后 `dataSource.close()` |

扩展点：

| 方法 | 默认实现 | 用于 |
|---|---|---|
| `protected DruidDataSource buildDruidDataSource() throws IOException` | `AlibabaDruidTestUtils.buildDefaultDruidDataSource()` | 提供自己的数据源（例如 Spring 容器管理的 —— 见 `AbstractDruidSpringTest`） |
| `protected void customize(DruidDataSource dataSource)` | 空 | 注册 filter：`dataSource.getProxyFilters().add(new LoggingStatementFilter())` |
| `protected DruidDataSource getDruidDataSource()` | 返回当前实例 | 改变连接池的获取方式 |
| `protected void testExecuteStatement() throws Throwable` | 4 次 INSERT（覆盖全部 `executeUpdate` 重载）、`addBatch`/`executeBatch`、`executeQuery`、UPDATE、4 次 DELETE 以及 `DELETE FROM users`，并断言影响行数 | 替换 SQL 负载 |
| `protected void testExecutePreparedStatement() throws Throwable` | 对 `INSERT INTO users (id, name) VALUES (?, ?)` 执行 `executeUpdate`，再用 `prepareStatement(sql, TYPE_FORWARD_ONLY)` 与 `prepareStatement(sql, TYPE_FORWARD_ONLY, CONCUR_UPDATABLE)` 执行查询 | 替换预编译语句负载 |
| `protected void executeStatement(ThrowableConsumer<Statement> consumer) throws Throwable` | 借出 `Statement` 并保证关闭 | 通过被拦截的连接池执行自定义 SQL |
| `protected void executePreparedStatement(String sql, ThrowableConsumer<PreparedStatement> consumer) throws Throwable` | 为 `sql` 借出 `PreparedStatement` 并保证关闭 | 执行自定义参数化 SQL |
| `protected void executeConnection(ThrowableConsumer<Connection> consumer) throws Throwable` | 从 `getDruidDataSource()` 借出 `Connection` 并保证关闭 | 连接级别的任意操作 |

`executeStatement`、`executePreparedStatement`、`executeConnection` 接收
`io.microsphere.lang.function.ThrowableConsumer`，因此 lambda 可以直接抛出受检异常，无需包装。

### `io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidTest`

`@Target(TYPE) @Retention(RUNTIME) @ExtendWith(AlibabaDruidTestExtension.class) @Inherited public @interface AlibabaDruidTest`
（自 `1.0.0` 起）。

| 属性 | 类型 | 默认值 | 含义 |
|---|---|---|---|
| `properties` | `String[]` | `{}` | 内联的 `druid.*=value` 条目，会**覆盖** `propertiesResource()` 中的同名项 |
| `propertiesResource` | `String` | `AlibabaDruidTestUtils.DEFAULT_PROPERTIES_RESOURCE`（`META-INF/druid/h2.properties`） | 交给 `DruidDataSource.configFromProperties(Properties)` 的类路径资源 |
| `filters` | `String[]` | `{}` | 声明的 filter 名称（面向 `DruidDataSource.setFilters(String)`，例如 `{"log", "stat", "wall"}`）。`0.2.21` 的扩展**并未**应用它 —— 请通过属性资源（`druid.filters=...`）或 filter 自身的 `@AutoLoad` 机制来配置 |

```java
@AlibabaDruidTest(
    properties = {
        "druid.name=mysql",
        "druid.url=jdbc:mysql://localhost:3306/test",
        "druid.username=root",
        "druid.password=123456",
        "druid.driverClassName=com.mysql.jdbc.Driver"
    }
)
class MyTest {
}
```

（代码片段取自 `AlibabaDruidTest` 的 Javadoc。）

### `io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidRuntime`

`@Target({FIELD, PARAMETER}) @Retention(RUNTIME) public @interface AlibabaDruidRuntime`（自 `1.0.0` 起）—— 把 Druid
运行时注入以下类型：

| 组件 | 静态字段 | 实例字段 | 方法参数 |
|---|---|---|---|
| `javax.sql.DataSource` | Y | Y | Y |
| `javax.sql.ConnectionPoolDataSource` | Y | Y | Y |
| `com.alibaba.druid.proxy.jdbc.DataSourceProxy` | Y | Y | Y |
| `com.alibaba.druid.pool.DruidDataSource` | Y | Y | Y |

任何可赋值为 `DataSource` 或类名以 `com.alibaba.druid.` 开头的类型都是候选（见
`AlibabaDruidTestExtension.isAlibabaDruidClass`）。

### `io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidTestExtension`

`class AlibabaDruidTestExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor,
AfterEachCallback, ParameterResolver` —— **包级私有**（自 `1.0.0` 起），由 `@AlibabaDruidTest` 激活。

| 行为 | 细节 |
|---|---|
| 静态字段注入 | `beforeAll` → `injectFields(context, null)` 填充 `@AlibabaDruidRuntime` 静态字段 |
| 实例字段注入 | `postProcessTestInstance` 填充非静态字段 |
| 参数注入 | 通过 `supportsParameter` / `resolveParameter` 支持带 `@AlibabaDruidRuntime` 的参数 |
| 构建 | `buildDruidDataSource(AlibabaDruidTest)` 加载 `propertiesResource()`，再用 `properties()` 覆盖（`putAll`），随后 `configFromProperties(...)` 与 `init()` |
| 存储 | 类级 store（`beforeAll`/`afterAll`）与方法级 store（逐个测试）分开，键为 `测试类名或方法签名:组件类名` |
| 释放 | `afterEach` 关闭方法作用域的池；`afterAll` 关闭类作用域的池；`close(...)` 对 `null` 与已关闭实例都是安全的 |
| 注解查找 | `getAlibabaDruidTest(Class)` 会回退到外部类，因此外层类带注解时 `@Nested` 测试类也能工作 |

## 使用示例

### 继承基类并插入 filter

`core` 自己的基类就是标准写法（来自 `microsphere-alibaba-druid-core` 测试源码）：

```java
public abstract class AbstractFilterTest<F extends Filter> extends AbstractAlibabaDruidTest {

    @Override
    protected void customize(DruidDataSource dataSource) {
        dataSource.getProxyFilters().add(createFilter());
    }

    protected abstract F createFilter();
}
```

```java
@LoggingLevelsClass(levels = {"TRACE", "INFO", "ERROR"})
public class LoggingStatementFilterTest extends AbstractFilterTest<LoggingStatementFilter> {

    @Override
    protected LoggingStatementFilter createFilter() {
        return new LoggingStatementFilter();
    }
}
```

最简子类 `DefaultAlibabaDruidTest`（本模块测试源码）一行代码都不需要：

```java
public class DefaultAlibabaDruidTest extends AbstractAlibabaDruidTest {
}
```

### 使用 JUnit 5 注解

来自 `microsphere-alibaba-druid-spring-boot` 的 `DruidDataSourcePoolMetadataTest`：

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
}
```

字段、静态字段与参数注入三者并用，正是 `AlibabaDruidTestExtensionTest`（测试源码）覆盖的场景：

```java
@AlibabaDruidTest
class AlibabaDruidTestExtensionTest {

    @Nested
    @DisplayName("Test : Field Injection")
    class FieldTest extends AbstractAlibabaDruidTest {

        @AlibabaDruidRuntime
        private DruidDataSource druidDataSource;

        @AlibabaDruidRuntime
        private DataSource dataSource;

        @Override
        protected DruidDataSource buildDruidDataSource() throws IOException {
            return this.druidDataSource;
        }
    }

    @Nested
    @DisplayName("Test : Static Field Injection")
    class StaticFieldTest {

        @AlibabaDruidRuntime
        private static DruidDataSource druidDataSource;

        @AlibabaDruidRuntime
        private static DataSource dataSource;

        @Test
        void test() {
            assertObjects(druidDataSource, dataSource);

            close(druidDataSource);
            close(druidDataSource);
        }
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

注意 `FieldTest` 通过覆写 `buildDruidDataSource()` 复用被注入的连接池 —— 否则 `AbstractAlibabaDruidTest` 会再创建一个。

### 自行构建数据源

```java
DruidDataSource dataSource = AlibabaDruidTestUtils.buildDefaultDruidDataSource();
dataSource.getProxyFilters().add(new LoggingStatementFilter());
dataSource.init();
try {
    // exercise SQL
} finally {
    dataSource.close();
}
```

## 配置属性

本模块不读取任何 `microsphere.alibaba.druid.*` 属性，它通过注解与属性资源来配置：

| 配置点 | 位置 | 默认值 | 作用 |
|---|---|---|---|
| `propertiesResource` | `@AlibabaDruidTest` | `META-INF/druid/h2.properties` | 交给 `configFromProperties` 的 Druid `druid.*` 键 |
| `properties` | `@AlibabaDruidTest` | `{}` | 在资源之上生效的内联覆盖项 |
| `filters` | `@AlibabaDruidTest` | `{}` | 已声明，但 `0.2.21` 的扩展未应用 |
| `customize(DruidDataSource)` | `AbstractAlibabaDruidTest` | 空 | 在 `init()` 之前添加 filter 或调整连接池 |

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `current`（release 分支） |
| 最新已发布版本 | `0.2.21`（`main` 上的 `0.2.22-SNAPSHOT` 是未发布的开发版） |
| Javadoc `@since` | `AbstractAlibabaDruidTest`、`AlibabaDruidTestUtils`、`@AlibabaDruidTest`、`@AlibabaDruidRuntime`、`AlibabaDruidTestExtension` 均为 `1.0.0` |
| Java | 17+（根 `pom.xml` 中 `<java.version>17</java.version>`） |
| CI JDK 矩阵 | 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`，由 `microsphere-alibaba-druid-parent` 管理 |

## 兼容范围

| 维度 | `current` 文档线支持情况 |
|---|---|
| Alibaba Druid | `1.2.28`（由父 POM 管理） |
| Java | 17、21、25（要求 JDK 17+） |
| JUnit | JUnit Jupiter 5（使用的扩展接口为 `BeforeAllCallback` / `AfterAllCallback` / `TestInstancePostProcessor` / `AfterEachCallback` / `ParameterResolver`） |
| 数据库 | 开箱即用为 H2 内存库（`jdbc:h2:mem:test_mem`）；通过 `properties` / `propertiesResource` 可用任意驱动 |
| 框架要求 | 无 —— 不需要 Spring；基于 Spring 的测试请使用 `microsphere-alibaba-druid-spring-test` |

仍在使用 Spring Cloud `Hoxton` – `2021.0.x` / JDK 8？请查看 [1.x 用户文档](../../../1.x/user-guide/README.md)。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring-test.md](microsphere-alibaba-druid-spring-test.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
- [项目 README](../../../../README.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-test.md)

## English

- [English version of this document](../microsphere-alibaba-druid-test.md)
