package me.him188.maven.central.publish.gradle.publishing.multiplatform

import me.him188.maven.central.publish.gradle.MavenCentralPublishPlugin
import me.him188.maven.central.publish.gradle.publishing.mavenLocal
import org.junit.jupiter.api.TestFactory
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.assertTrue

class PlatformModuleInRootTest : AbstractMultiplatformPublishingTest() {

    @TestFactory
    fun `can publish Kotlin MPP with common native and jvm in root module`() = createTestsForKotlinVersions {
        val rand = Random.nextInt().absoluteValue
        val group = "group-id-mpp-${rand}"
        val name = "project-name"
        val version = "1.0.0"
        val packageName = "test${rand}"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("gradle.properties").writeText(
            """
            kotlin.code.style=official
            kotlin.mpp.enableGranularSourceSetsMetadata=true
            kotlin.native.enableDependencyPropagation=false
        """.trimIndent()
        )
        assertTrue { publisherDir.resolve("src/commonMain/kotlin/").mkdirs() }
        publisherDir.resolve("src/commonMain/kotlin/main.kt").writeText("package $packageName; \nobject Test;")

        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("${MavenCentralPublishPlugin.PLUGIN_ID}")
                kotlin("multiplatform") version "$publisherVersion"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                workingDir = File("${publisherDir.resolve("gpg").absolutePath.replace("\\", "/")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
                publishPlatformArtifactsInRootModule = "jvm" // DIFF HERE!
            }
        """.trimIndent() + configureKotlinSourceSets
        )

        runPublishToMavenLocal()
        mavenLocal(group).walk().forEach { println(it) }

        projectScope(group, name, version, true) {
            verifyMetadata()
            verifyJvm("jvm")
            verifyNative("native")
            verifyJs("js")

            testJvmConsume(packageName)
            testMultiplatformConsume(packageName)

            // TODO: 2021/7/1 Test for maven consumers
        }
    }
}