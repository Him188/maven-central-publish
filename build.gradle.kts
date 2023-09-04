plugins {
    val kotlinVer = "1.8.21"
    kotlin("jvm") version kotlinVer
    kotlin("plugin.serialization") version kotlinVer apply false
    id("com.gradle.plugin-publish") version "1.2.1" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

allprojects {
    group = "me.him188"
    description = "Configure publication to Maven Central for Gradle projects with minimal effort."
    version = rootProject.properties["version"].toString()

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}