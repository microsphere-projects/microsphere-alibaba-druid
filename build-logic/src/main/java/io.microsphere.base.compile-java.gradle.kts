plugins {
    java
}
val javaVersion: String = providers.environmentVariable("java.version").getOrElse("21")

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
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