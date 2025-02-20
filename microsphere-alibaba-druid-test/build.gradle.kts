plugins {
    id("buildlogic.java-library-conventions")
}

description = "Microsphere Alibaba Druid Test"


dependencies {
    // BOM
    // Microsphere Java Dependencies(BOM)
    implementation(platform(libs.microsphere.java.dependencies))

    // Microsphere Java Code
    "optionalApi"("io.github.microsphere-projects:microsphere-java-core")

    // Alibaba Druid
    "optionalApi"(libs.druid)

    // Testing
    api(libs.junit.jupiter.engine)

    // H2 DataBase
    api(libs.h2)

    // Logback
    api(libs.logback.classic)

}