plugins {
    id("buildlogic.java-library-conventions")
}

description = "Microsphere Alibaba Druid Spring Boot"

dependencies {
    // BOM
    // Microsphere Spring Boot Dependencies (BOM)
    implementation(platform(libs.microsphere.spring.boot.dependencies))
    // Spring Boot Dependencies (BOM)
    implementation(platform(libs.spring.boot.dependencies))

    // Microsphere Alibaba Druid Spring
    api(project(":microsphere-alibaba-druid-spring"))
    // Microsphere Spring
    api("io.github.microsphere-projects:microsphere-spring-boot-core")

    // Alibaba Druid
    "optionalApi"(libs.druid)

    // Spring Boot
    "optionalApi"("org.springframework.boot:spring-boot-starter")
    "optionalApi"("org.springframework.boot:spring-boot-starter-actuator")
    "optionalApi"("org.springframework.boot:spring-boot-configuration-processor")

    // Testing
    testImplementation(project(":microsphere-alibaba-druid-spring-test"))

    // Spring Boot Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
