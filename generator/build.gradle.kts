import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
}

kotlin {
    apply(from = rootProject.file("gradle/compile-native-multiplatform.gradle"))

    sourceSets {
        all {
            dependencies {
                api(project(":maven-central-publish-protocol"))
            }
        }
    }

    targets.configureEach {
        if (this !is KotlinNativeTarget) return@configureEach

        binaries {
            executable {
                entryPoint("me.him188.maven.central.publish.generator.main")
            }
        }
    }
}