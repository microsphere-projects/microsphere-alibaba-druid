plugins {
    `java-library`
    `java-test-fixtures`
    `jvm-test-suite`
}

project.description = "Microsphere Alibaba Druid Code"
val microsphereJavaDependenciesVersion by extra {
    project.findProperty("microsphere-java-dependencies.version")
}
/*
val integrationTest = sourceSets.create("integrationTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

configurations.getByName(integrationTest.implementationConfigurationName) {
    extendsFrom(
        configurations.implementation.get(),
        configurations.testImplementation.get()
    )
}
configurations.getByName(integrationTest.runtimeOnlyConfigurationName) {
    extendsFrom(
        configurations.runtimeOnly.get(),
        configurations.testRuntimeOnly.get()
    )
}*/

dependencies {
    // BOM
    // Microsphere Java Dependencies (BOM)
    implementation(platform("io.github.microsphere-projects:microsphere-java-dependencies:$microsphereJavaDependenciesVersion"))

    // Microsphere Java Code
    implementation("io.github.microsphere-projects:microsphere-java-core")
    // Alibaba Druid
    implementation("com.alibaba:druid:1.2.20")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("com.h2database:h2:1.4.200")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.12")

    testFixturesImplementation(platform("io.github.microsphere-projects:microsphere-java-dependencies:$microsphereJavaDependenciesVersion"))
    testFixturesImplementation("com.alibaba:druid:1.2.20")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testFixturesImplementation("io.github.microsphere-projects:microsphere-java-core")

//    "integrationTestImplementation"(project(path))

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
/*
tasks.test {
    useJUnitPlatform()

}
val integrationTestTask = tasks.register<Test>(integrationTest.name) {
    mustRunAfter(tasks.test)
    group = "verification"
    description = "Runs integration tests."

    testClassesDirs = sourceSets[integrationTest.name].output.classesDirs
    classpath = sourceSets[integrationTest.name].runtimeClasspath

    useJUnitPlatform()

}
tasks.check {
   dependsOn(integrationTestTask)
}*/
