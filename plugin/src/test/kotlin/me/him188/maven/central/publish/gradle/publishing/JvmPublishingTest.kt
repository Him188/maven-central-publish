package me.him188.maven.central.publish.gradle.publishing

import me.him188.maven.central.publish.gradle.MavenCentralPublishPlugin
import org.junit.jupiter.api.TestFactory
import kotlin.random.Random

class JvmPublishingTest : AbstractPublishingTest() {

    @TestFactory
    fun `can publish Kotlin JVM`() = createTestsForKotlinVersions {
        val group = "group-id"
        val name = "project-name"
        val version = "1.0.0"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("${MavenCentralPublishPlugin.PLUGIN_ID}")
                kotlin("jvm") version "$publisherVersion"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                workingDir = File("${publisherDir.resolve("gpg").absolutePath.replace("\\", "/")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
        """.trimIndent()
        )

        runPublishToMavenLocal()

        /*
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0-javadoc.jar
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0-javadoc.jar.asc
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0-sources.jar
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0-sources.jar.asc
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0.jar
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0.jar.asc
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0.module
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0.module.asc
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0.pom
        C:\Users\Him188\.m2\repository\group-id\project-name\1.0.0\project-name-1.0.0.pom.asc
         */
        verifyModuleJvm(group, name, version, true)
    }

    @TestFactory
    fun `can publish Kotlin JVM with custom project coordinates`() = createTestsForKotlinVersions {
        val rand = Random.nextInt()
        val originalGroupId = "group-id-$rand"
        val originalArtifactId = "project-name"
        val originalVersion = "1.0.0"

        val customGroupId = "custom-group-id-$rand"
        val customArtifactId = "custom-artifact-id"
        val customVersion = "1.0.0"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$originalArtifactId"""")
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("${MavenCentralPublishPlugin.PLUGIN_ID}")
                kotlin("jvm") version "$publisherVersion"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$originalGroupId"
            version = "$originalVersion"
            mavenCentralPublish {
                groupId = "$customGroupId"
                artifactId = "$customArtifactId"
                version = "$customVersion"
                workingDir = File("${publisherDir.resolve("gpg").absolutePath.replace("\\", "/")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
        """.trimIndent()
        )

        runPublishToMavenLocal()

        verifyModuleJvm(originalGroupId, originalArtifactId, originalVersion, false)
        verifyModuleJvm(customGroupId, customArtifactId, customVersion, true)
    }
}