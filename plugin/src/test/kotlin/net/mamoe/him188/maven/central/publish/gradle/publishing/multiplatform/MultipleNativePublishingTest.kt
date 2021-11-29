package net.mamoe.him188.maven.central.publish.gradle.publishing.multiplatform

import net.mamoe.him188.maven.central.publish.gradle.publishing.mavenLocal
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.assertTrue

class MultipleNativePublishingTest : AbstractMultiplatformPublishingTest() {

    @Test
    fun `can publish Kotlin MPP with multiple native targets`() {
        val rand = Random.nextInt().absoluteValue
        val group = "group-id-mpp-${rand}"
        val name = "project-name"
        val version = "1.0.0"
        val packageName = "test${rand}"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("gradle.properties").writeText(
            """
            kotlin.code.style=official
            kotlin.mpp.enableGranularSourceSetsMetadata=true
            kotlin.native.enableDependencyPropagation=false
        """.trimIndent()
        )
        assertTrue { publisherDir.resolve("src/commonMain/kotlin/").mkdirs() }
        publisherDir.resolve("src/commonMain/kotlin/main.kt").writeText("package $packageName; \nobject Test;")

        publisherDir.resolve("compile-native-multiplatform.gradle").writeText(
            Thread.currentThread().contextClassLoader.getResource("compile-native-multiplatform.gradle")!!.readText()
        )
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("net.mamoe.maven-central-publish")
                kotlin("multiplatform") version "1.5.10"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                workingDir = File("${publisherDir.resolve("gpg").absolutePath.replace("\\", "\\\\")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
            kotlin {
                jvm {
                    compilations.all { kotlinOptions.jvmTarget = "1.8" }
                    testRuns["test"].executionTask.configure { useJUnit() }
                }
                js(BOTH) {
                    useCommonJs()
                }
            }
            apply(from="compile-native-multiplatform.gradle")
        """.trimIndent()
        )

        runPublishToMavenLocal()

        mavenLocal(group).walk().forEach { println(it) }

        projectScope(group, name, version, true) {
            verifyMetadata()
            verifyJs("js")
            verifyJvm("jvm")

            val hostOs = System.getProperty("os.name")
            when {
                hostOs == "Mac OS X" -> {
                    verifyNative("iosArm64")
                    verifyNative("iosArm32")
                    verifyNative("iosX64")
                    verifyNative("macosX64")
                    verifyNative("tvosArm64")
                    verifyNative("tvosX64")
                    verifyNative("watchosArm32")
                    verifyNative("watchosArm64")
                    verifyNative("watchosX86")

                    verifyNative("linuxX64")
                }
                hostOs == "Linux" -> {
                    verifyNative("linuxX64")
                }
                hostOs.startsWith("Windows") -> {
                    verifyNative("linuxX64")
                    verifyNative("mingwX64")
                }
                else -> {
                }
            }

            testJvmConsume(packageName)
            testMultiplatformConsume(packageName)
        }
    }

}