package net.mamoe.him188.maven.central.publish.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.test.assertEquals

abstract class AbstractPluginTest {
    companion object {
        val credentialsHex =
            AbstractPluginTest::class.java.classLoader.getResource("credentials.txt")?.readText()
                ?: error("Cannot find credentials.txt")

        val kotlinVersionForTests = "1.6.0"
        val gradleVersionForTests = "7.2"

        val supportingKotlinVersions = listOf(
            "1.4.31",
            "1.5.31",
            "1.6.0",
        )
    }

    fun assertGradleTaskSuccess(dir: File, taskName: String, resultAction: BuildResultScope.() -> Unit = {}) {
        assertGradleTaskOutcome(dir, taskName, TaskOutcome.SUCCESS, resultAction)
    }

    class BuildResultScope(
        private val delegate: BuildResult,
        private val taskName: String
    ) : BuildResult by delegate {
        val task = delegate.task(":$taskName")!!
    }

    fun assertGradleTaskOutcome(
        dir: File,
        taskName: String,
        outcome: TaskOutcome,
        resultAction: BuildResultScope.() -> Unit = {}
    ) {
        val result = runGradleBuild(dir, taskName, expectFailure = outcome == TaskOutcome.FAILED)
        assertEquals(outcome, result.task(":$taskName")!!.outcome)
        resultAction(BuildResultScope(result, taskName))
    }


    fun runGradleBuild(dir: File, vararg taskNames: String, expectFailure: Boolean = false): BuildResult {
        val result = GradleRunner.create()
            .withProjectDir(dir)
            .withArguments(
                "clean",
                "build",
                *taskNames,
                "--stacktrace",
                "-PPUBLICATION_CREDENTIALS=$credentialsHex",
            )
            .withGradleVersion("7.1")
            .withPluginClasspath()
            .forwardOutput()
            .withEnvironment(System.getenv())
            .runCatching {
                if (expectFailure) buildAndFail()
                else build()
            }.onFailure {
                println("Failed to ${taskNames.joinToString { "'$it'" }}")
                dir.walk().forEach { println(it) }
            }.getOrThrow()
        return result
    }

}