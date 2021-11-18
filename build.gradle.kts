plugins {
    val kotlinVer = "1.6.0"
    kotlin("jvm") version kotlinVer
    kotlin("plugin.serialization") version kotlinVer apply false
    id("com.gradle.plugin-publish") version "0.13.0" apply false
    id("com.github.johnrengelman.shadow") version "6.1.0" apply false
}

allprojects {
    group = "net.mamoe.him188"
    description = "Configure publication to Maven Central for Gradle projects with minimal effort."
    version = rootProject.properties["version"].toString()

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}