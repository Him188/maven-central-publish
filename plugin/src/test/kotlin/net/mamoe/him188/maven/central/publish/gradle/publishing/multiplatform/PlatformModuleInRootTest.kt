package net.mamoe.him188.maven.central.publish.gradle.publishing.multiplatform

import net.mamoe.him188.maven.central.publish.gradle.credentialsHex
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlatformModuleInRootTest : AbstractMultiplatformPublishingTest() {

    @Test
    fun `can publish Kotlin MPP with common native and jvm in root module`() {
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
                publishPlatformArtifactsInRootModule = "jvm" // DIFF HERE!
            }
        """.trimIndent() + configureKotlinSourceSets
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
//            assertTrue { dir.resolve("$module-$version-metadata.jar").exists() } // not present on linux
//            assertTrue { dir.resolve("$module-$version-metadata.jar.asc").exists() }
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

        testMppConsume(packageName, group, name, version)
        // TODO: 2021/7/1 Test for maven consumers
    }
}