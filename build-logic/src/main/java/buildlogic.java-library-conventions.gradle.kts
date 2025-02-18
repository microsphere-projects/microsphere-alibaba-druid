plugins {
    id("buildlogic.java-common-conventions")
    `java-library`
    id("buildlogic.java-testing")
    id("buildlogic.maven-publish")
}

tasks.javadoc {
    if (JavaVersion.current().isJava8Compatible) {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "quiet")
    }
}

