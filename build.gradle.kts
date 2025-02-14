/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("buildlogic.java-library-conventions")
    jacoco
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
    registerFeature("optional") {
        usingSourceSet(sourceSets["main"])
    }
}

dependencies {
    // BOM
    implementation(platform(libs.microsphere.java.dependencies))

    // Microsphere Java Code
    "optionalApi"("io.github.microsphere-projects:microsphere-java-core")

    // Alibaba Druid
    "optionalApi"(libs.druid)

    // Testing
    testImplementation(libs.junit.jupiter.engine)

    // H2 DataBase
    testImplementation(libs.h2)

    // Logback
    testImplementation(libs.logback.classic)
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required = true
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-source", "8", "-target", "8"))
}
