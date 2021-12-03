package me.him188.maven.central.publish.gradle.publishing.noProjectComponents

import me.him188.maven.central.publish.gradle.MavenCentralPublishPlugin
import me.him188.maven.central.publish.gradle.publishing.AbstractPublishingTest
import org.junit.jupiter.api.TestFactory
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.assertFails
import kotlin.test.assertTrue

class PublicationWithoutProjectComponentsTest : AbstractPublishingTest() {
    @TestFactory
    fun `without project component`() = createTestsForKotlinVersions {
        val rand = Random.nextInt().absoluteValue
        val group = "group-id-$rand"
        val name = "project-name"
        val version = "1.0.0"
        val packageName = "test${rand}"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                kotlin("jvm") version "$publisherVersion"
                id("${MavenCentralPublishPlugin.PLUGIN_ID}")
                id("com.github.johnrengelman.shadow") version "7.0.0" // for gradle 7.0+
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                addProjectComponents = false
                addSources = false
                addJavadoc = false
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

        verifyBase(group, name, version, true) {
            verify("$name-$version.jar", expected = false)
            verify("$name-$version-sources.jar", expected = false)
            verify("$name-$version-javadoc.jar", expected = false)
            verify("$name-$version.module", expected = false)
            verify("$name-$version.pom", expected = true)
        }

        assertFails { testJvmConsume(packageName, group, name, version, consumerVersion) }
        assertFails { testMavenConsume(packageName, group, name, version, consumerVersion) }
    }

}