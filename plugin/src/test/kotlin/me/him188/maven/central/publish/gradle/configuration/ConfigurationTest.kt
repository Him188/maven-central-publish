package me.him188.maven.central.publish.gradle.configuration

import me.him188.maven.central.publish.gradle.tasks.CheckMavenCentralPublication
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class ConfigurationTest : AbstractPluginConfigurationTest() {

    @Test
    fun `should fail if projectUrl not set`() {
        buildFile.appendText(
            """
            mavenCentralPublish {
                connection = "foo"
            }
        """.trimIndent()
        )

        assertGradleTaskOutcome(tempDir, CheckMavenCentralPublication.TASK_NAME, TaskOutcome.FAILED) {
            assertTrue { output.contains("'projectUrl' is not set") }
        }
    }

    @Test
    fun `should fail if connection not set`() {
        buildFile.appendText(
            """
            mavenCentralPublish {
                projectUrl = "foo"
            }
        """.trimIndent()
        )

        assertGradleTaskOutcome(tempDir, CheckMavenCentralPublication.TASK_NAME, TaskOutcome.FAILED) {
            assertTrue { output.contains("'connection' is not set") }
        }
    }

    @Test
    fun `should fail if developer not set`() {
        buildFile.appendText(
            """
            mavenCentralPublish {
                githubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
        """.trimIndent()
        )

        assertGradleTaskOutcome(tempDir, CheckMavenCentralPublication.TASK_NAME, TaskOutcome.FAILED) {
            assertTrue { output.contains("'developer' is not set") }
        }
    }

    @Test
    fun `should pass if correctly configured`() {
        buildFile.appendText(
            """
            mavenCentralPublish {
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
        """.trimIndent()
        )

        assertGradleTaskSuccess(tempDir, CheckMavenCentralPublication.TASK_NAME)
    }
}