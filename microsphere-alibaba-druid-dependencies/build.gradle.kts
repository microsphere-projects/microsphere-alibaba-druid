plugins {
    `java-platform`
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
