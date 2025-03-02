plugins {
    id("io.microsphere.component.java-library")
    id("io.microsphere.base.maven-publish")

}

description = "Microsphere Alibaba Druid Spring"

dependencies {
    // Microsphere Alibaba Druid
    implementation(project(":microsphere-alibaba-druid-core"))
    // Microsphere Spring
    implementation("io.github.microsphere-projects:microsphere-java-core")
    implementation("io.github.microsphere-projects:microsphere-spring-context")
    // Alibaba Druid
    implementation(libs.druid)
    // Spring Framework
    implementation("org.springframework:spring-beans")
    implementation("org.springframework:spring-context")


    // Spring Framework Test
    testImplementation(testFixtures(project(":microsphere-alibaba-druid-core")))
    testImplementation("org.springframework:spring-test")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly(libs.h2)
    testRuntimeOnly(libs.logback.classic)


    testFixturesImplementation(testFixtures(project(":microsphere-alibaba-druid-core")))
    testFixturesImplementation(libs.druid)
    testFixturesImplementation("org.springframework:spring-beans")
    testFixturesImplementation("org.springframework:spring-context")
}
