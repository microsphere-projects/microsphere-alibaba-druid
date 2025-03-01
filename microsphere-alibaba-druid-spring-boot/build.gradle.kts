plugins {
    `java-library`
    `java-test-fixtures`
    `jvm-test-suite`
}

description = "Microsphere Alibaba Druid Spring Boot"
val microsphereJavaDependenciesVersion by extra {
    project.findProperty("microsphere-java-dependencies.version")
}
val microsphereSpringBootDependenciesVersion by extra {
    project.findProperty("microsphere-spring-boot-dependencies.version")
}
val springbootVersion by extra {
    project.findProperty("spring-boot.version")
}

dependencies {
    // BOM
    // Microsphere Spring Boot Dependencies (BOM)

    // Microsphere Spring Dependencies (BOM)
    implementation(platform("io.github.microsphere-projects:microsphere-java-dependencies:$microsphereJavaDependenciesVersion"))

    // Microsphere Java Dependencies (BOM)
    implementation(platform("io.github.microsphere-projects:microsphere-spring-boot-dependencies:$microsphereSpringBootDependenciesVersion"))
    // Microsphere Java Code
    // Spring Framework BOM
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springbootVersion"))


    // Microsphere Alibaba Druid Spring
    implementation(project(":microsphere-alibaba-druid-core"))

    implementation(project(":microsphere-alibaba-druid-spring"))
    // Microsphere Spring
    implementation("io.github.microsphere-projects:microsphere-spring-boot-core")

    // Alibaba Druid
    implementation("com.alibaba:druid:1.2.20")


    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    // Spring Boot Test
    testImplementation(testFixtures(project(":microsphere-alibaba-druid-core")))
    testImplementation(testFixtures(project(":microsphere-alibaba-druid-spring")))

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("com.h2database:h2:1.4.200")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.12")
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

            dependencies{
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
