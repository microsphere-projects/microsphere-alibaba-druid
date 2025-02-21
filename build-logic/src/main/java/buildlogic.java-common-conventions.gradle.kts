plugins {
    `java`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
    }
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        url = uri("https://maven.aliyun.com/repository/gradle-plugin")
    }
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    registerFeature("optional") {
        usingSourceSet(sourceSets["main"])
    }

    withJavadocJar()
    withSourcesJar()
}

tasks.javadoc {
    if (JavaVersion.current().isJava8Compatible) {
        options {
            this as StandardJavadocDocletOptions
            addBooleanOption("Xdoclint:none", true)
            addBooleanOption("quiet", true)
        }
    }
}
