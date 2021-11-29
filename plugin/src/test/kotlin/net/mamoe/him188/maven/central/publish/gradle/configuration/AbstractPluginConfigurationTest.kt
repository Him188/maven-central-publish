package net.mamoe.him188.maven.central.publish.gradle.configuration

import net.mamoe.him188.maven.central.publish.gradle.AbstractPluginTest
import net.mamoe.him188.maven.central.publish.gradle.MavenCentralPublishPlugin
import net.mamoe.him188.maven.central.publish.gradle.createTempDirSmart
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import java.io.File

abstract class AbstractPluginConfigurationTest : AbstractPluginTest() {
    val tempDir: File by lazy { createTempDirSmart() }

    lateinit var buildFile: File
    lateinit var settingsFile: File
    lateinit var propertiesFile: File


    fun gradleRunner(): GradleRunner? {
        return GradleRunner.create()
            .withProjectDir(tempDir)
            .withGradleVersion(gradleVersionForTests)
            .withPluginClasspath()
            .forwardOutput()
    }

    @BeforeEach
    fun setup() {
        settingsFile = File(tempDir, "settings.gradle")
        settingsFile.delete()
        settingsFile.writeText(
            """
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                }
            }
        """
        )

        propertiesFile = File(tempDir, "gradle.properties")
        propertiesFile.delete()

        buildFile = File(tempDir, "build.gradle")
        buildFile.delete()
        buildFile.writeText(
            """
            plugins {
                id '${MavenCentralPublishPlugin.PLUGIN_ID}'
                id 'org.jetbrains.kotlin.jvm' version '$kotlinVersionForTests'
            }
            repositories {
                mavenCentral()
            }
        """
        )
    }
}