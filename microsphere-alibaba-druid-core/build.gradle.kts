plugins {
    id("buildlogic.java-library-conventions")
}

project.description = "Microsphere Alibaba Druid Code"

dependencies {
    // BOM
    // Microsphere Java Dependencies(BOM)
    implementation(platform(libs.microsphere.java.dependencies))

    // Microsphere Java Code
    "optionalApi"("io.github.microsphere-projects:microsphere-java-core")

    // Alibaba Druid
    "optionalApi"(libs.druid)

    // Testing
    testImplementation(project(":microsphere-alibaba-druid-test"))
}
