import groovy.lang.Closure.DELEGATE_FIRST

plugins {
    `java-platform`
    `maven-publish`
}

description = "Microsphere Alibaba Druid Dependencies"

dependencies {
    constraints {
        project.rootProject.subprojects.forEach {
            val subproject = it as Project
            if (subproject.name != project.name) {
                api(it)
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
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
        }
    }
}