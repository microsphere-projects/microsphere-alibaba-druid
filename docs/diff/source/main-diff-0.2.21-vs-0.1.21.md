# Source Diff: 0.1.21 vs 0.2.21 (`src/main`)

[中文 →](./zh/main-diff-0.2.21-vs-0.1.21.md)

Comparison of the `src/main` content of every Maven module between tags `0.1.21` (Java 8 / Spring Boot 2 line) and
`0.2.21` (Java 17 / Spring Boot 3 line). Modules are matched by name; both tags contain the same eight modules
(`core`, `spring`, `spring-boot`, `spring-cloud`, `spring-test`, `test`, `parent`, `dependencies`), so none was
ignored. Differences that stem purely from Java 8 vs Java 17 syntax/API or from Java EE (`javax.*`) vs Jakarta EE
(`jakarta.*`) replacement are excluded from the tables below.

**Method**: for each module, the full `src/main` trees of both tags were compared file-by-file via
`git diff 0.1.21 0.2.21 -- <module>/src/main`.

## Result Overview

> **All Java sources under `src/main` are byte-identical between the two tags**, in all six modules that carry
> `src/main` code. The only `src/main` differences are resource files.

## Modules Compared

| **Module** | **src/main diff** | **Notes** |
|------------|-------------------|-----------|
| `microsphere-alibaba-druid-core` | none | All Java sources identical |
| `microsphere-alibaba-druid-spring` | none | All Java sources identical |
| `microsphere-alibaba-druid-spring-boot` | 1 resource deleted | See table below |
| `microsphere-alibaba-druid-spring-cloud` | 1 resource deleted | See table below |
| `microsphere-alibaba-druid-spring-test` | none | All Java sources identical |
| `microsphere-alibaba-druid-test` | none | All Java sources identical |
| `microsphere-alibaba-druid-parent` | no `src` directory | POM-only module |
| `microsphere-alibaba-druid-dependencies` | no `src` directory | BOM-only module |

## Resource Differences (src/main)

| **Module** | **File** | **0.1.21** | **0.2.21** | **Reason** |
|------------|----------|------------|------------|------------|
| `spring-boot` | `src/main/resources/META-INF/spring.factories` | present — registers `AlibabaDruidAutoConfiguration` under `EnableAutoConfiguration` | **deleted** | API compatibility: Spring Boot 3 no longer loads auto-configurations from `spring.factories`. The replacement file `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` already exists with identical content in **both** tags, so 0.2.21 only drops the legacy duplicate kept for Spring Boot 2 consumers. |
| `spring-cloud` | `src/main/resources/META-INF/spring.factories` | present — registers `AlibabaDruidCloudAutoConfiguration` under `EnableAutoConfiguration` | **deleted** | Same as above — legacy registration removed once the baseline moves to Spring Boot 3; the `AutoConfiguration.imports` entry is unchanged. |

## Why the Java Sources Need No Changes Across the Java 8 → 17 / javax → jakarta Migration

- **No language-level upgrades**: the 0.2.x line did not rewrite sources with records, `var`, switch expressions or
  other Java 17 features; maintaining one identical source tree for both lines keeps the dual release branches cheap
  to sync (see the `merge-main-to-branches` workflow).
- **No Java EE / Jakarta EE surface**: the code only uses `java.sql.DataSource`, `java.sql.Connection` and
  `javax.sql.*` types that are part of the JDK itself and were **not** renamed into `jakarta.*`, so the Spring Boot 3
  namespace migration does not touch them.
- **Spring APIs used are namespace-stable**: auto-configuration classes rely on `spring-context` /
  `spring-boot` annotations (`@Configuration`, `@Bean`, `@Conditional*`, `@ConfigurationProperties`) whose FQNs did
  not change between Boot 2 and Boot 3.

## Related Non-src Configuration (context only, not part of src/main)

| **File** | **0.1.21** | **0.2.21** | **Reason** |
|----------|------------|------------|------------|
| root `pom.xml` | `revision=0.1.21-SNAPSHOT` | `revision=0.2.21-SNAPSHOT`, `java.version=17` | Release-line versioning / build baseline |
| root `pom.xml` parent | `microsphere-spring-cloud-parent:0.1.26` | `microsphere-spring-cloud-parent:0.2.26` | Platform BOM upgrade (JDK 17 / Boot 3 line) |
| `parent/pom.xml` | `microsphere-spring-cloud.version=0.1.26` | `0.2.26` | Same platform BOM upgrade; `druid.version` stays `1.2.28` in both |

## Companion Document

Test sources (`src/test`) are compared separately: [test-diff-0.2.21-vs-0.1.21.md](./test-diff-0.2.21-vs-0.1.21.md) ·
[中文](./zh/test-diff-0.2.21-vs-0.1.21.md)
