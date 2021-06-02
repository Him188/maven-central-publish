package net.mamoe.him188.maven.central.publish.gradle


import org.junit.jupiter.api.Test

import static net.mamoe.him188.maven.central.publish.gradle.MavenCentralPublishPlugin.CHECK_PUBLICATION_CREDENTIALS
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class CredentialsTest extends AbstractPluginTest {
    @Test
    void "provide credentials by project property"() {
        def result = gradleRunner()
                .withArguments(
                        CHECK_PUBLICATION_CREDENTIALS,
                        '-P' + "PUBLICATION_CREDENTIALS=${credentialsHex}",
                        '--stacktrace'
                )
                .build()

        assert result.task(":$CHECK_PUBLICATION_CREDENTIALS").outcome == SUCCESS
    }

    @Test
    void "provide credentials by system env"() {
        def result = gradleRunner()
                .withArguments(
                        CHECK_PUBLICATION_CREDENTIALS,
                        '--stacktrace'
                )
                .withEnvironment([
                        "PUBLICATION_CREDENTIALS": credentialsHex
                ])
                .build()

        assert result.task(":$CHECK_PUBLICATION_CREDENTIALS").outcome == SUCCESS
    }
}
