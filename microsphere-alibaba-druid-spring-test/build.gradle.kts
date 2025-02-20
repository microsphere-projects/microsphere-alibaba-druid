plugins {
    id("buildlogic.java-library-conventions")
}

description = "Microsphere Alibaba Druid Spring Test"


dependencies {
    // BOM
    // Microsphere Java Dependencies(BOM)
    implementation(platform(libs.microsphere.java.dependencies))
    // Spring Framework BOM
    implementation(platform(libs.spring.framework.bom))
    // Microsphere Alibaba Druid Test
    api(project(":microsphere-alibaba-druid-test"))

    // Microsphere Java Code
    "optionalApi"("io.github.microsphere-projects:microsphere-java-core")

    // Alibaba Druid
    "optionalApi"(libs.druid)

    // Spring Framework
    api("org.springframework:spring-context")

    // Spring Test
    api("org.springframework:spring-test")

}