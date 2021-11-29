package net.mamoe.him188.maven.central.publish.gradle.publishing.multiplatform

import net.mamoe.him188.maven.central.publish.gradle.publishing.Verifier
import net.mamoe.him188.maven.central.publish.gradle.publishing.mavenLocal
import net.mamoe.him188.maven.central.publish.gradle.publishing.module
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.assertTrue

class CommonNativePublishingTest : AbstractMultiplatformPublishingTest() {

    @Test
    fun `can publish Kotlin MPP with common native`() {
        val rand = Random.nextInt().absoluteValue
        val groupId = "group-id-mpp-${rand}"
        val artifactId = "project-name"
        val version = "1.0.0"
        val packageName = "test${rand}"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$artifactId"""")
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
                id("net.mamoe.maven-central-publish")
                kotlin("multiplatform") version "1.5.10"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$groupId"
            version = "$version"
            mavenCentralPublish {
                workingDir = File("${publisherDir.resolve("gpg").absolutePath.replace("\\", "\\\\")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
            $configureKotlinSourceSets
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
        mavenLocal(groupId).walk().forEach { println(it) }

        fun verifyModule(module: String, additional: Verifier) =
            verifyModuleMetadata(groupId, module, version, true, additional)

        verifyModule(artifactId) {
            verifyCommon()
            verify("$module-$version.jar")
            verify("$module-$version.jar.asc")
        }
        verifyModule("$artifactId-jvm") {
            verifyCommon()
            verify("$module-$version.jar")
            verify("$module-$version.jar.asc")
            verify("$module-$version-javadoc.jar")
            verify("$module-$version-javadoc.jar.asc")
        }
        verifyModule("$artifactId-native") {
            verifyCommon()
            verify("$module-$version.klib")
            verify("$module-$version.klib.asc")
            verify("$module-$version-javadoc.jar")
            verify("$module-$version-javadoc.jar.asc")
        }
        verifyModule("$artifactId-js") {
            verifyCommon()
            verify("$module-$version-samplessources.jar")
            verify("$module-$version-samplessources.jar.asc")
            verify("$module-$version-javadoc.jar")
            verify("$module-$version-javadoc.jar.asc")
        }

        publisherDir.deleteRecursively()

        println("Publishing succeed.")

        testJvmConsume(packageName, groupId, artifactId, version)
        testMultiplatformConsume(packageName, groupId, artifactId, version)
    }

    @Test
    fun `can publish Kotlin MPP with common native with custom project coordinates`() {
        val rand = Random.nextInt().absoluteValue
        val originalGroup = "group-id-mpp-${rand}"
        val originalName = "project-name"
        val originalVersion = "1.0.0"
        val packageName = "test${rand}"

        val customGroup = "custom-group-id"
        val customName = "custom-artifact-id"
        val customVersion = "9.9.9"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$originalName"""")
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
                id("net.mamoe.maven-central-publish")
                kotlin("multiplatform") version "1.5.10"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$originalGroup"
            version = "$originalVersion"
            mavenCentralPublish {
                groupId = "$customGroup"
                artifactId = "$customName"
                version = "$customVersion"
                workingDir = File("${publisherDir.resolve("gpg").absolutePath.replace("\\", "\\\\")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
            $configureKotlinSourceSets
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
        mavenLocal(customGroup).walk().forEach { println(it) }

        projectScope(originalGroup, originalName, originalVersion, false) {
            verifyMetadata()
            verifyJvm("jvm")
            verifyJs("js")
            verifyNative("native")
        }

        projectScope(customGroup, customName, customVersion, true) {
            verifyMetadata()
            verifyJvm("jvm")
            verifyJs("js")
            verifyNative("native")
            testJvmConsume(packageName)
            testMultiplatformConsume(packageName)
        }
    }
}