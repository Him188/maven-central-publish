package net.mamoe.him188.maven.central.publish.gradle.publishing.multiplatform

import net.mamoe.him188.maven.central.publish.gradle.credentialsHex
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommonNativePublishingTest : AbstractMultiplatformPublishingTest() {

    @Test
    fun `can publish Kotlin MPP with common native`() {
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
            $configureKotlinSourceSets
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
        verifyModule("$name-native") { module, dir ->
            assertTrue { dir.resolve("$module-$version.klib").exists() }
            assertTrue { dir.resolve("$module-$version.klib.asc").exists() }
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

        testJvmConsume(packageName, group, name, version)
        testMppConsume(packageName, group, name, version)
    }

    @Test
    fun `can publish Kotlin MPP with common native with custom project coordinates`() {
        val rand = Random.nextInt().absoluteValue
        val originalGroup = "group-id-mpp-${rand}"
        val originalName = "project-name"
        val originalVersion = "1.0.0"
        val packageName = "test${rand}"

        val customGroup = "custom-group-id"
        val customName = "custom-artifact-id"
        val customVersion = "9.9.9"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$originalName"""")
        publisherDir.resolve("gradle.properties").writeText(
            """
            kotlin.code.style=official
            kotlin.mpp.enableGranularSourceSetsMetadata=true
            kotlin.native.enableDependencyPropagation=false
        """.trimIndent()
        )
        assertTrue { publisherDir.resolve("src/commonMain/kotlin/").mkdirs() }
        publisherDir.resolve("src/commonMain/kotlin/main.kt").writeText("package $packageName; \nobject Test;")

        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("net.mamoe.maven-central-publish")
                kotlin("multiplatform") version "1.5.10"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$originalGroup"
            version = "$originalVersion"
            mavenCentralPublish {
                groupId = "custom-group-id"
                artifactId = "custom-artifact-id"
                version = "9.9.9"
                workingDir = File("${publisherDir.resolve("gpg").absolutePath.replace("\\", "\\\\")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
            $configureKotlinSourceSets
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
        mavenLocal.resolve(customGroup).walk().forEach { println(it) }

        fun verifyModule(
            module: String,
            additional: (module: String, version: String, dir: File) -> Unit = { _, _, _ -> }
        ) {
            val dir = mavenLocal.resolve(customGroup).resolve(module).resolve(originalVersion)
            assertTrue(dir.exists())
            println(dir.absolutePath)

            assertTrue { dir.resolve("$module-$originalVersion-sources.jar").exists() }
            assertTrue { dir.resolve("$module-$originalVersion-sources.jar.asc").exists() }
            assertTrue { dir.resolve("$module-$originalVersion.module").exists() }
            assertTrue { dir.resolve("$module-$originalVersion.module.asc").exists() }
            assertTrue { dir.resolve("$module-$originalVersion.pom").exists() }
            assertTrue { dir.resolve("$module-$originalVersion.pom.asc").exists() }

            assertFalse { dir.resolve("$module-$originalVersion-sources.jar").exists() }
            assertFalse { dir.resolve("$module-$originalVersion-sources.jar.asc").exists() }
            assertFalse { dir.resolve("$module-$originalVersion.module").exists() }
            assertFalse { dir.resolve("$module-$originalVersion.module.asc").exists() }
            assertFalse { dir.resolve("$module-$originalVersion.pom").exists() }
            assertFalse { dir.resolve("$module-$originalVersion.pom.asc").exists() }

            additional(customName, customVersion, dir)
        }

        verifyModule(customName) { module, version, dir ->
            assertTrue { dir.resolve("$module-$version.jar").exists() }
            assertTrue { dir.resolve("$module-$version.jar.asc").exists() }
        }
        verifyModule("$customName-jvm") { module, version, dir ->
            assertTrue { dir.resolve("$module-$version.jar").exists() }
            assertTrue { dir.resolve("$module-$version.jar.asc").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar.asc").exists() }
        }
        verifyModule("$customName-native") { module, version, dir ->
            assertTrue { dir.resolve("$module-$version.klib").exists() }
            assertTrue { dir.resolve("$module-$version.klib.asc").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar.asc").exists() }
        }
        verifyModule("$customName-js") { module, version, dir ->
            assertTrue { dir.resolve("$module-$version-samplessources.jar").exists() }
            assertTrue { dir.resolve("$module-$version-samplessources.jar.asc").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar").exists() }
            assertTrue { dir.resolve("$module-$version-javadoc.jar.asc").exists() }
        }
        publisherDir.deleteRecursively()


        println("Publishing succeed.")

        testJvmConsume(packageName, originalGroup, originalName, originalVersion)
        testMppConsume(packageName, originalGroup, originalName, originalVersion)
    }

}