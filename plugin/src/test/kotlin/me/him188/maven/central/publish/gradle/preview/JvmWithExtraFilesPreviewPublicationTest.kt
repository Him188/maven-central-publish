package me.him188.maven.central.publish.gradle.preview

import me.him188.maven.central.publish.gradle.MavenCentralPublishPlugin
import me.him188.maven.central.publish.gradle.tasks.PreviewPublication
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

class JvmWithExtraFilesPreviewPublicationTest : AbstractPreviewPublicationTest() {
    @TestFactory
    fun `test Kotlin JVM`() = createTestsForKotlinVersions {
        val group = "group-id"
        val name = "project-name"
        val version = "1.0.0"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("${MavenCentralPublishPlugin.PLUGIN_ID}")
                kotlin("jvm") version "$publisherVersion"
                id("com.github.johnrengelman.shadow") version "7.0.0" // for gradle 7.0+
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

        assertGradleTaskSuccess(publisherDir, PreviewPublication.TASK_NAME) {
            val message = output.substringAfter("Publication Preview").substringBefore("Publication Preview End").trim()

            assertEquals(
                """
                Root module:
                  GroupId: group-id
                  ArtifactId: project-name
                  Version: 1.0.0

                Your project targets JVM platform only.
                Gradle users can add dependency by `implementation("group-id:project-name:1.0.0")`.
                Maven users can add dependency as follows:
                <dependency>
                    <groupId>group-id</groupId>
                    <artifactId>project-name</artifactId>
                    <version>1.0.0</version>
                </dependency>

                There are some extra files that are going to be published:
                
                [jvm]
                project-name-1.0.0-all.jar  (extension=jar, classifier=all)
            """.trimIndent(),
                message
            )
        }
    }

    @TestFactory
    fun `test Kotlin JVM with custom coordinates`() = createTestsForKotlinVersions {
        val group = "group-id"
        val name = "project-name"
        val version = "1.0.0"

        val customGroupId = "custom-group-id"
        val customArtifactId = "custom-artifact-id"
        val customVersion = "9.9.9"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("${MavenCentralPublishPlugin.PLUGIN_ID}")
                kotlin("jvm") version "$publisherVersion"
                id("com.github.johnrengelman.shadow") version "7.0.0" // for gradle 7.0+
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                groupId = "$customGroupId"
                artifactId = "$customArtifactId"
                version = "$customVersion"
                workingDir = File("${publisherDir.resolve("gpg").absolutePath.replace("\\", "/")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
            tasks.withType(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
                archiveFileName.set("$customArtifactId-$customVersion-all.jar")
            }
        """.trimIndent()
        )

        assertGradleTaskSuccess(publisherDir, PreviewPublication.TASK_NAME) {
            val message = output.substringAfter("Publication Preview").substringBefore("Publication Preview End").trim()

            assertEquals(
                """
                Root module:
                  GroupId: custom-group-id
                  ArtifactId: custom-artifact-id
                  Version: 9.9.9

                Your project targets JVM platform only.
                Gradle users can add dependency by `implementation("custom-group-id:custom-artifact-id:9.9.9")`.
                Maven users can add dependency as follows:
                <dependency>
                    <groupId>custom-group-id</groupId>
                    <artifactId>custom-artifact-id</artifactId>
                    <version>9.9.9</version>
                </dependency>

                There are some extra files that are going to be published:
                
                [jvm]
                $customArtifactId-$customVersion-all.jar  (extension=jar, classifier=all)
            """.trimIndent(),
                message
            )
        }
    }
}