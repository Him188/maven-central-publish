package net.mamoe.him188.maven.central.publish.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class AbstractPluginTest {
    companion object {
        val credentialsHex =
            AbstractPluginTest::class.java.classLoader.getResource("credentials.txt")?.readText()
                ?: error("Cannot find credentials.txt")
    }

    @TempDir
    lateinit var tempDir: File

    lateinit var buildFile: File
    lateinit var settingsFile: File
    lateinit var propertiesFile: File


    fun gradleRunner(): GradleRunner? {
        return GradleRunner.create()
            .withProjectDir(tempDir)
            .withGradleVersion("6.8.3")
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
                    mavenCentral()
                }
            }
        """
        )



        propertiesFile = File(tempDir, "gradle.properties")
        propertiesFile.delete()
//        val proxy = getProxy()
//        if (proxy != null) propertiesFile << """
//            |systemProp.http.proxyHost=${proxy.first}
//            |systemProp.http.proxyPort=${proxy.second}
//            |systemProp.https.proxyHost=${proxy.first}
//            |systemProp.https.proxyPort=${proxy.second}
//        """.stripMargin()


        buildFile = File(tempDir, "build.gradle")
        buildFile.delete()
        buildFile.writeText(
            """
            plugins {
                id 'net.mamoe.maven-central-publish'
                id 'org.jetbrains.kotlin.jvm' version '1.4.32'
            }
            repositories {
                mavenCentral()
            }
        """
        )
    }

    @AfterEach
    fun cleanup() {
        tempDir.deleteRecursively()
    }
}