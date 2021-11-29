package net.mamoe.him188.maven.central.publish.gradle.tasks

import net.mamoe.him188.maven.central.publish.gradle.mcExt
import org.gradle.api.DefaultTask
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

open class PreviewPublication : DefaultTask() {
    companion object {
        const val TASK_NAME = "previewPublication"
    }

    private val ext get() = project.mcExt

    @Internal
    override fun getDescription(): String = "See how your published projects will be."

    @TaskAction
    fun printPreview() {
        println(generatePreview())
    }

    fun generatePreview(): String = ext.run {
        return buildString {
            appendLine(
                """
                    Publication Preview
                    
                    Root module:
                      GroupId: $groupId
                      ArtifactId: $artifactId
                      Version: $version
                """.trimIndent()
            )
            appendLine()

            project.extensions.findByType(KotlinJvmProjectExtension::class.java)?.let {
                appendLine("Your project targets JVM platform only.")

                appendLine(
                    """
                        Gradle users can add dependency by `implementation("$groupId:$artifactId:$version")`repository declared.
                    """.trimIndent()
                )

                appendLine(
                    """
                        Maven users can add dependency as follows:
                        <dependency>
                            <groupId>${ext.groupId}</groupId>
                            <artifactId>${ext.artifactId}</artifactId>
                            <version>${ext.version}</version>
                        </dependency>
                    """.trimIndent()
                )
            }

            fun publicationNameToPlatformName(name: String) = when (name) {
                "kotlinMultiplatform" -> "common"
                "metadata" -> "common"
                else -> name
            }

            project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.let { kotlin ->
                appendLine("Your project targets multi platforms.")
                val publications = project.extensions.findByType(PublishingExtension::class.java)?.publications
                if (publications == null) {
                    appendLine("Internal Error: failed to find PublishingExtension.")
                    return@let
                }
                appendLine("Target platforms include: " + publications.joinToString { publicationNameToPlatformName(it.name) })
                appendLine("Artifact ids are: ")
                for (target in publications) {
                    appendLine("${ext.artifactId}-${publicationNameToPlatformName(target.name)}")
                }

                appendLine()

                appendLine(
                    """
                        Gradle users can add multiplatform dependency in commonMain by `implementation("$groupId:$artifactId:$version")`.
                        Gradle users can also add $publishPlatformArtifactsInRootModule dependency by `implementation("$groupId:$artifactId:$version")`.
                    """.trimIndent()
                )
                appendLine()

                val jvmTargets =
                    kotlin.targets.filter { it.platformType == KotlinPlatformType.jvm || it.platformType == KotlinPlatformType.androidJvm }

                if (jvmTargets.isNotEmpty()) {
                    appendLine("Maven users can only add JVM dependencies, including: " + jvmTargets.joinToString { it.targetName })
                    appendLine()

                    for (target in jvmTargets) {
                        appendLine(
                            """
                                    Maven users can add ${target.name} dependency as follows:
                                    <dependency>
                                        <groupId>${ext.groupId}</groupId>
                                        <artifactId>${ext.artifactId}-${target.name}</artifactId>
                                        <version>${ext.version}</version>
                                    </dependency>
                                """.trimIndent()
                        )
                        appendLine()
                    }
                }

                val publishPlatformArtifactsInRootModule = publishPlatformArtifactsInRootModule
                if (publishPlatformArtifactsInRootModule != null) {
                    appendLine("You have configured to publish $publishPlatformArtifactsInRootModule into root module.")
                    appendLine(
                        """
                        ${if (jvmTargets.isNotEmpty()) "So, Maven users can also" else "So, Maven users can"} add $publishPlatformArtifactsInRootModule dependency as follows:
                        <dependency>
                            <groupId>${ext.groupId}</groupId>
                            <artifactId>${ext.artifactId}</artifactId>
                            <version>${ext.version}</version>
                        </dependency>
                    """.trimIndent()
                    )
                    appendLine()
                }
            }

            appendLine("Publication Preview End")
        }

    }
}