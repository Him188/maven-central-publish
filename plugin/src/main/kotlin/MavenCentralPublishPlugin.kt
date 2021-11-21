@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.mamoe.him188.maven.central.publish.gradle

import groovy.util.Node
import groovy.util.NodeList
import io.github.karlatemp.publicationsign.PublicationSignPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.XmlProvider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.jvm.tasks.Jar


class MavenCentralPublishPlugin : Plugin<Project> {
    companion object {
        const val CHECK_PUBLICATION_CREDENTIALS = "checkPublicationCredentials"
        const val CHECK_MAVEN_CENTRAL_PUBLICATION = "checkMavenCentralPublication"
    }

    override fun apply(target: Project) {
        target.plugins.apply("maven-publish")
        target.plugins.apply(PublicationSignPlugin::class.java)

        target.extensions.create(
            MavenCentralPublishExtension::class.java,
            "mavenCentralPublish",
            MavenCentralPublishExtension::class.java,
            target
        )

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

                project.logger.info("[MavenCentralPublish] credentials: length=${credentials.toString().length}")

                project.logger.info("[MavenCentralPublish] workingDir=${ext.workingDir.absolutePath}")

                project.logger.info("[MavenCentralPublish] Writing public key len=${credentials.pgpPublicKey.length} to \$workingDir/keys/key.pub.")
                project.logger.info("[MavenCentralPublish] Writing private key len=${credentials.pgpPrivateKey.length} to \$workingDir/keys/key.pri.")

                val keysDir = ext.workingDir

                keysDir.run {
                    deleteRecursively() // clear caches
                    mkdirs()
                    resolve("key.pub").apply { createNewFile() }.writeText(credentials.pgpPublicKey)
                    resolve("key.pri").apply { createNewFile() }.writeText(credentials.pgpPrivateKey)
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
        lateinit var platformXml: XmlProvider
        platformPublication.pom.withXml { platformXml = it }

        extensions.findByType(PublishingExtension::class.java)
            ?.publications?.getByName("kotlinMultiplatform")
            ?.let { it as MavenPublication }?.run {

                // replace pom
                pom.withXml { xmlProvider ->
                    val root = xmlProvider.asNode()
                    // Remove the original content and add the content from the platform POM:
                    root.children().toList().forEach { root.remove(it as Node) }
                    platformXml.asNode().children().forEach { root.append(it as Node) }

                    // Adjust the self artifact ID, as it should match the root module's coordinates:
                    ((root.get("artifactId") as NodeList).get(0) as Node).setValue(artifactId)

                    // Set packaging to POM to indicate that there's no artifact:
                    root.appendNode("packaging", "pom")

                    // Remove the original platform dependencies and add a single dependency on the platform module:
                    val dependencies = (root.get("dependencies") as NodeList).get(0) as Node
                    dependencies.children().toList().forEach { dependencies.remove(it as Node) }
                    val singleDependency = dependencies.appendNode("dependency")
                    singleDependency.appendNode("groupId", platformPublication.groupId)
                    singleDependency.appendNode("artifactId", platformPublication.artifactId)
                    singleDependency.appendNode("version", platformPublication.version)
                    singleDependency.appendNode("scope", "compile")
                }
            }

        tasks.matching { it.name == "generatePomFileForKotlinMultiplatformPublication" }.configureEach { task ->
            task.dependsOn("generatePomFileFor${platformPublication.name.capitalize()}Publication")
        }
    }

    fun registerJarTasks(
        project: Project,
    ) = project.run {
        tasks.getOrRegister("sourcesJar", Jar::class.java) {
            @Suppress("DEPRECATION")
            classifier = "sources"
            val sourceSets = (project.extensions.getByName("sourceSets") as SourceSetContainer).matching {
                it.name.endsWith("main", ignoreCase = true)
            }
            for (sourceSet in sourceSets) {
                from(sourceSet.allSource)
            }
        }

        tasks.getOrRegister("javadocJar", Jar::class.java) {
            @Suppress("DEPRECATION")
            classifier = "javadoc"
        }

        tasks.getOrRegister("samplessourcesJar", Jar::class.java) {
            @Suppress("DEPRECATION")
            classifier = "samplessources"
            val sourceSets = (project.extensions.getByName("sourceSets") as SourceSetContainer).matching {
                it.name.endsWith("test", ignoreCase = true)
            }
            for (sourceSet in sourceSets) {
                from(sourceSet.allSource)
            }
        }
    }

    fun registerPublication(
        name: String,
        project: Project,
        ext: MavenCentralPublishExtension,
    ): Unit = project.run {

        fun getJarTask(classifier: String) =
            tasks.singleOrNull { it is Jar && it.name == "${classifier}Jar" }
                ?: tasks.firstOrNull { it is Jar && it.archiveClassifier.get() == classifier }
                ?: error("Could not find $classifier Jar task.")


        val credentials = ext.credentials ?: return
        extensions.findByType(PublishingExtension::class.java)?.apply {
            val deploymentServerUrl = ext.deploymentServerUrl
            if (deploymentServerUrl != null) {
                repositories.maven { repo ->
                    repo.setUrl(deploymentServerUrl)
                    repo.credentials { c ->
                        c.username = credentials.sonatypeUsername
                        c.password = credentials.sonatypePassword
                    }
                }
            } else {
                logger.warn("[MavenCentralPublish] `deploymentServerUrl` was set to `null`, so no server is being automatically set. ")
            }

            if (project.plugins.findPlugin("org.jetbrains.kotlin.multiplatform") == null) {
                publications.register(name, MavenPublication::class.java) { publication ->
                    publication.run {
                        if (ext.addProjectComponents) {
                            from(components.getByName("java"))
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
                    if (publication.name != "kotlinMultiplatform") publication.artifact(getJarTask("javadoc"))

                    setupPom(publication, project, ext)

                    when (val type = publication.name) {
                        "kotlinMultiplatform" -> {
                            publication.artifactId = project.name
                        }
                        "metadata", "jvm", "native", "js" -> {
                            publication.artifactId = "${project.name}-$type"
                            if (publication.name.contains("js", ignoreCase = true)) {
                                publication.artifact(getJarTask("samplessources"))
                            }
                        }
                        // mingwx64 and others
                    }
                    ext.publicationConfigurators.forEach {
                        it.execute(publication)
                    }
                }
                if (ext.publishPlatformArtifactsInRootModule != null) {
                    val targetName = ext.publishPlatformArtifactsInRootModule
                    val publication =
                        publications.filterIsInstance<MavenPublication>()
                            .find { it.artifactId == "${project.name}-$targetName" }
                            ?: error(
                                "Could not find publication with artifactId '${project.name}-$targetName' for root module. " +
                                        "This means the target name '$targetName' you specifdied to `publishPlatformArtifactsInRootModule` is invalid."
                            )
                    publishPlatformArtifactsInRootModule(publication)
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
