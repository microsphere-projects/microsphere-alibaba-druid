
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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
    registerFeature("optional") {
        usingSourceSet(sourceSets["main"])
    }

    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-source", "8", "-target", "8"))
}

tasks.javadoc {
    if (JavaVersion.current().isJava8Compatible) {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "quiet")
    }
}
