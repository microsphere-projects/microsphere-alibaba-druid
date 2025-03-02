plugins {
    `java-library`
    id("io.microsphere.base.compile-java")
    id("io.microsphere.base.test")
}

val microsphereJavaDependenciesVersion by extra {
    project.findProperty("microsphere-java-dependencies.version")
}
val microsphereSpringDependenciesVersion by extra {
    project.findProperty("microsphere-spring-dependencies.version")
}
val microsphereSpringBootDependenciesVersion by extra {
    project.findProperty("microsphere-spring-boot-dependencies.version")
}
val microsphereSpringCloudDependenciesVersion by extra {
    project.findProperty("microsphere-spring-cloud-dependencies.version")
}

val springFrameworkVersion by extra {
    project.findProperty("spring.version")
}
val springBootVersion by extra {
    project.findProperty("spring-boot.version")
}
val springCloudVersion by extra {
    project.findProperty("spring-cloud.version")
}


dependencies {
    implementation(platform("io.github.microsphere-projects:microsphere-java-dependencies:$microsphereJavaDependenciesVersion"))
    implementation(platform("io.github.microsphere-projects:microsphere-spring-dependencies:$microsphereSpringDependenciesVersion"))
    implementation(platform("io.github.microsphere-projects:microsphere-spring-boot-dependencies:$microsphereSpringBootDependenciesVersion"))
    implementation(platform("io.github.microsphere-projects:microsphere-spring-cloud-dependencies:$microsphereSpringCloudDependenciesVersion"))

    implementation(platform("org.springframework:spring-framework-bom:$springFrameworkVersion"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))

    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))

    implementation(platform("org.junit:junit-bom:5.12.0"))

    testFixturesImplementation(platform("io.github.microsphere-projects:microsphere-java-dependencies:$microsphereJavaDependenciesVersion"))
    testFixturesImplementation(platform("io.github.microsphere-projects:microsphere-spring-dependencies:$microsphereSpringDependenciesVersion"))
    testFixturesImplementation(platform("io.github.microsphere-projects:microsphere-spring-boot-dependencies:$microsphereSpringBootDependenciesVersion"))
    testFixturesImplementation(platform("io.github.microsphere-projects:microsphere-spring-cloud-dependencies:$microsphereSpringCloudDependenciesVersion"))

    testFixturesImplementation(platform("org.springframework:spring-framework-bom:$springFrameworkVersion"))
    testFixturesImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testFixturesImplementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))

    testFixturesImplementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))

    testFixturesImplementation(platform("org.junit:junit-bom:5.12.0"))
}