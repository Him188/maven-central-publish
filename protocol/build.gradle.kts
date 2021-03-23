import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
    apply(from = rootProject.file("gradle/compile-native-multiplatform.gradle"))

    sourceSets {
        commonMain {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:${extra.get("serialization")}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${extra.get("serialization")}")
            }
        }
    }

    targets.filter { it.platformType != KotlinPlatformType.native }.forEach { target ->
        target.compilations.all {
            kotlinOptions {
                apiVersion = "1.3"
                languageVersion = "1.4"
            }
        }
    }
}