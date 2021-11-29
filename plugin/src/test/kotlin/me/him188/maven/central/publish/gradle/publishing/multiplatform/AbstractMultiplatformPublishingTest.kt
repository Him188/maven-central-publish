package me.him188.maven.central.publish.gradle.publishing.multiplatform

import me.him188.maven.central.publish.gradle.createTempDirSmart
import me.him188.maven.central.publish.gradle.publishing.AbstractPublishingTest
import me.him188.maven.central.publish.gradle.publishing.Verifier
import me.him188.maven.central.publish.gradle.publishing.module

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

    /**
     * Verifies:
     * - `.klib`
     * - `-javadoc.jar`
     * - `-sources.jar`
     * - `-metadata.jar`
     * - `.module`
     * - `.pom`
     */
    fun verifyModuleNative(
        groupId: String,
        moduleName: String,
        version: String,
        expected: Boolean,
        withMetadata: Boolean = true,
        verifier: Verifier = {}
    ) = verifyModule(groupId, moduleName, version, expected) {
        verifier()
        verify("$module-$version.klib")
        verify("$module-$version-javadoc.jar")
        if (withMetadata) verify("$module-$version-metadata.jar")
    }

    /**
     * Verifies:
     * - `-samplessources.jar`
     * - `-javadoc.jar`
     * - `-sources.jar`
     * - `.module`
     * - `.pom`
     */
    fun verifyModuleJs(
        groupId: String,
        moduleName: String,
        version: String,
        expected: Boolean,
        verifier: Verifier = {}
    ) = verifyModule(groupId, moduleName, version, expected) {
        verifier()
        verify("$module-$version-samplessources.jar")
        verify("$module-$version-javadoc.jar")
    }

    fun testMultiplatformConsume(
        packageName: String,
        group: String,
        name: String,
        version: String,
        kotlinVersion: String = kotlinVersionForTests,
    ) {
        val consumerDir = createTempDirSmart()
        consumerDir.mkdirs()
        consumerDir.resolve("gradle.properties").writeText(
            """
                kotlin.code.style=official
                kotlin.mpp.enableGranularSourceSetsMetadata=true
                kotlin.native.enableDependencyPropagation=false
            """.trimIndent()
        )
        consumerDir.resolve("src/commonMain/kotlin/").mkdirs()
        consumerDir.resolve("src/commonMain/kotlin/main.kt")
            .writeText("import $packageName.Test; \nfun main() { println(Test.toString()) }")
        consumerDir.resolve("build.gradle.kts").writeText(
            """
                plugins {
                    kotlin("multiplatform") version "$kotlinVersion"
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

        assertGradleTaskSuccess(consumerDir, "assemble")
    }

    fun testMultiplatformJvmOnlyConsume(
        packageName: String,
        group: String,
        name: String,
        version: String,
        kotlinVersion: String = kotlinVersionForTests,
    ) {
        val consumerDir = createTempDirSmart()
        consumerDir.mkdirs()
        consumerDir.resolve("gradle.properties").writeText(
            """
                kotlin.code.style=official
                kotlin.mpp.enableGranularSourceSetsMetadata=true
                kotlin.native.enableDependencyPropagation=false
            """.trimIndent()
        )
        consumerDir.resolve("src/commonMain/kotlin/").mkdirs()
        consumerDir.resolve("src/commonMain/kotlin/main.kt")
            .writeText("import $packageName.Test; \nfun main() { println(Test.toString()) }")
        consumerDir.resolve("build.gradle.kts").writeText(
            """
                plugins {
                    kotlin("multiplatform") version "$kotlinVersion"
                }
                repositories { mavenCentral(); mavenLocal() }
                kotlin {
                    jvm {
                        compilations.all { kotlinOptions.jvmTarget = "1.8" }
                        testRuns["test"].executionTask.configure { useJUnit() }
                    }
                }
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

        assertGradleTaskSuccess(consumerDir, "assemble")
    }
}