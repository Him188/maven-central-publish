package me.him188.maven.central.publish.gradle.configuration

import me.him188.maven.central.publish.gradle.AbstractPluginTest
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter

class KotlinTransitiveDependenciesIntegrationTest : AbstractPluginTest() {
    @Test
    fun `user can override Kotlin plugin version`(@TempDir dir: File) {
        // We're packaging the plugin with kotlin transitive dependencies > 1.4.30
        // This is testing that users are free to use different versions
        val userSpecifiedKotlinPluginVersion = "1.5.10"
        dir.resolve("settings.gradle").writeText("")
        dir.resolve("build.gradle").writeText(
            """
            plugins {
                id 'net.mamoe.maven-central-publish'
                id 'org.jetbrains.kotlin.jvm' version '$userSpecifiedKotlinPluginVersion'
            }
            
            repositories {
                mavenCentral()
            }
        """.trimIndent()
        )

        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()

        GradleRunner.create()
            .withProjectDir(dir)
            .withGradleVersion(gradleVersionForTests)
            .withPluginClasspath()
            .forwardStdOutput(PrintWriter(stdout))
            .forwardStdError(PrintWriter(stderr))
            .withArguments(listOf("dependencies", "--stacktrace"))
            .build()

        println(stdout)
        System.err.println(stderr)

        Assertions.assertTrue(
            stdout.toString()
                .contains("org.jetbrains.kotlin:kotlin-compiler-embeddable:${userSpecifiedKotlinPluginVersion}")
        )
    }
}