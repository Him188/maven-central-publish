package net.mamoe.him188.maven.central.publish.gradle.configuration

import net.mamoe.him188.maven.central.publish.gradle.AbstractPluginTest
import net.mamoe.him188.maven.central.publish.gradle.MavenCentralPublishPlugin.Companion.CHECK_PUBLICATION_CREDENTIALS
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CredentialsTest : AbstractPluginTest() {

    @Test
    fun `provide credentials by project property`() {
        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments(
                CHECK_PUBLICATION_CREDENTIALS,
                "-P" + "PUBLICATION_CREDENTIALS=$credentialsHex",
                "--stacktrace"
            )
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":$CHECK_PUBLICATION_CREDENTIALS")?.outcome)
    }

    @Test
    fun `provide credentials by system env`() {
        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments(
                CHECK_PUBLICATION_CREDENTIALS,
                "--stacktrace"
            ).withEnvironment(mapOf("PUBLICATION.CREDENTIALS" to credentialsHex))
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":$CHECK_PUBLICATION_CREDENTIALS")?.outcome)
    }

    @Test
    fun `provide credentials by project property2`() {
        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments(
                CHECK_PUBLICATION_CREDENTIALS,
                "-P" + "publication.credentials=$credentialsHex",
                "--stacktrace"
            )
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":$CHECK_PUBLICATION_CREDENTIALS")?.outcome)
    }

    @Test
    fun `provide credentials by system env2`() {
        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments(
                CHECK_PUBLICATION_CREDENTIALS,
                "--stacktrace"
            ).withEnvironment(mapOf("publication.credentials" to credentialsHex))
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":$CHECK_PUBLICATION_CREDENTIALS")?.outcome)
    }
}