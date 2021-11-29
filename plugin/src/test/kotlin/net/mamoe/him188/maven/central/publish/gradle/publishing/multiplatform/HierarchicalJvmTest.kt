package net.mamoe.him188.maven.central.publish.gradle.publishing.multiplatform

import net.mamoe.him188.maven.central.publish.gradle.publishing.mavenLocal
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.assertTrue

class HierarchicalJvmTest : AbstractMultiplatformPublishingTest() {

    @Disabled
    @Test
    fun `can publish Kotlin MPP hierarchical JVM modules`() {
        val rand = Random.nextInt().absoluteValue
        val group = "group-id-mpp-${rand}"
        val name = "project-name"
        val version = "1.0.0"
        val packageName = "test${rand}"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("gradle.properties").writeText(
            """
            kotlin.code.style=official
            kotlin.incremental.multiplatform=true
            kotlin.mpp.enableGranularSourceSetsMetadata=true
            kotlin.native.enableDependencyPropagation=false
            systemProp.org.gradle.internal.publish.checksums.insecure=true
        """.trimIndent()
        )
        assertTrue { publisherDir.resolve("src/commonMain/kotlin/").mkdirs() }
        publisherDir.resolve("src/commonMain/kotlin/main.kt").writeText("package $packageName; \nobject Test;")

        publisherDir.resolve("build.gradle.kts").writeText(
            """
            import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
            plugins {
                id("net.mamoe.maven-central-publish")
                kotlin("multiplatform") version "1.5.10"
            }
            repositories { mavenCentral(); google() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                workingDir = File("${publisherDir.resolve("gpg").absolutePath.replace("\\", "\\\\")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
            kotlin {
                jvm("android") {
                    attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.androidJvm)
                    compilations.all { kotlinOptions.jvmTarget = "1.8" }
                    testRuns["test"].executionTask.configure { useJUnit() }
                }
                jvm("common") {
                    attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.common)
                    compilations.all { kotlinOptions.jvmTarget = "1.8" }
                    testRuns["test"].executionTask.configure { useJUnit() }
                }
                jvm("jvm") {
                    compilations.all { kotlinOptions.jvmTarget = "1.8" }
                    testRuns["test"].executionTask.configure { useJUnit() }
                }
                sourceSets {
                    val commonMain by getting {
                        dependencies {
                            api(kotlin("reflect"))
                        }
                    }
                    val androidMain by getting {
                        dependsOn(commonMain)
                        dependencies {
                            compileOnly("com.google.android:android:4.1.1.4")
                        }
                    }
                    val jvmMain by getting {
                    }
                }
            }
            afterEvaluate {
                tasks.getByName("compileKotlinCommon").enabled = false
                tasks.getByName("compileTestKotlinCommon").enabled = false
        
                tasks.getByName("compileCommonMainKotlinMetadata").enabled = false
                tasks.getByName("compileKotlinMetadata").enabled = false
                
//                tasks.findByName("generateMetadataFileForKotlinMultiplatformPublication")?.enabled = false
            }
        """.trimIndent()
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
        }
    }
}