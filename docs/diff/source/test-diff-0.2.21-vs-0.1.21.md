# Source Diff: 0.1.21 vs 0.2.21 (`src/test`)

[中文 →](./zh/test-diff-0.2.21-vs-0.1.21.md)

Comparison of the `src/test` content of every Maven module between tags `0.1.21` (Java 8 / Spring Boot 2 line) and
`0.2.21` (Java 17 / Spring Boot 3 line). Modules are matched by name; both tags contain the same eight modules, and
none had to be ignored. Pure Java 8 vs Java 17 syntax/API differences and Java EE vs Jakarta EE namespace
differences are excluded (none occurred).

**Method**: for each module, the full `src/test` trees of both tags were compared file-by-file via
`git diff 0.1.21 0.2.21 -- <module>/src/test`.

## Result Overview

> **All test sources are byte-identical between the two tags except one test class** in
> `microsphere-alibaba-druid-test`, which was restructured (no behavioural change).

## Modules Compared

| **Module** | **src/test diff** | **Notes** |
|------------|-------------------|-----------|
| `microsphere-alibaba-druid-core` | none | |
| `microsphere-alibaba-druid-spring` | none | Incl. identical `src/test/resources/META-INF/spring.factories` (test-scope registration, kept in both lines) |
| `microsphere-alibaba-druid-spring-boot` | none | |
| `microsphere-alibaba-druid-spring-cloud` | none | |
| `microsphere-alibaba-druid-spring-test` | none | |
| `microsphere-alibaba-druid-test` | 1 file changed | See below |
| `microsphere-alibaba-druid-parent` | no `src` directory | POM-only module |
| `microsphere-alibaba-druid-dependencies` | no `src` directory | BOM-only module |

## Changed Test Files (src/test)

| **Module** | **File** | **Change** | **Reason** |
|------------|----------|------------|------------|
| `test` | `src/test/java/io/microsphere/alibaba/druid/test/junit/jupiter/AlibabaDruidTestExtensionTest.java` | The top-level static-field-injection test was moved into a `@Nested` class `StaticFieldTest` (`@DisplayName("Test : Static Field Injection")`); the `assertObjects(Object...)` helper was relocated to the bottom area of the class. Assertions and logic are unchanged (`assertNotNull` on both injected fields, `close(druidDataSource)` called twice). | Test structure / feature verification: the rewrite isolates the *static field injection* scenario next to the existing `@Nested ParameterTest`, keeping one `@Nested` class per injection scenario. This documents that `@AlibabaDruidRuntime` injection also works for `static` fields of a nested test class — a capability check, not a fix (production code is identical, see the main diff). |

## Excluded Difference Categories

- **Java 8 vs Java 17 syntax/API**: no occurrences — test code is written at a Java 8-compatible level in both lines.
- **Java EE vs Jakarta EE**: no occurrences — tests only use JDK `java.sql` / Druid types and JUnit 5, whose
  coordinates do not change with the namespace migration.

## Companion Document

Main sources (`src/main`) are compared separately: [main-diff-0.2.21-vs-0.1.21.md](./main-diff-0.2.21-vs-0.1.21.md) ·
[中文](./zh/main-diff-0.2.21-vs-0.1.21.md)
