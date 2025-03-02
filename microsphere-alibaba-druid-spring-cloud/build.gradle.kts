plugins {
    id("io.microsphere.component.java-library")
    id("io.microsphere.base.maven-publish")
}

description = "Microsphere Alibaba Druid Spring Cloud"

dependencies {
    // Microsphere Alibaba Druid Spring Boot
    implementation(project(":microsphere-alibaba-druid-core"))
    implementation(project(":microsphere-alibaba-druid-spring-boot"))
    // Alibaba Druid
    implementation(libs.druid)
    // Microsphere Spring Cloud Commons
    implementation("io.github.microsphere-projects:microsphere-spring-cloud-commons")
    // Spring Cloud
    implementation("org.springframework.cloud:spring-cloud-commons")
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    // Testing

    testImplementation(testFixtures(project(":microsphere-alibaba-druid-core")))
    testImplementation(testFixtures(project(":microsphere-alibaba-druid-spring")))

    // Spring Boot Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly(libs.h2)
    testRuntimeOnly(libs.logback.classic)
}
