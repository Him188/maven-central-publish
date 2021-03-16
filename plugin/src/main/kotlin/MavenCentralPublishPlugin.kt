@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.mamoe.him188.maven.central.publish.gradle

import io.codearte.gradle.nexus.NexusStagingExtension
import io.codearte.gradle.nexus.NexusStagingPlugin
import io.github.karlatemp.publicationsign.PublicationSignPlugin
import org.gradle.api.*
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName


public class MavenCentralPublishPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.rootProject.plugins.apply(NexusStagingPlugin::class.java)
        target.plugins.apply("maven-publish")
        target.plugins.apply(PublicationSignPlugin::class.java)

        target.run {
            afterEvaluate {
                val ext = target.mcExt
                val credentials = ext.credentials ?: kotlin.run {
                    logger.warn("[Publishing] No credentials were set.")
                    return@afterEvaluate
                }

                extensions.configure(NexusStagingExtension::class.java) { nexus ->
                    with(nexus) {
                        packageGroup = (target.group ?: target.rootProject.group).toString()
                        username = credentials.sonatypeUsername
                        password = credentials.sonatypePassword
                    }
                }

                extensions.configure(io.github.karlatemp.publicationsign.PublicationSignExtension::class.java) { sign ->
                    sign.setupWorkflow { workflow ->
                        workflow.workingDir = buildDir.resolve("gpg").apply { mkdirs() }
                        workflow.fastSetup(
                            buildDir.relativeTo(projectDir).resolve("key.pub").path,
                            buildDir.relativeTo(projectDir).resolve("key.pri").path,
                        )
                    }
                }

                buildDir.resolve("keys")
                    .apply { mkdirs() }
                    .run {
                        resolve("key.pub").writeText(credentials.gpgPublicKey)
                        resolve("key.pri").writeText(credentials.gpgPrivateKey)
                    }

                val sourcesJar = tasks.getOrRegister("sourcesJar", Jar::class.java) {
                    @Suppress("DEPRECATION")
                    classifier = "sources"
                    from(project.extensions.getByName<org.gradle.api.tasks.SourceSetContainer>("sourceSets")["main"].allSource)
                }

                val javadocJar = tasks.getOrRegister("javadocJar", Jar::class.java) {
                    @Suppress("DEPRECATION")
                    classifier = "javadoc"
                }

                extensions.findByType(PublishingExtension::class.java)?.apply {
                    repositories.maven { repo ->
                        repo.setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                        repo.credentials { c ->
                            c.username = credentials.sonatypeUsername
                            c.password = credentials.sonatypePassword
                        }
                    }

                    if (project.plugins.findPlugin("org.jetbrains.kotlin.multiplatform") == null) {
                        publications.register("MavenCentral", MavenPublication::class.java) { publication ->
                            publication.run {
                                if (ext.addProjectComponents) {
                                    from(components["java"])
                                }
                                artifact(sourcesJar)
                                artifact(javadocJar)
                                this.groupId = groupId
                                this.artifactId = artifactId
                                this.version = project.version.toString()
                                setupPom(publication, project, ext)
                                ext.publicationConfigurators.forEach {
                                    it.execute(this)
                                }
                            }

                        }
                    } else {
                        publications.filterIsInstance<MavenPublication>().forEach { publication ->
                            publication.artifact(javadocJar)
                            setupPom(publication, project, ext)

                            when (val type = publication.name) {
                                "kotlinMultiplatform" -> {
                                    publication.artifactId = project.name

                                    publishPlatformArtifactsInRootModule(publications.getByName("jvm") as MavenPublication)
                                }
                                "common" -> {
                                }
                                else -> {
                                    // "jvm", "native", "js"
                                    publication.artifactId = "${project.name}-$type"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Project.publishPlatformArtifactsInRootModule(platformPublication: MavenPublication) {
        lateinit var platformPomBuilder: XmlProvider
        platformPublication.pom.withXml { platformPomBuilder = it }

        extensions.findByType(PublishingExtension::class.java)?.publications?.getByName("kotlinMultiplatform")?.let { it as MavenPublication }?.run {
            this.artifacts.removeIf {
                it.classifier == null && it.extension == "jar"
            }

            platformPublication.artifacts.forEach {
                artifact(it)
            }

            // replace pom
            pom.withXml { xmlProvider ->
                val pomStringBuilder = xmlProvider.asString()
                pomStringBuilder.setLength(0)
                platformPomBuilder.toString().lines().forEach { line ->
                    if (!line.contains("<!--")) { // Remove the Gradle module metadata marker as it will be added anew
                        pomStringBuilder.append(line.replace(platformPublication.artifactId, artifactId))
                        pomStringBuilder.append("\n")
                    }
                }
            }
        }

        tasks.matching { it.name == "generatePomFileForKotlinMultiplatformPublication" }.configureEach { task ->
            task.dependsOn(tasks["generatePomFileFor${platformPublication.name.capitalize()}Publication"])
        }
    }

    private fun setupPom(
        mavenPublication: MavenPublication,
        project: Project,
        ext: MavenCentralPublishExtension
    ) {
        mavenPublication.pom { pom ->
            pom.withXml {
                it.asNode()
            }
            pom.name.set(project.project.name)
            pom.description.set(project.project.description)
            pom.url.set(ext.projectUrl)
            pom.scm { scm ->
                scm.url.set(ext.projectUrl)
                scm.connection.set(ext.connection)
            }
            ext.pomConfigurators.forEach {
                it.execute(pom)
            }
        }
    }
}

private fun <T : Task> TaskContainer.getOrRegister(name: String, type: Class<T>, configurationAction: T.() -> Unit): T {
    return findByName(name)?.let { type.cast(it) } ?: register(name, type, configurationAction).get()
}

private val Project.mcExt: MavenCentralPublishExtension get() = extensions.getByType(MavenCentralPublishExtension::class.java)
