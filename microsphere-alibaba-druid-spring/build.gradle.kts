plugins {
    id("buildlogic.java-library-conventions")
}

description = "Microsphere Alibaba Druid Spring"

dependencies {
    // BOM
    // Microsphere Spring BOM
    implementation(platform(libs.microsphere.spring.dependencies))
    // Spring Framework BOM
    implementation(platform(libs.spring.framework.bom))

    // Microsphere Alibaba Druid
    implementation(project(":microsphere-alibaba-druid-core"))
    // Microsphere Spring
    implementation("io.github.microsphere-projects:microsphere-spring-context")

    // Alibaba Druid
    "optionalApi"(libs.druid)

    // Spring Framework
    "optionalApi"("org.springframework:spring-beans")
    "optionalApi"("org.springframework:spring-context")

    // Testing
    testImplementation(libs.junit.jupiter.engine)

    // H2 DataBase
    testImplementation(libs.h2)

    // Logback
    testImplementation(libs.logback.classic)

    // Spring Framework Test
    testImplementation("org.springframework:spring-test")
}
