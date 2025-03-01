plugins {
    `java-library`
    `java-test-fixtures`
    `jvm-test-suite`
}

description = "Microsphere Alibaba Druid Spring"
val microsphereJavaDependenciesVersion by extra {
    project.findProperty("microsphere-java-dependencies.version")
}
val microsphereSpringDependenciesVersion by extra {
    project.findProperty("microsphere-spring-dependencies.version")
}
val springFrameworkVersion by extra {
    project.findProperty("spring.version")
}
dependencies {
    // BOM
    // Microsphere Java Dependencies (BOM)
    implementation(platform("io.github.microsphere-projects:microsphere-java-dependencies:$microsphereJavaDependenciesVersion"))
    // Microsphere Spring Dependencies (BOM)
    implementation(platform("io.github.microsphere-projects:microsphere-spring-dependencies:$microsphereSpringDependenciesVersion"))
    // Microsphere Java Code
    // Spring Framework BOM
    implementation(platform("org.springframework:spring-framework-bom:$springFrameworkVersion"))

    // Microsphere Alibaba Druid
    implementation(project(":microsphere-alibaba-druid-core"))
    // Microsphere Spring
    implementation("io.github.microsphere-projects:microsphere-java-core")
    implementation("io.github.microsphere-projects:microsphere-spring-context")
    // Alibaba Druid
    implementation("com.alibaba:druid:1.2.20")


    // Spring Framework
    implementation("org.springframework:spring-beans")
    implementation("org.springframework:spring-context")


    // Spring Framework Test
    testImplementation("org.springframework:spring-test")
    testImplementation(testFixtures(project(":microsphere-alibaba-druid-core")))

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("com.h2database:h2:1.4.200")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.12")
    testFixturesImplementation(platform("org.springframework:spring-framework-bom:$springFrameworkVersion"))

    testFixturesImplementation("com.alibaba:druid:1.2.20")
    testFixturesImplementation("org.springframework:spring-beans")
    testFixturesImplementation("org.springframework:spring-context")

    testFixturesImplementation(testFixtures(project(":microsphere-alibaba-druid-core")))

}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        register<JvmTestSuite>("integrationTest") {
            sources {

            }
            useJUnitJupiter()

            dependencies {
                implementation(project.sourceSets.main.get().compileClasspath)
                runtimeOnly(project.sourceSets.test.get().runtimeClasspath)

                implementation(testFixtures(project(path)))

            }
            targets {
                all {
                    testTask.configure() {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}