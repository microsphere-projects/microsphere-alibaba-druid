plugins {
    `java-test-fixtures`
    `jvm-test-suite`
    jacoco
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter()

            dependencies {
                implementation(project.sourceSets.named<SourceSet>("main").get().compileClasspath)
                runtimeOnly(project.sourceSets.named<SourceSet>("test").get().runtimeClasspath)

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

tasks.withType<JacocoReport> {
    reports {
        xml.required = true
    }
}

