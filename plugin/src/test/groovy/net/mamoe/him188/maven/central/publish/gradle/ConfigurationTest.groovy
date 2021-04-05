package net.mamoe.him188.maven.central.publish.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test

import static net.mamoe.him188.maven.central.publish.gradle.MavenCentralPublishPlugin.CHECK_MAVEN_CENTRAL_PUBLICATION
import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class ConfigurationTest extends AbstractPluginTest {

    @Test
    void "should fail if projectUrl not set"() {
        buildFile << """
            mavenCentralPublish {
                connection = "foo"
            }
        """
        def result = GradleRunner.create()
                .withProjectDir(tempDir)
                .withArguments(
                        CHECK_MAVEN_CENTRAL_PUBLICATION,
                        '--stacktrace'
                )
                .withEnvironment([
                        "PUBLICATION_CREDENTIALS": credentialsHex
                ])
                .withPluginClasspath()
                .forwardOutput()
                .buildAndFail()

        assert result.task(":$CHECK_MAVEN_CENTRAL_PUBLICATION").outcome == FAILED
        assert result.output.contains("'projectUrl' is not set")
    }

    @Test
    void "should fail if connection not set"() {
        buildFile << """
            mavenCentralPublish {
                projectUrl = "foo"
            }
        """
        def result = GradleRunner.create()
                .withProjectDir(tempDir)
                .withArguments(
                        CHECK_MAVEN_CENTRAL_PUBLICATION,
                        '--stacktrace'
                )
                .withEnvironment([
                        "PUBLICATION_CREDENTIALS": credentialsHex
                ])
                .withPluginClasspath()
                .forwardOutput()
                .buildAndFail()

        assert result.task(":$CHECK_MAVEN_CENTRAL_PUBLICATION").outcome == FAILED
        assert result.output.contains("'connection' is not set")
    }

    @Test
    void "should pass if correctly configured"() {
        buildFile << """
            mavenCentralPublish {
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
        """
        def result = GradleRunner.create()
                .withProjectDir(tempDir)
                .withArguments(
                        CHECK_MAVEN_CENTRAL_PUBLICATION,
                        '--stacktrace'
                )
                .withEnvironment([
                        "PUBLICATION_CREDENTIALS": credentialsHex
                ])
                .withPluginClasspath()
                .forwardOutput()
                .build()

        assert result.task(":$CHECK_MAVEN_CENTRAL_PUBLICATION").outcome == SUCCESS
    }
}
