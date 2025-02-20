pluginManagement {
    // Include 'plugins build' to define convention plugins.
    includeBuild("build-logic")
}

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "microsphere-alibaba-druid"

include(
    "microsphere-alibaba-druid-core",
    "microsphere-alibaba-druid-spring",
    "microsphere-alibaba-druid-spring-boot",
    "microsphere-alibaba-druid-spring-cloud",
    "microsphere-alibaba-druid-test",
    "microsphere-alibaba-druid-spring-test",
    "microsphere-alibaba-druid-dependencies",
)
