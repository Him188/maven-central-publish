package me.him188.maven.central.publish.gradle.configuration

import me.him188.maven.central.publish.gradle.tasks.CheckPublicationCredentials
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CredentialsTest : AbstractPluginConfigurationTest() {
    private val taskName = CheckPublicationCredentials.TASK_NAME

    @Test
    fun `provide credentials by project property`() {
        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments(
                taskName,
                "-PPUBLICATION_CREDENTIALS=$credentialsHex",
                "--stacktrace"
            )
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":$taskName")?.outcome)
    }

    @Test
    fun `provide credentials by system env`() {
        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments(
                taskName,
                "--stacktrace"
            ).withEnvironment(mapOf("publication.credentials" to credentialsHex))
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":$taskName")?.outcome)
    }

    @Test
    fun `provide credentials by project property2`() {
        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments(
                taskName,
                "-Ppublication.credentials=$credentialsHex",
                "--stacktrace"
            )
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":$taskName")?.outcome)
    }

    @Test
    fun `provide credentials by system env2`() {
        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments(
                taskName,
                "--stacktrace"
            ).withEnvironment(mapOf("publication.credentials" to credentialsHex))
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":$taskName")?.outcome)
    }
}