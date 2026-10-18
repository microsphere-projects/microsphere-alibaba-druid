# microsphere-alibaba-druid-spring-cloud

[English](../microsphere-alibaba-druid-spring-cloud.md)

## 模块用途

`microsphere-alibaba-druid-spring-cloud` 是位于 `microsphere-alibaba-druid-spring-boot` 之上的轻量 Spring Cloud 层，
提供：

- `AlibabaDruidCloudAutoConfiguration` —— Spring Cloud 自动配置，与 Boot 自动配置受同一个
  `@ConditionalOnAlibabaDruidAvailable` 条件门控。
- `META-INF/config/default/features.yaml` —— *抽象特性（abstract feature）* 声明，使 Microsphere Alibaba Druid 出现在
  Spring Cloud 的 `/actuator/features` 端点上。

只要声明一个 `DruidDataSource` 与一个 `Filter` Bean，`/actuator/features` 就会把 Druid 报告为应用的激活特性 —— 无需
编写任何代码。

## Maven 坐标

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-druid-spring-cloud</artifactId>
</dependency>
```

| 项目 | 值 |
|---|---|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-alibaba-druid-spring-cloud` |
| 版本 | 由 `microsphere-alibaba-druid-dependencies` BOM 管理（`current` 文档线为 `0.2.21`） |
| 打包方式 | `jar` |
| 编译依赖 | `microsphere-alibaba-druid-spring-boot`（非可选，因而也带入 `spring` 与 `core`） |
| 可选依赖 | `microsphere-annotation-processor`、`microsphere-spring-cloud-commons`、`com.alibaba:druid`、`spring-cloud-commons`、`spring-boot-starter`、`spring-boot-starter-actuator`、`spring-boot-configuration-processor`、`spring-boot-autoconfigure-processor` |

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

`spring-cloud-commons`（包含 `FeaturesEndpoint`、`HasFeatures`、`NamedFeature`）与 Actuator starter 都是**可选**依赖，
需要 Spring Cloud 应用自行引入 —— 通常通过其 Actuator 与 `spring-cloud-commons` 依赖。

## API 说明

### `io.microsphere.alibaba.druid.spring.cloud.autoconfigure.AlibabaDruidCloudAutoConfiguration`

`@ConditionalOnAlibabaDruidAvailable public class AlibabaDruidCloudAutoConfiguration`（自 `1.0.0` 起）。

| 方面 | 细节 |
|---|---|
| 注册方式 | `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中包含 `io.microsphere.alibaba.druid.spring.cloud.autoconfigure.AlibabaDruidCloudAutoConfiguration` |
| 生效条件 | `microsphere.alibaba.druid.enabled` 为 `true` 或未配置（`matchIfMissing = true`），**并且** 类路径上存在 `com.alibaba.druid.pool.DruidDataSource` |
| 声明的 Bean | 无 —— 该类是一个标记式自动配置；`DruidDataSourceBeanPostProcessor` 与 `DataSourcePoolMetadataProvider` 来自 Boot 模块的 `AlibabaDruidAutoConfiguration` |
| 作用 | 为本模块开启 Spring Cloud 特性上报（见下文 `features.yaml`），并经由继承的 Boot 配置暴露 `AlibabaDruidProperties` |

### `META-INF/config/default/features.yaml`

`src/main/resources` 中随包发布的内容：

```yaml
microsphere:
  spring:
    cloud:
      features:
        abstract:
          microsphere-alibaba-druid-core:
            - com.alibaba.druid.pool.DruidDataSource
            - com.alibaba.druid.filter.Filter
```

| 键 | 值 | 含义 |
|---|---|---|
| `microsphere.spring.cloud.features.abstract.microsphere-alibaba-druid-core` | `com.alibaba.druid.pool.DruidDataSource` | *抽象特性* 条目：容器中存在 `DruidDataSource` Bean 时，`/actuator/features` 上报 `microsphere-alibaba-druid-core:DruidDataSource` |
| 同一列表 | `com.alibaba.druid.filter.Filter` | `Filter` 条目是抽象的，会解析为已注册的具体 filter 类型，例如 `microsphere-alibaba-druid-core:LoggingStatementFilter` |

> 尽管该文件位于 `spring-cloud` 模块，列表的键仍是 `microsphere-alibaba-druid-core` —— 特性名是逻辑分组标签，而不是发布该
> 文件的构件名。

## 使用示例

### 配置属性

```properties
microsphere.alibaba.druid.enabled=true
spring.cloud.features.enabled=true
management.endpoints.web.exposure.include=features
microsphere.alibaba.druid.filter.classes=io.microsphere.alibaba.druid.filter.LoggingStatementFilter
```

### 校验上报的特性

`AlibabaDruidCloudAutoConfigurationIntegrationTest`（测试源码）展示了本模块的产出：它注入了所有 `HasFeatures` Bean 以及
`FeaturesEndpoint`：

```java
@SpringBootTest(classes = {
        LoggingStatementFilter.class,
        DruidDataSourceTestConfiguration.class,
        AlibabaDruidCloudAutoConfigurationIntegrationTest.class
}, webEnvironment = NONE,
        properties = {
                "microsphere.alibaba.druid.enabled=true",
                "management.endpoints.web.exposure.include=features",
                "spring.cloud.features.enabled=true",
                "microsphere.alibaba.druid.filter.classes=io.microsphere.alibaba.druid.filter.LoggingStatementFilter"
        })
@EnableAutoConfiguration
public class AlibabaDruidCloudAutoConfigurationIntegrationTest extends AbstractDruidSpringTest {

    @Autowired
    private Map<String, HasFeatures> hasFeaturesMap;

    @Autowired
    private FeaturesEndpoint featuresEndpoint;

