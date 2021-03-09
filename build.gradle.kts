@file:Suppress("UnstableApiUsage", "LocalVariableName")

plugins {
    kotlin("jvm") version "1.4.30"
    id("com.gradle.plugin-publish") version "0.13.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

allprojects {
    group = "net.mamoe.him188"
    description = "Configure publication to Maven Central for Gradle projects with minimal effort."
    version = extra("version")

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}