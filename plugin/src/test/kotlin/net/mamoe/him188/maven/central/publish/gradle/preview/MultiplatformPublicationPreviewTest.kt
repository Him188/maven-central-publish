package net.mamoe.him188.maven.central.publish.gradle.preview

import net.mamoe.him188.maven.central.publish.gradle.credentialsHex
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import kotlin.test.assertEquals

class MultiplatformPublicationPreviewTest : AbstractPublicationPreviewTest() {
    @Test
    fun `test Kotlin MPP`() {
        val group = "group-id"
        val name = "project-name"
        val version = "1.0.0"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("net.mamoe.maven-central-publish")
                kotlin("multiplatform") version "1.5.10"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                workingDir = File("${publisherDir.absolutePath.replace("\\", "\\\\")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
            kotlin {
                jvm {
                    compilations.all { kotlinOptions.jvmTarget = "1.8" }
                    testRuns["test"].executionTask.configure { useJUnit() }
                }
                js(BOTH) {
                    useCommonJs()
                }
                val hostOs = System.getProperty("os.name")
                val isMingwX64 = hostOs.startsWith("Windows")
                val nativeTarget = when {
                    hostOs == "Mac OS X" -> macosX64("native")
                    hostOs == "Linux" -> linuxX64("native")
                    isMingwX64 -> mingwX64("native")
                    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
                }
            }
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(publisherDir)
            .withArguments(
                "clean",
                "publicationPreview",
                "--stacktrace",
                "-PPUBLICATION_CREDENTIALS=$credentialsHex",
            )
            .withGradleVersion("7.1")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        val message =
            result.output.substringAfter("Publication Preview").substringBefore("Publication Preview End").trim()

        assertEquals(
            """
            Root module:
              GroupId: group-id
              ArtifactId: project-name
              Version: 1.0.0
            
            Your project targets multi platforms.
            Target platforms include: js, jvm, common, native
            Artifact ids are: 
            project-name-js
            project-name-jvm
            project-name-common
            project-name-native
            
            Gradle users can add multiplatform dependency in commonMain by `implementation("group-id:project-name:1.0.0")`.
            Gradle users can also add null dependency by `implementation("group-id:project-name:1.0.0")`.
            
            Maven users can only add JVM dependencies, including: jvm
            
            Maven users can add jvm dependency as the following:
            <dependency>
                <groupId>group-id</groupId>
                <artifactId>project-name-jvm</artifactId>
                <version>1.0.0</version>
            </dependency>
            """.trimIndent(),
            message
        )
    }

    @Test
    fun `test Kotlin MPP with multiple JVM modules`() {
        val group = "group-id"
        val name = "project-name"
        val version = "1.0.0"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("net.mamoe.maven-central-publish")
                kotlin("multiplatform") version "1.5.10"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                workingDir = File("${publisherDir.absolutePath.replace("\\", "\\\\")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
            kotlin {
                jvm("jvmBase") {
                    compilations.all { kotlinOptions.jvmTarget = "1.8" }
                    testRuns["test"].executionTask.configure { useJUnit() }
                }
                jvm("jvmDesktop") {
                    compilations.all { kotlinOptions.jvmTarget = "1.8" }
                    testRuns["test"].executionTask.configure { useJUnit() }
                }
                jvm("jvmAndroid") {
                    compilations.all { kotlinOptions.jvmTarget = "1.8" }
                    testRuns["test"].executionTask.configure { useJUnit() }
                }
                js(BOTH) {
                    useCommonJs()
                }
                val hostOs = System.getProperty("os.name")
                val isMingwX64 = hostOs.startsWith("Windows")
                val nativeTarget = when {
                    hostOs == "Mac OS X" -> macosX64("native")
                    hostOs == "Linux" -> linuxX64("native")
                    isMingwX64 -> mingwX64("native")
                    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
                }
                sourceSets {
                    val jvmBaseMain by getting
                    val jvmDesktopMain by getting {
                        dependsOn(jvmBaseMain)
                    }
                    val jvmAndroidMain by getting {
                        dependsOn(jvmBaseMain)
                    }
                }
            }
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(publisherDir)
            .withArguments(
                "clean",
                "publicationPreview",
                "--stacktrace",
                "-PPUBLICATION_CREDENTIALS=$credentialsHex",
            )
            .withGradleVersion("7.1")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        val message =
            result.output.substringAfter("Publication Preview").substringBefore("Publication Preview End").trim()

        assertEquals(
            """
            Root module:
              GroupId: group-id
              ArtifactId: project-name
              Version: 1.0.0
            
            Your project targets multi platforms.
            Target platforms include: js, jvmAndroid, jvmBase, jvmDesktop, common, native
            Artifact ids are: 
            project-name-js
            project-name-jvmAndroid
            project-name-jvmBase
            project-name-jvmDesktop
            project-name-common
            project-name-native
            
            Gradle users can add multiplatform dependency in commonMain by `implementation("group-id:project-name:1.0.0")`.
            Gradle users can also add null dependency by `implementation("group-id:project-name:1.0.0")`.
            
            Maven users can only add JVM dependencies, including: jvmAndroid, jvmBase, jvmDesktop
            
            Maven users can add jvmAndroid dependency as the following:
            <dependency>
                <groupId>group-id</groupId>
                <artifactId>project-name-jvmAndroid</artifactId>
                <version>1.0.0</version>
            </dependency>
            
            Maven users can add jvmBase dependency as the following:
            <dependency>
                <groupId>group-id</groupId>
                <artifactId>project-name-jvmBase</artifactId>
                <version>1.0.0</version>
            </dependency>
            
            Maven users can add jvmDesktop dependency as the following:
            <dependency>
                <groupId>group-id</groupId>
                <artifactId>project-name-jvmDesktop</artifactId>
                <version>1.0.0</version>
            </dependency>
            """.trimIndent(),
            message
        )
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    fun `test Kotlin MPP with multiple native targets`() {
        val group = "group-id"
        val name = "project-name"
        val version = "1.0.0"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("net.mamoe.maven-central-publish")
                kotlin("multiplatform") version "1.5.10"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                workingDir = File("${publisherDir.absolutePath.replace("\\", "\\\\")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
                publishPlatformArtifactsInRootModule = "jvm"
            }
            kotlin {
                jvm {
                    compilations.all { kotlinOptions.jvmTarget = "1.8" }
                    testRuns["test"].executionTask.configure { useJUnit() }
                }
                js(BOTH) {
                    useCommonJs()
                }
                macosX64()
                linuxX64()
                mingwX64() // this does not work on macOS so will be ignored on publishing
            }
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(publisherDir)
            .withArguments(
                "clean",
                "publicationPreview",
                "--stacktrace",
                "-PPUBLICATION_CREDENTIALS=$credentialsHex",
            )
            .withGradleVersion("7.1")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        val message =
            result.output.substringAfter("Publication Preview").substringBefore("Publication Preview End").trim()

        assertEquals(
            """
            Root module:
              GroupId: group-id
              ArtifactId: project-name
              Version: 1.0.0
            
            Your project targets multi platforms.
            Target platforms include: js, jvm, common, linuxX64, macosX64
            Artifact ids are: 
            project-name-js
            project-name-jvm
            project-name-common
            project-name-linuxX64
            project-name-macosX64
            
            Gradle users can add multiplatform dependency in commonMain by `implementation("group-id:project-name:1.0.0")`.
            Gradle users can also add jvm dependency by `implementation("group-id:project-name:1.0.0")`.
            
            Maven users can only add JVM dependencies, including: jvm
            
            Maven users can add jvm dependency as the following:
            <dependency>
                <groupId>group-id</groupId>
                <artifactId>project-name-jvm</artifactId>
                <version>1.0.0</version>
            </dependency>
            
            You have configured to publish jvm into root module.
            So, Maven users can also add jvm dependency as the following:
            <dependency>
                <groupId>group-id</groupId>
                <artifactId>project-name</artifactId>
                <version>1.0.0</version>
            </dependency>
            """.trimIndent(),
            message
        )
    }
}