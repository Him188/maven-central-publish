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
}