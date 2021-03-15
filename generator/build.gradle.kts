import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
}

kotlin {
    apply(from = rootProject.file("gradle/compile-native-multiplatform.gradle"))

    sourceSets {
        all {
            dependencies {
                api(project(":protocol"))
            }
        }
    }

    targets.configureEach {
        if (this !is KotlinNativeTarget) return@configureEach

        binaries {
            executable {
                entryPoint("net.mamoe.him188.maven.central.publish.generator.main")
            }
        }
    }
}