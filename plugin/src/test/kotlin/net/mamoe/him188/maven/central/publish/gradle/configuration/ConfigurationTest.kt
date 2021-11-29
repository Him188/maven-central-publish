package net.mamoe.him188.maven.central.publish.gradle.configuration

import net.mamoe.him188.maven.central.publish.gradle.MavenCentralPublishPlugin.Companion.CHECK_MAVEN_CENTRAL_PUBLICATION
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

        assertGradleTaskOutcome(tempDir, CHECK_MAVEN_CENTRAL_PUBLICATION, TaskOutcome.FAILED) {
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

        assertGradleTaskOutcome(tempDir, CHECK_MAVEN_CENTRAL_PUBLICATION, TaskOutcome.FAILED) {
            assertTrue { output.contains("'connection' is not set") }
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

        assertGradleTaskSuccess(tempDir, CHECK_MAVEN_CENTRAL_PUBLICATION)
    }
}