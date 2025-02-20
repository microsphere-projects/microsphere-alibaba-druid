plugins {
    id("buildlogic.java-library-conventions")
}

description = "Microsphere Alibaba Druid Spring Cloud"

dependencies {
    // BOM
    // Microsphere Spring Cloud Dependencies (BOM)
    implementation(platform(libs.microsphere.spring.cloud.dependencies))
    // Spring Cloud Dependencies (BOM)
    implementation(platform(libs.spring.cloud.dependencies))

    // Microsphere Alibaba Druid Spring Boot
    api(project(":microsphere-alibaba-druid-spring-boot"))

    // Microsphere Spring Cloud Commons
    api("io.github.microsphere-projects:microsphere-spring-cloud-commons")

    // Alibaba Druid
    "optionalApi"(libs.druid)

    // Spring Cloud
    "optionalApi"("org.springframework.cloud:spring-cloud-commons")

    // Spring Boot
    "optionalApi"("org.springframework.boot:spring-boot-starter")
    "optionalApi"("org.springframework.boot:spring-boot-starter-actuator")
    "optionalApi"("org.springframework.boot:spring-boot-configuration-processor")

    // Testing
    testImplementation(project(":microsphere-alibaba-druid-spring-test"))

    // Spring Boot Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
