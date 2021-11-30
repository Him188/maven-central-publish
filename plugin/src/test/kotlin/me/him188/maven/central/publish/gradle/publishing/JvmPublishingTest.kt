package me.him188.maven.central.publish.gradle.publishing

import me.him188.maven.central.publish.gradle.MavenCentralPublishPlugin
import org.junit.jupiter.api.TestFactory
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.assertFails
import kotlin.test.assertTrue

class JvmPublishingTest : AbstractPublishingTest() {

    @TestFactory
    fun `can publish Kotlin JVM`() = createTestsForKotlinVersions {
        val rand = Random.nextInt().absoluteValue
        val group = "group-id"
        val name = "project-name"
        val version = "1.0.0"
        val packageName = "test${rand}"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                kotlin("jvm") version "$publisherVersion"
                id("${MavenCentralPublishPlugin.PLUGIN_ID}")
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

        assertTrue { publisherDir.resolve("src/main/kotlin/").mkdirs() }
        publisherDir.resolve("src/main/kotlin/main.kt").writeText("package $packageName; \nobject Test;")
        runPublishToMavenLocal()

        assertTrue { publisherDir.resolve("build/classes/kotlin/main/$packageName/Test.class").exists() }

        verifyModuleJvm(group, name, version, true)


        testJvmConsume(packageName, group, name, version, consumerVersion)
        testMavenConsume(packageName, group, name, version, consumerVersion)
    }

    @TestFactory
    fun `can publish Kotlin JVM with custom project coordinates`() = createTestsForKotlinVersions {
        val rand = Random.nextInt().absoluteValue
        val originalGroupId = "group-id-$rand"
        val originalArtifactId = "project-name"
        val originalVersion = "1.0.0"
        val packageName = "test${rand}"

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

        assertTrue { publisherDir.resolve("src/main/kotlin/").mkdirs() }
        publisherDir.resolve("src/main/kotlin/main.kt").writeText("package $packageName; \nobject Test;")

        runPublishToMavenLocal()

        assertTrue { publisherDir.resolve("build/classes/kotlin/main/$packageName/Test.class").exists() }

        verifyModuleJvm(originalGroupId, originalArtifactId, originalVersion, false)
        verifyModuleJvm(customGroupId, customArtifactId, customVersion, true)

        testJvmConsume(packageName, customGroupId, customArtifactId, customVersion, consumerVersion)
        testMavenConsume(packageName, customGroupId, customArtifactId, customVersion, consumerVersion)

        assertFails {
            testJvmConsume(packageName, originalGroupId, originalArtifactId, originalVersion, consumerVersion)
        }
        assertFails {
            testMavenConsume(packageName, originalGroupId, originalArtifactId, originalVersion, consumerVersion)
        }
    }
}