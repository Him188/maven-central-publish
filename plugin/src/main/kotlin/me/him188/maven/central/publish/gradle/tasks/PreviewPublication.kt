package me.him188.maven.central.publish.gradle.tasks

import me.him188.maven.central.publish.gradle.MavenCentralPublishExtension
import me.him188.maven.central.publish.gradle.mcExt
import org.gradle.api.DefaultTask
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

open class PreviewPublication : DefaultTask() {
    companion object {
        const val TASK_NAME = "previewPublication"

        val knownExtensionAndClassifiers: List<Pair<String, String?>> = listOf(
            "jar" to "javadoc",
            "jar" to "samplessources",
            "jar" to "sources",
            "jar" to null,
            "klib" to null,
        )
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

            appendProjectComponentsInfo(ext)
            appendExtraFilesInfo(ext)

            appendLine("Publication Preview End")
        }
    }

    private fun StringBuilder.appendExtraFilesInfo(ext: MavenCentralPublishExtension): Unit = ext.run {
        val unknownArtifacts = getUnknownArtifacts(if (addProjectComponents) knownExtensionAndClassifiers else listOf())
        if (!unknownArtifacts.all { it.second.isEmpty() }) {
            appendLine()
            if (addProjectComponents) {
                appendLine("There are some extra files that are going to be published:")
            } else {
                appendLine("The files to be published:")
            }
            appendLine()
            for ((publication, files) in unknownArtifacts) {
                appendLine("[${publicationNameToPlatformName(publication.name)}]")
                for (artifact in files) {
                    appendLine(artifact.render())
                }
                appendLine()
            }
        }
    }

    private fun publicationNameToPlatformName(name: String) = when (name) {
        "kotlinMultiplatform" -> "common"
        "metadata" -> "common"
        "mavenCentral" -> "jvm"
        else -> name
    }

    private fun StringBuilder.appendProjectComponentsInfo(ext: MavenCentralPublishExtension): Unit = ext.run {
        if (!addProjectComponents) {
            appendLine("`addProjectComponents` was set to `false`, so no project files will be added by default.")
            return
        }

        project.extensions.findByType(KotlinJvmProjectExtension::class.java)?.let {
            appendLine("Your project targets JVM platform only.")

            appendLine(
                """
                        Gradle users can add dependency by `implementation("$groupId:$artifactId:$version")`.
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

    }

    private fun MavenArtifact.render(): String = "${file.name}  (extension=$extension, classifier=$classifier)"

    private fun getUnknownArtifacts(knownExtensionAndClassifiers: List<Pair<String, String?>>): List<Pair<MavenPublication, List<MavenArtifact>>> {
        val extension = project.extensions.findByType(PublishingExtension::class.java) ?: return listOf()
        return extension.publications
            .filterIsInstance<MavenPublication>()
            .map { publication ->
                publication to publication.artifacts
                    .filter { it.extension to it.classifier !in knownExtensionAndClassifiers }
            }
    }
}