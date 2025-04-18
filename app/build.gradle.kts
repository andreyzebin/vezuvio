import java.util.Properties

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.13/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    // id("io.freefair.lombok") version "8.12.2"
}

var logbackVersion = "1.5.6"
var slf4jVersion = "2.0.13"
var junitVersion = "5.10.1"

var version = "0.0.2"

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // This dependency is used by the application.
    implementation("io.github.andreyzebin:java-bash:0.0.14")
    implementation("io.github.andreyzebin:java-git:0.0.4")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.codehaus.janino:janino:3.1.12")

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    testCompileOnly("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    // if dev environment
    val isProd = (project.findProperty("isProduction") ?: 0) == 1
    if (!isProd) {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }
}

application {
    // Define the main class for the application.
    mainClass = "io.github.zebin.App"
    if ("true" == project.findProperty("isProduction")) {
        // Configure via properties file
        var props = Properties()
        props.load(File("../etc/vezuvio.properties").reader())
        applicationDefaultJvmArgs = listOf(
            "-Dlogger.root.level=${props.getProperty("logger.root.level")}",
            "-Dio.github.vezuvio.workingDirectory=${project.findProperty("workingDirectory")}",
            "-Dio.github.vezuvio.version=${version}"
        )
    } else {
        applicationDefaultJvmArgs = listOf(
            "-Dlogger.root.level=DEBUG",
            "-Dio.github.vezuvio.version=${version}"
        )
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
