package net.mamoe.him188.maven.central.publish.gradle.publishing.multiplatform

import net.mamoe.him188.maven.central.publish.gradle.publishing.AbstractPublishingTest
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class AbstractMultiplatformPublishingTest : AbstractPublishingTest() {
    val configureKotlinSourceSets = "\n" + """
            kotlin {
                jvm {
                    compilations.all { kotlinOptions.jvmTarget = "1.8" }
                    testRuns["test"].executionTask.configure { useJUnit() }
                }
                js(BOTH) {
                    useCommonJs()
                }
                val hostOs = System.getProperty("os.name")
                val isMingwX64 = hostOs.startsWith("Windows")
                val nativeTarget = when {
                    hostOs == "Mac OS X" -> macosX64("native")
                    hostOs == "Linux" -> linuxX64("native")
                    isMingwX64 -> mingwX64("native")
                    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
                }
            }
    """.trimIndent()

    fun testMppConsume(
        packageName: String,
        group: String,
        name: String,
        version: String
    ) {
        consumerDir.mkdirs()
        consumerDir.resolve("gradle.properties").writeText(
            """
                kotlin.code.style=official
                kotlin.mpp.enableGranularSourceSetsMetadata=true
                kotlin.native.enableDependencyPropagation=false
            """.trimIndent()
        )
        assertTrue { consumerDir.resolve("src/commonMain/kotlin/").mkdirs() }
        consumerDir.resolve("src/commonMain/kotlin/main.kt")
            .writeText("import $packageName.Test; \nfun main() { println(Test.toString()) }")
        consumerDir.resolve("build.gradle.kts").writeText(
            """
                plugins {
                    kotlin("multiplatform") version "1.5.10"
                }
                repositories { mavenCentral(); mavenLocal() }
                $configureKotlinSourceSets
                kotlin {
                    sourceSets {
                        val commonMain by getting {
                             dependencies {
                                 api("$group:$name:$version")
                             }
                        }
                    }
                }
            """.trimIndent()
        )

        val result2 = GradleRunner.create()
            .withProjectDir(publisherDir)
            .withArguments(
                "clean",
                "assemble",
                "--stacktrace",
            )
            .withGradleVersion("7.1")
            .withPluginClasspath()
            .forwardOutput()
            .withEnvironment(System.getenv())
            .runCatching {
                build()
            }.onFailure {
                publisherDir.walk().forEach { println(it) }
            }.getOrThrow()
        assertEquals(TaskOutcome.SUCCESS, result2.task(":assemble")!!.outcome)
    }
}