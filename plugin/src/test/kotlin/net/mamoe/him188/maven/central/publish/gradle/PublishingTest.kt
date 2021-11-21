package net.mamoe.him188.maven.central.publish.gradle

import org.gradle.api.internal.artifacts.mvnsettings.DefaultLocalMavenRepositoryLocator
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenFileLocations
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenSettingsProvider
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

val mavenLocal by lazy {
    DefaultLocalMavenRepositoryLocator(DefaultMavenSettingsProvider(DefaultMavenFileLocations())).localMavenRepository
}

class PublishingTest {
    @TempDir
    lateinit var publisherDir: File

    @TempDir
    lateinit var consumerDir: File

    @Test
    fun `can publish Kotlin JVM`() {
        val group = "group-id"
        val name = "project-name"
        val version = "1.0.0"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("net.mamoe.maven-central-publish")
                kotlin("jvm") version "1.5.10"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                workingDir = File("${publisherDir.absolutePath.replace("\\", "\\\\")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(publisherDir)
            .withArguments(
                "clean",
                "publishToMavenLocal",
                "--stacktrace",
                "-PPUBLICATION_CREDENTIALS=$credentialsHex",
            )
            .withGradleVersion("7.1")
            .withPluginClasspath()
            .forwardOutput()
            .build()

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
        val dir = mavenLocal.resolve(group).resolve(name).resolve(version)
        assertTrue(dir.exists())
        assertTrue { dir.resolve("$name-$version-javadoc.jar").exists() }
        assertTrue { dir.resolve("$name-$version-javadoc.jar.asc").exists() }
        assertTrue { dir.resolve("$name-$version-sources.jar").exists() }
        assertTrue { dir.resolve("$name-$version-sources.jar.asc").exists() }
        assertTrue { dir.resolve("$name-$version.jar").exists() }
        assertTrue { dir.resolve("$name-$version.jar.asc").exists() }
        assertTrue { dir.resolve("$name-$version.module").exists() }
        assertTrue { dir.resolve("$name-$version.module.asc").exists() }
        assertTrue { dir.resolve("$name-$version.pom").exists() }
        assertTrue { dir.resolve("$name-$version.pom.asc").exists() }
        dir.deleteRecursively()
    }

    @Test
    fun `can publish Kotlin JVM with custom project coordinates`() {
        val group = "group-id"
        val name = "project-name"
        val version = "1.0.0"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("net.mamoe.maven-central-publish")
                kotlin("jvm") version "1.5.10"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                groupId = "custom-group-id"
                artifactId = "custom-artifact-id"
                version = "9.9.9"
                workingDir = File("${publisherDir.absolutePath.replace("\\", "\\\\")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(publisherDir)
            .withArguments(
                "clean",
                "publishToMavenLocal",
                "--stacktrace",
                "-PPUBLICATION_CREDENTIALS=$credentialsHex",
            )
            .withGradleVersion("7.1")
            .withPluginClasspath()
            .forwardOutput()
            .build()

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
        fun check(expectedGroup: String, expectedName: String, expectedVersion: String) {
            val dir = mavenLocal.resolve(expectedGroup).resolve(expectedName).resolve(expectedVersion)
            assertTrue(dir.exists())
            assertTrue { dir.resolve("$expectedName-$expectedVersion-javadoc.jar").exists() }
            assertTrue { dir.resolve("$expectedName-$expectedVersion-javadoc.jar.asc").exists() }
            assertTrue { dir.resolve("$expectedName-$expectedVersion-sources.jar").exists() }
            assertTrue { dir.resolve("$expectedName-$expectedVersion-sources.jar.asc").exists() }
            assertTrue { dir.resolve("$expectedName-$expectedVersion.jar").exists() }
            assertTrue { dir.resolve("$expectedName-$expectedVersion.jar.asc").exists() }
            assertTrue { dir.resolve("$expectedName-$expectedVersion.module").exists() }
            assertTrue { dir.resolve("$expectedName-$expectedVersion.module.asc").exists() }
            assertTrue { dir.resolve("$expectedName-$expectedVersion.pom").exists() }
            assertTrue { dir.resolve("$expectedName-$expectedVersion.pom.asc").exists() }

            assertFalse { dir.resolve("$name-$version-javadoc.jar").exists() }
            assertFalse { dir.resolve("$name-$version-javadoc.jar.asc").exists() }
            assertFalse { dir.resolve("$name-$version-sources.jar").exists() }
            assertFalse { dir.resolve("$name-$version-sources.jar.asc").exists() }
            assertFalse { dir.resolve("$name-$version.jar").exists() }
            assertFalse { dir.resolve("$name-$version.jar.asc").exists() }
            assertFalse { dir.resolve("$name-$version.module").exists() }
            assertFalse { dir.resolve("$name-$version.module.asc").exists() }
            assertFalse { dir.resolve("$name-$version.pom").exists() }
            assertFalse { dir.resolve("$name-$version.pom.asc").exists() }

            dir.deleteRecursively()
        }

        check("custom-group-id", "custom-artifact-name", "1.0.0")
    }

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

        testMppConsume(packageName, group, name, version)
    }


    @Test
    fun `can publish Kotlin MPP with common native with custome project coordinates`() {
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

        testMppConsume(packageName, originalGroup, originalName, originalVersion)
    }

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

    @Ignore // TODO: 2021/8/16 this test failed
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

    private val configureKotlinSourceSets = "\n" + """
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

    private fun testMppConsume(
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