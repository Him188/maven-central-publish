package net.mamoe.him188.maven.central.publish.gradle.publishing.multiplatform

import net.mamoe.him188.maven.central.publish.gradle.credentialsHex
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HierarchicalJvmTest : AbstractMultiplatformPublishingTest() {

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

        val result = GradleRunner.create()
            .withProjectDir(publisherDir)
            .withArguments(
                "clean",
                "build",
                "publishToMavenLocal",
                "--stacktrace",
                "-PPUBLICATION_CREDENTIALS=$credentialsHex",
            )
            .withGradleVersion("7.1")
            .withPluginClasspath()
            .forwardOutput()
            .withEnvironment(System.getenv())
            .runCatching {
                build()
            }.onFailure {
                println("Failed to publish")
                publisherDir.walk().forEach { println(it) }
            }.getOrThrow()
        assertEquals(TaskOutcome.SUCCESS, result.task(":publishToMavenLocal")!!.outcome)

        /*
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0-javadoc.jar
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0-javadoc.jar.asc
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0-sources.jar
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0-sources.jar.asc
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0.jar
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0.jar.asc
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0.module
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0.module.asc
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0.pom
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0.pom.asc
         */
        mavenLocal.resolve(group).walk().forEach { println(it) }

        fun verifyModule(module: String, additional: (module: String, dir: File) -> Unit = { _, _ -> }) {
            val dir = mavenLocal.resolve(group).resolve(module).resolve(version)
            assertTrue(dir.exists())
            println(dir.absolutePath)
            assertTrue { dir.resolve("$module-$version-sources.jar").exists() }
            assertTrue { dir.resolve("$module-$version-sources.jar.asc").exists() }
            assertTrue { dir.resolve("$module-$version.module").exists() }
            assertTrue { dir.resolve("$module-$version.module.asc").exists() }
            assertTrue { dir.resolve("$module-$version.pom").exists() }
            assertTrue { dir.resolve("$module-$version.pom.asc").exists() }
            additional(module, dir)
        }
        verifyModule(name) { module, dir ->
            assertTrue { dir.resolve("$module-$version.jar").exists() }
            assertTrue { dir.resolve("$module-$version.jar.asc").exists() }
        }
        verifyModule("$name-jvm") { module, dir ->
            assertTrue { dir.resolve("$module-$version.jar").exists() }
            assertTrue { dir.resolve("$module-$version.jar.asc").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar.asc").exists() }
        }
        fun verifyNativeModule(target: String) {
            return verifyModule("$name-${target.toLowerCase()}") { module, dir ->
                assertTrue { dir.resolve("$module-$version.klib").exists() }
                assertTrue { dir.resolve("$module-$version.klib.asc").exists() }
                assertTrue { dir.resolve("$module-$version-javadoc.jar").exists() }
                assertTrue { dir.resolve("$module-$version-javadoc.jar.asc").exists() }
            }
        }

        val hostOs = System.getProperty("os.name")
        when {
            hostOs == "Mac OS X" -> {
                verifyNativeModule("iosArm64")
                verifyNativeModule("iosArm32")
                verifyNativeModule("iosX64")
                verifyNativeModule("macosX64")
                verifyNativeModule("tvosArm64")
                verifyNativeModule("tvosX64")
                verifyNativeModule("watchosArm32")
                verifyNativeModule("watchosArm64")
                verifyNativeModule("watchosX86")

                verifyNativeModule("linuxX64")
            }
            hostOs == "Linux" -> {
                verifyNativeModule("linuxX64")
            }
            hostOs.startsWith("Windows") -> {
                verifyNativeModule("linuxX64")
                verifyNativeModule("mingwX64")
            }
            else -> {
            }
        }

        verifyModule("$name-js") { module, dir ->
            assertTrue { dir.resolve("$module-$version-samplessources.jar").exists() }
            assertTrue { dir.resolve("$module-$version-samplessources.jar.asc").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar.asc").exists() }
        }
        publisherDir.deleteRecursively()


        println("Publishing succeed.")

        testMppConsume(packageName, group, name, version)
    }

    @Disabled // TODO: 2021/8/16 this test failed
    @Test
    fun `can publish Kotlin MPP hierarchical JVM modules`() {
        val rand = Random.nextInt().absoluteValue
        val group = "group-id-mpp-${rand}"
        val name = "project-name"
        val version = "1.0.0"
        val packageName = "test${rand}"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("gradle.properties").writeText(
            """
            kotlin.code.style=official
            kotlin.incremental.multiplatform=true
            kotlin.mpp.enableGranularSourceSetsMetadata=true
            kotlin.native.enableDependencyPropagation=false
            systemProp.org.gradle.internal.publish.checksums.insecure=true
        """.trimIndent()
        )
        assertTrue { publisherDir.resolve("src/commonMain/kotlin/").mkdirs() }
        publisherDir.resolve("src/commonMain/kotlin/main.kt").writeText("package $packageName; \nobject Test;")

        publisherDir.resolve("build.gradle.kts").writeText(
            """
            import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
            plugins {
                id("net.mamoe.maven-central-publish")
                kotlin("multiplatform") version "1.5.10"
            }
            repositories { mavenCentral(); google() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                workingDir = File("${publisherDir.resolve("gpg").absolutePath.replace("\\", "\\\\")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
            kotlin {
                jvm("android") {
                    attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.androidJvm)
                    compilations.all { kotlinOptions.jvmTarget = "1.8" }
                    testRuns["test"].executionTask.configure { useJUnit() }
                }
                jvm("common") {
                    attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.common)
                    compilations.all { kotlinOptions.jvmTarget = "1.8" }
                    testRuns["test"].executionTask.configure { useJUnit() }
                }
                jvm("jvm") {
                    compilations.all { kotlinOptions.jvmTarget = "1.8" }
                    testRuns["test"].executionTask.configure { useJUnit() }
                }
                sourceSets {
                    val commonMain by getting {
                        dependencies {
                            api(kotlin("reflect"))
                        }
                    }
                    val androidMain by getting {
                        dependsOn(commonMain)
                        dependencies {
                            compileOnly("com.google.android:android:4.1.1.4")
                        }
                    }
                    val jvmMain by getting {
                    }
                }
            }
            afterEvaluate {
                tasks.getByName("compileKotlinCommon").enabled = false
                tasks.getByName("compileTestKotlinCommon").enabled = false
        
                tasks.getByName("compileCommonMainKotlinMetadata").enabled = false
                tasks.getByName("compileKotlinMetadata").enabled = false
                
//                tasks.findByName("generateMetadataFileForKotlinMultiplatformPublication")?.enabled = false
            }
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(publisherDir)
            .withArguments(
                "clean",
                "build",
                "publishToMavenLocal",
                "--stacktrace",
                "-PPUBLICATION_CREDENTIALS=$credentialsHex",
            )
            .withGradleVersion("7.1")
            .withPluginClasspath()
            .forwardOutput()
            .withEnvironment(System.getenv())
            .runCatching {
                build()
            }.onFailure {
                println("Failed to publish")
                publisherDir.walk().forEach { println(it) }
            }.getOrThrow()
        assertEquals(TaskOutcome.SUCCESS, result.task(":publishToMavenLocal")!!.outcome)

        mavenLocal.resolve(group).walk().forEach { println(it) }

        fun verifyModule(module: String, additional: (module: String, dir: File) -> Unit = { _, _ -> }) {
            val dir = mavenLocal.resolve(group).resolve(module).resolve(version)
            assertTrue(dir.exists())
            println(dir.absolutePath)
            assertTrue { dir.resolve("$module-$version-sources.jar").exists() }
            assertTrue { dir.resolve("$module-$version-sources.jar.asc").exists() }
            assertTrue { dir.resolve("$module-$version.module").exists() }
            assertTrue { dir.resolve("$module-$version.module.asc").exists() }
            assertTrue { dir.resolve("$module-$version.pom").exists() }
            assertTrue { dir.resolve("$module-$version.pom.asc").exists() }
            additional(module, dir)
        }
        verifyModule(name) { module, dir ->
            assertTrue { dir.resolve("$module-$version.jar").exists() }
            assertTrue { dir.resolve("$module-$version.jar.asc").exists() }
        }
        verifyModule("$name-jvm") { module, dir ->
            assertTrue { dir.resolve("$module-$version.jar").exists() }
            assertTrue { dir.resolve("$module-$version.jar.asc").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar.asc").exists() }
        }
        verifyModule("$name-native") { module, dir ->
            assertTrue { dir.resolve("$module-$version.klib").exists() }
            assertTrue { dir.resolve("$module-$version.klib.asc").exists() }
            assertTrue { dir.resolve("$module-$version-metadata.jar").exists() }
            assertTrue { dir.resolve("$module-$version-metadata.jar.asc").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar.asc").exists() }
        }
        verifyModule("$name-js") { module, dir ->
            assertTrue { dir.resolve("$module-$version-samplessources.jar").exists() }
            assertTrue { dir.resolve("$module-$version-samplessources.jar.asc").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar.asc").exists() }
        }
        publisherDir.deleteRecursively()


        println("Publishing succeed.")

        testMppConsume(packageName, group, name, version)
    }
}