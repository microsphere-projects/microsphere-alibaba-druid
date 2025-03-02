plugins {
    id("io.microsphere.component.java-library")
    id("io.microsphere.base.maven-publish")

}

description = "Microsphere Alibaba Druid Spring Boot"

dependencies {

    // Microsphere Alibaba Druid Spring
    implementation(project(":microsphere-alibaba-druid-core"))
    implementation(project(":microsphere-alibaba-druid-spring"))
    // Alibaba Druid
    implementation(libs.druid)
    // Microsphere Spring
    implementation("io.github.microsphere-projects:microsphere-spring-boot-core")
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    // Spring Boot Test
    testImplementation(testFixtures(project(":microsphere-alibaba-druid-core")))
    testImplementation(testFixtures(project(":microsphere-alibaba-druid-spring")))

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly(libs.h2)
    testRuntimeOnly(libs.logback.classic)
}