    @Test
    public void test() {
        assertTrue(this.hasFeaturesMap.size() > 0);
        HasFeatures hadFeatures = this.hasFeaturesMap.get("microsphere-alibaba-druid-core.features");
        assertNotNull(hadFeatures);

        List<NamedFeature> namedFeatures = hadFeatures.getNamedFeatures();
        assertEquals(2, namedFeatures.size());

        assertNamedFeature(namedFeatures, 0, "microsphere-alibaba-druid-core:DruidDataSource", DruidDataSource.class);
        assertNamedFeature(namedFeatures, 1, "microsphere-alibaba-druid-core:LoggingStatementFilter", LoggingStatementFilter.class);

        assertNotNull(featuresEndpoint.features());
    }

    private void assertNamedFeature(List<NamedFeature> namedFeatures, int index, String name, Class<?> type) {
        NamedFeature namedFeature = namedFeatures.get(index);
        assertEquals(name, namedFeature.getName());
        assertEquals(type, namedFeature.getType());
    }
}
```

可以依赖的事实：

- `HasFeatures` Bean 的名称是 `microsphere-alibaba-druid-core.features`。
- 它恰好贡献声明的两个特性，顺序与声明一致：先 `...:DruidDataSource`，再解析出的具体 filter
  `...:LoggingStatementFilter`。
- 端点负载需要调用 `featuresEndpoint.features()`；暴露范围由 `management.endpoints.web.exposure.include=features`
  配置，而端点本身由 `spring.cloud.features.enabled=true` 开启。

### 自动配置的单元测试

`AlibabaDruidCloudAutoConfigurationTest`（测试源码）继承 `microsphere-spring-boot-test` 的 `AutoConfigurationTest`：

```java
@SpringBootTest(
        classes = {
                AlibabaDruidCloudAutoConfigurationTest.class
        },
        webEnvironment = NONE
)
class AlibabaDruidCloudAutoConfigurationTest extends AutoConfigurationTest<AlibabaDruidCloudAutoConfiguration> {

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
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

`configureAutoConfiguredClasses` 有意留空：本自动配置不注册任何自有 Bean；而禁用属性与缺失 `DruidDataSource` 类正是它退避的
两种方式。

## 配置属性

本模块没有新增属性，它复用 Boot 的属性以及 Spring Cloud 的特性开关：

| 属性 | 类型 | 默认值 | 定义位置 | 作用 |
|---|---|---|---|---|
| `microsphere.alibaba.druid.enabled` | `boolean` | `true`（`matchIfMissing = true`） | `PropertyConstants`（core），由 `AlibabaDruidProperties` 绑定 | 开关 `AlibabaDruidCloudAutoConfiguration` |
| `microsphere.alibaba.druid.filter.classes` | `Class<? extends com.alibaba.druid.filter.Filter>[]` | `[com.alibaba.druid.filter.Filter]` | `AlibabaDruidProperties.Filter`（spring-boot） | 决定存在哪些 filter Bean，从而决定上报哪个具体 `Filter` 特性 |
| `spring.cloud.features.enabled` | `boolean` | 由 Spring Cloud `spring-cloud-commons` 定义 | Spring Cloud，而非本项目 | 开启 `FeaturesEndpoint` |
| `management.endpoints.web.exposure.include` | `String`（逗号分隔） | — | Spring Boot Actuator | 必须包含 `features` 才会暴露 `/actuator/features` |

## 版本信息

| 项目 | 值 |
|---|---|
| 文档线 | `current`（release 分支） |
| 最新已发布版本 | `0.2.21`（`main` 上的 `0.2.22-SNAPSHOT` 是未发布的开发版） |
| Javadoc `@since` | `AlibabaDruidCloudAutoConfiguration` 为 `1.0.0` |
| Java | 17+（根 `pom.xml` 中 `<java.version>17</java.version>`） |
| CI JDK 矩阵 | 17 / 21 / 25 |
| Alibaba Druid | `1.2.28`，由 `microsphere-alibaba-druid-parent` 管理 |

## 兼容范围

| 维度 | `current` 文档线支持情况 |
|---|---|
| Alibaba Druid | `1.2.28`（由父 POM 管理） |
| Java | 17、21、25（要求 JDK 17+） |
| Spring Cloud | `2022.0.x` – `2025.0.x` —— 验证版本 `2022.0.5`、`2023.0.6`、`2024.0.3`、`2025.0.3`（构建 profile `spring-cloud-2022` … `spring-cloud-2025`）；这些版本**继承自外部父 POM 链** `microsphere-spring-cloud-parent:0.2.26`，并非本仓库声明 |
| Spring Boot | `3.0.x` – `3.5.x`（`3.0.13`、`3.3.13`、`3.4.13`、`3.5.15`） |
| 特性上报 | 需要类路径上有 `spring-cloud-commons`（`FeaturesEndpoint` / `HasFeatures`）与 Actuator |

仍在使用 Spring Cloud `Hoxton` – `2021.0.x` / JDK 8？请查看 [1.x 用户文档](../../../1.x/user-guide/README.md)。

## 另见

- [用户文档索引](README.md)
- [microsphere-alibaba-druid-spring-boot.md](microsphere-alibaba-druid-spring-boot.md)
- [microsphere-alibaba-druid-core.md](microsphere-alibaba-druid-core.md)
- [microsphere-alibaba-druid-spring.md](microsphere-alibaba-druid-spring.md)
- [microsphere-alibaba-druid-dependencies.md](microsphere-alibaba-druid-dependencies.md)
- [项目 README](../../../../README.md)
- [开发者文档](../../developer-guide/zh/microsphere-alibaba-druid-spring-cloud.md)

## English

- [English version of this document](../microsphere-alibaba-druid-spring-cloud.md)
