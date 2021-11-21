package net.mamoe.him188.maven.central.publish.gradle.publishing

import net.mamoe.him188.maven.central.publish.gradle.credentialsHex
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JvmPublishingTest : AbstractPublishingTest() {

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

        check("custom-group-id", "custom-artifact-id", "1.0.0")
    }
}