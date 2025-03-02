plugins {
    id("io.microsphere.component.java-library")
    id("io.microsphere.base.maven-publish")
}

project.description = "Microsphere Alibaba Druid Code"

dependencies {

    implementation(libs.druid)
    implementation("io.github.microsphere-projects:microsphere-java-core")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly(libs.h2)
    testRuntimeOnly(libs.logback.classic)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testFixturesImplementation(libs.druid)
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api")
    testFixturesImplementation("io.github.microsphere-projects:microsphere-java-core")

}

