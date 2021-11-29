package me.him188.maven.central.publish.gradle.configuration

import me.him188.maven.central.publish.gradle.AbstractPluginTest
import me.him188.maven.central.publish.gradle.MavenCentralPublishPlugin
import me.him188.maven.central.publish.gradle.createTempDirSmart
import org.junit.jupiter.api.BeforeEach
import java.io.File

abstract class AbstractPluginConfigurationTest : AbstractPluginTest() {
    val tempDir: File by lazy { createTempDirSmart() }

    lateinit var buildFile: File
    lateinit var settingsFile: File
    lateinit var propertiesFile: File


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