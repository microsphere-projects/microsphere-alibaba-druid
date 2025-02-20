plugins {
    id("buildlogic.java-library-conventions")
}

description = "Microsphere Alibaba Druid Spring"

dependencies {
    // BOM
    // Microsphere Spring Dependencies (BOM)
    implementation(platform(libs.microsphere.spring.dependencies))
    // Spring Framework BOM
    implementation(platform(libs.spring.framework.bom))

    // Microsphere Alibaba Druid
    api(project(":microsphere-alibaba-druid-core"))
    // Microsphere Spring
    api("io.github.microsphere-projects:microsphere-spring-context")

    // Alibaba Druid
    "optionalApi"(libs.druid)

    // Spring Framework
    "optionalApi"("org.springframework:spring-beans")
    "optionalApi"("org.springframework:spring-context")

    // Testing
    testImplementation(project(":microsphere-alibaba-druid-spring-test"))

    // Spring Framework Test
    testImplementation("org.springframework:spring-test")
}
