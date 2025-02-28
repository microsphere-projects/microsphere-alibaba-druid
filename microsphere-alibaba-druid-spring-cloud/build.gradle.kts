plugins {
    `java-library`
    `java-test-fixtures`
    `jvm-test-suite`
}

description = "Microsphere Alibaba Druid Spring Cloud"
val microsphereSpringCloudDependenciesVersion by extra {
    project.findProperty("microsphere-spring-cloud-dependencies.version")
}
val springcloudVersion by extra {
    project.findProperty("spring-cloud.version")
}
dependencies {
    // BOM
    // Microsphere Spring Cloud Dependencies (BOM)
    implementation(platform("io.github.microsphere-projects:microsphere-spring-cloud-dependencies:$microsphereSpringCloudDependenciesVersion"))
    // Spring Cloud Dependencies (BOM)
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springcloudVersion"))

    // Microsphere Alibaba Druid Spring Boot
    implementation(project(":microsphere-alibaba-druid-spring-boot"))

    // Microsphere Spring Cloud Commons
    implementation("io.github.microsphere-projects:microsphere-spring-cloud-commons")

    // Alibaba Druid
    implementation("com.alibaba:druid:1.2.20")


    // Spring Cloud
    implementation("org.springframework.cloud:spring-cloud-commons")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    // Testing
    testImplementation(testFixtures(project(":microsphere-alibaba-druid-spring")))

    // Spring Boot Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
