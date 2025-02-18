plugins {
    id("buildlogic.java-library-conventions")
}

description = "Microsphere Alibaba Druid Spring Boot"

dependencies {
    // BOM

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
