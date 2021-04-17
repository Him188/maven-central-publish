@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.mamoe.him188.maven.central.publish.gradle

import io.codearte.gradle.nexus.NexusStagingExtension
import io.codearte.gradle.nexus.NexusStagingPlugin
import io.github.karlatemp.publicationsign.PublicationSignPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.XmlProvider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType


class MavenCentralPublishPlugin : Plugin<Project> {
    companion object {
        const val CHECK_PUBLICATION_CREDENTIALS = "checkPublicationCredentials"
        const val CHECK_MAVEN_CENTRAL_PUBLICATION = "checkMavenCentralPublication"
    }

    override fun apply(target: Project) {
        target.rootProject.plugins.apply(NexusStagingPlugin::class.java)
        target.plugins.apply("maven-publish")
        target.plugins.apply(PublicationSignPlugin::class.java)

        target.extensions.create(MavenCentralPublishExtension::class.java, "mavenCentralPublish", MavenCentralPublishExtension::class.java, target)

        val checkPublicationCredentials = target.tasks.register(CHECK_PUBLICATION_CREDENTIALS) { task ->
            task.group = "publishing"
            task.description = "Check publication credentials."
            task.doLast {
                val ext = task.project.mcExt
                val credentials = ext.credentials ?: error("No Publication credentials were set.")

                Credentials.check(credentials)
            }
        }

        target.tasks.register(CHECK_MAVEN_CENTRAL_PUBLICATION) { task ->
            task.group = "publishing"
            task.description = "Check whether information required to maven central publication is provided.."
            task.dependsOn(checkPublicationCredentials)
            task.doLast {
                val ext = task.project.mcExt
                check(ext.projectUrl.isNotBlank()) { "'projectUrl' is not set. This means `mavenCentralPublish` is not configured." }
                check(ext.connection.isNotBlank()) { "'connection' is not set. This means `mavenCentralPublish` is not configured." }
            }
        }

        target.run {
            afterEvaluate {
                val ext = target.mcExt
                val credentials = ext.credentials ?: kotlin.run {
                    logger.warn("[MavenCentralPublish] No credentials were set.")
                    return@afterEvaluate
                }

                rootProject.extensions.configure(NexusStagingExtension::class.java) { nexus ->
                    with(nexus) {
                        if (packageGroup == null) packageGroup = ext.packageGroup
                        if (username == null) username = credentials.sonatypeUsername
                        if (password == null) password = credentials.sonatypePassword
                    }
                }

                project.logger.info("[MavenCentralPublish] Writing public key len=${credentials.pgpPublicKey.length} to \$buildDir/keys/key.pub.")
                project.logger.info("[MavenCentralPublish] Writing private key len=${credentials.pgpPrivateKey.length} to \$buildDir/keys/key.pri.")

                project.logger.info("[MavenCentralPublish] workingDir=${ext.workingDir.absolutePath}")

                val keysDir = ext.workingDir

                keysDir.run {
                    deleteRecursively() // clear caches
                    mkdirs()
                    resolve("key.pub").writeText(credentials.pgpPublicKey)
                    resolve("key.pri").writeText(credentials.pgpPrivateKey)
                }

                extensions.configure(io.github.karlatemp.publicationsign.PublicationSignExtension::class.java) { sign ->
                    sign.setupWorkflow { workflow ->
                        workflow.workingDir = keysDir
                        workflow.fastSetup(
                            keysDir.resolve("key.pub").absolutePath,
                            keysDir.resolve("key.pri").absolutePath,
                        )
                    }
                }

                if (ext.projectUrl.isEmpty() || ext.connection.isEmpty()) {
                    logger.warn("[MavenCentralPublish] projectUrl is not set. No publication is being configured. Please invoke `mavenCentralPublish()` according to https://github.com/Him188/maven-central-publish.")
                    return@afterEvaluate
                }

                registerJarTasks(project)
                registerPublication("mavenCentral", project, ext)
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

    fun registerJarTasks(
        project: Project,
    ) = project.run {
        tasks.getOrRegister("sourcesJar", Jar::class.java) {
            @Suppress("DEPRECATION")
            classifier = "sources"
            val sourceSets = project.extensions.getByName<SourceSetContainer>("sourceSets").matching { it.name.endsWith("main", ignoreCase = true) }
            for (sourceSet in sourceSets) {
                from(sourceSet.allSource)
            }
        }

        tasks.getOrRegister("javadocJar", Jar::class.java) {
            @Suppress("DEPRECATION")
            classifier = "javadoc"
        }
    }

    fun registerPublication(
        name: String,
        project: Project,
        ext: MavenCentralPublishExtension,
    ): Unit = project.run {

        fun getJarTask(classifier: String) =
            tasks.singleOrNull { it is Jar && it.archiveClassifier.get() == classifier } ?: error("Could not find $classifier Jar task.")


        val credentials = ext.credentials ?: return

        extensions.findByType(PublishingExtension::class.java)?.apply {
            repositories.maven { repo ->
                repo.setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                repo.credentials { c ->
                    c.username = credentials.sonatypeUsername
                    c.password = credentials.sonatypePassword
                }
            }

            if (project.plugins.findPlugin("org.jetbrains.kotlin.multiplatform") == null) {
                publications.register(name, MavenPublication::class.java) { publication ->
                    publication.run {
                        if (ext.addProjectComponents) {
                            from(components["java"])
                        }

                        artifact(getJarTask("sources"))
                        artifact(getJarTask("javadoc"))
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
                    // kotlin configures `sources` for us.
                    publication.artifact(getJarTask("javadoc"))

                    setupPom(publication, project, ext)

                    when (val type = publication.name) {
                        "kotlinMultiplatform" -> {
                            publication.artifactId = project.name
                        }
                        "common" -> {
                        }
                        else -> {
                            // "jvm", "native", "js"
                            publication.artifactId = "${project.name}-$type"
                        }
                    }
                }
                if (ext.publishPlatformArtifactsInRootModule) {
                    val jvmTarget =
                        project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.targets?.find { it.publishable && it.platformType == KotlinPlatformType.jvm }
                    if (jvmTarget != null) {
                        val publication = publications.filterIsInstance<MavenPublication>().find { it.name == jvmTarget.name }
                        if (publication != null) {
                            publishPlatformArtifactsInRootModule(publication)
                        }
                    }
                }
            }
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
            pom.description.set(project.project.description ?: project.rootProject.description ?: kotlin.run {
                project.logger.warn("[MavenCentralPublish] Project description not found for project '${project.path}'. Please set by `project.description`.")
                "No description provided."
            })
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
