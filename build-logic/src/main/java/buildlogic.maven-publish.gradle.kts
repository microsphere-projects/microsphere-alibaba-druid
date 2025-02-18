
plugins {
    `maven-publish`
    signing
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {

            from(components["java"])

            pom {
                name = project.description
                description = project.description
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

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

            repositories {
                maven {
                    name = "ossrh"
                    val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                    url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
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
    sign(publishing.publications["mavenJava"])
}