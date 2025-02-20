plugins {
    `maven-publish`
    `signing`
}

version = System.getProperty("revision", "0.0.1-SNAPSHOT")

subprojects {
    val isBom = name.endsWith("-dependencies")
    val javaPlugin = if (isBom) "java-platform" else "java"
    val pkg = if (isBom) "pom" else "jar"
    val componentName = if (isBom) "javaPlatform" else "java"

    apply(plugin = javaPlugin)
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    val mavenTaskName = "maven-${name}"
    
    publishing {
        publications {
            create<MavenPublication>(mavenTaskName) {

                from(components[componentName])

                pom {
                    groupId = "io.github.microsphere-projects"
                    name = project.name
                    description = project.name
                    version = System.getProperty("revision", "0.0.1-SNAPSHOT")
                    packaging = pkg
                    url = "https://github.com/microsphere-projects/microsphere-alibaba-druid"

                    organization {
                        name = "Microsphere"
                        url = "https://github.com/microsphere-projects"
                    }

                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }

                    developers {
                        developer {
                            id = "mercyblitz"
                            name = "Mercy Ma"
                            email = "mercyblitz@gmail.com"
                            organization = "Microsphere"
                            url = "https://github.com/mercyblitz"
                            roles = setOf("lead", "architect", "developer")
                        }
                    }

                    scm {
                        url = "git@github.com/microsphere-projects/microsphere-alibaba-druid.git"
                        connection = "scm:git:git@github.com/microsphere-projects/microsphere-alibaba-druid.git"
                        developerConnection =
                            "scm:git:ssh://git@github.com/microsphere-projects/microsphere-alibaba-druid.git"
                    }

                    issueManagement {
                        system = "GitHub"
                        url = "https://github.com/microsphere-projects/microsphere-alibaba-druid/issues"
                    }
                }

                if (!isBom) {
                    versionMapping {
                        usage("java-api") {
                            fromResolutionOf("runtimeClasspath")
                        }
                        usage("java-runtime") {
                            fromResolutionResult()
                        }
                    }
                }

                repositories {
                    maven {
                        name = "ossrh"
                        val releasesRepoUrl =
                            uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                        val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                        url =
                            uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                        credentials {
                            username = System.getenv("OSSRH_MAVEN_USERNAME")
                            password = System.getenv("OSSRH_MAVEN_PASSWORD")
                        }
                    }
                }
            }
        }
    }

    signing {
        val SIGNING_KEY_ID: String? by project
        val SIGNING_KEY: String? by project
        val SIGNING_PASSWORD: String? by project
        useInMemoryPgpKeys(SIGNING_KEY_ID, SIGNING_KEY, SIGNING_PASSWORD)
        sign(publishing.publications[mavenTaskName])
    }
}