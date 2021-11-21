package net.mamoe.him188.maven.central.publish.gradle.configuration

import net.mamoe.him188.maven.central.publish.gradle.AbstractPluginTest
import net.mamoe.him188.maven.central.publish.gradle.MavenCentralPublishPlugin.Companion.CHECK_MAVEN_CENTRAL_PUBLICATION
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigurationTest : AbstractPluginTest() {

    @Test
    fun `should fail if projectUrl not set`() {
        buildFile.writeText(
            """
            mavenCentralPublish {
                connection = "foo"
            }
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments(
                CHECK_MAVEN_CENTRAL_PUBLICATION,
                "--stacktrace"
            )
            .withEnvironment(
                mapOf(
                    "PUBLICATION_CREDENTIALS" to credentialsHex
                )
            )
            .withPluginClasspath()
            .forwardOutput()
            .buildAndFail()

        assertEquals(TaskOutcome.FAILED, result.task(":$CHECK_MAVEN_CENTRAL_PUBLICATION")?.outcome)
        assertTrue { result.output.contains("'projectUrl' is not set") }
    }

    @Test
    fun `should fail if connection not set`() {
        buildFile.writeText(
            """
            mavenCentralPublish {
                projectUrl = "foo"
            }
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments(
                CHECK_MAVEN_CENTRAL_PUBLICATION,
                "--stacktrace"
            )
            .withEnvironment(
                mapOf(
                    "PUBLICATION_CREDENTIALS" to credentialsHex
                )
            )
            .withPluginClasspath()
            .forwardOutput()
            .buildAndFail()

        assertEquals(TaskOutcome.FAILED, result.task(":$CHECK_MAVEN_CENTRAL_PUBLICATION")?.outcome)
        assertTrue { result.output.contains("'connection' is not set") }
    }

    @Test
    fun `should pass if correctly configured`() {
        buildFile.writeText(
            """
            mavenCentralPublish {
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments(
                CHECK_MAVEN_CENTRAL_PUBLICATION,
                "--stacktrace"
            )
            .withEnvironment(
                mapOf(
                    "PUBLICATION_CREDENTIALS" to credentialsHex
                )
            )
            .withPluginClasspath()
            .forwardOutput()
            .buildAndFail()

        assertEquals(TaskOutcome.SUCCESS, result.task(":$CHECK_MAVEN_CENTRAL_PUBLICATION")?.outcome)
    }
}