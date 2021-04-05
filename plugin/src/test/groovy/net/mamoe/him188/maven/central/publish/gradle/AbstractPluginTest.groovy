package net.mamoe.him188.maven.central.publish.gradle

import kotlin.Pair
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir

abstract class AbstractPluginTest {
    static String credentialsHex = PublishingTest.classLoader.getResource("credentials.txt").readLines().join("\n")

    @TempDir
    public File tempDir
    File buildFile
    File settingsFile
    File propertiesFile

    private static Pair<String, Integer> getProxy() {
        if (System.getenv("user.name") == "Him188") new Pair<String, Integer>("127.0.0.1", 7890)
        else null
    }

    def gradleRunner() {
        GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .forwardOutput()
                .withEnvironment(System.getenv())
    }

    @BeforeEach
    void setup() {
        settingsFile = new File(tempDir, "settings.gradle")
        settingsFile.delete()
        settingsFile << """
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                }
            }
        """


        propertiesFile = new File(tempDir, "gradle.properties")
        propertiesFile.delete()
        def proxy = getProxy()
        if (proxy != null) propertiesFile << """
            |systemProp.http.proxyHost=${proxy.first}
            |systemProp.http.proxyPort=${proxy.second}
            |systemProp.https.proxyHost=${proxy.first}
            |systemProp.https.proxyPort=${proxy.second}
        """.stripMargin()


        buildFile = new File(tempDir, "build.gradle")
        buildFile.delete()
        buildFile << """
            plugins {
                id 'net.mamoe.maven-central-publish'
                id 'org.jetbrains.kotlin.jvm' version '1.4.32'
            }
            repositories {
                mavenLocal()
                mavenCentral()
                jcenter()
            }
        """
    }

    @AfterEach
    void cleanup() {
        tempDir.deleteDir()
    }
}
