@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package me.him188.maven.central.publish.gradle

import me.him188.maven.central.publish.protocol.PublicationCredentials
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomDeveloper
import org.gradle.api.publish.maven.MavenPublication
import java.io.File
import java.io.IOException
import java.nio.file.Files

/**
 * The extension for Maven Central publication.
 *
 * For more information, read [https://github.com/Him188/maven-central-publish](https://github.com/Him188/maven-central-publish)
 *
 * @see pomConfigurators
 * @see publicationConfigurators
 */
open class MavenCentralPublishExtension(
    project: Project,
) {
    ///////////////////////////////////////////////////////////////////////////
    // Credentials, Plugin Configuration, Servers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Sonatype accounts and GPG keys.
     *
     * Will find from project property `PUBLICATION_CREDENTIALS`, `publication.credentials` or from [System.getProperty] and [System.getenv]
     */
    var credentials: PublicationCredentials? = kotlin.runCatching { Credentials.findCredentials(project) }.getOrNull()

    /**
     * Working dir. Please make it as short as it can, since the Signer has some embedded mechanism to detect the length of its filepath.¬
     *
     * The plugin will create a security file "KEEP_THIS_DIR_EMPTY.txt" into this directory.
     * All files in this dir will be deleted everytime the plugin is signing your artifact, provided that the security file exists.
     *
     * If [workingDir] is not empty and the security file does not exist, which means the directory is used for other purposes,
     * the plugin will throw an exception and will not delete anything.
     *
     * When system environment `publication.use.system.temp` is set to `true`, this will be set to `Files.createTempDirectory("publishing-tmp")`
     */
    var workingDir: File =
        if (System.getenv("publication.use.system.temp")?.toBoolean() == true
            || System.getenv("PUBLICATION_USE_SYSTEM_TEMP")?.toBoolean() == true
        ) {
            try {
                Files.createTempDirectory("publishing-tmp").toFile()
            } catch (e: IOException) {
                project.buildDir.resolve("publishing-tmp")
            }
        } else project.buildDir.resolve("publishing-tmp")

    /**
     * Set which server is to access to.
     *
     * If your account was created after February 2021, you may need to use [useCentralS01].
     *
     * See [the official documentation](https://central.sonatype.org/publish/publish-guide/#releasing-to-central) for more information.
     *
     * If [deploymentServerUrl] is set to `null`, no server will be configured, so you can customize the server.
     */
    var deploymentServerUrl: String? = "https://oss.sonatype.org/service/local/staging/deploy/maven2"

    /**
     * Sets [deploymentServerUrl] to `https://s01.oss.sonatype.org/service/local/staging/deploy/maven2`.
     *
     * If your account was created after February 2021, you need to use this new server.
     */
    fun useCentralS01() {
        deploymentServerUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2"
    }

    ///////////////////////////////////////////////////////////////////////////
    // Mandatory Project Coordinates
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Project main URL. Example: `https://github.com/him188/maven-central-publish`
     */
    var projectUrl: String = ""

    /**
     * Connection URL. Example: `scm:git:git://github.com/him188/maven-central-publish.git`
     */
    var connection: String = ""

    /**
     * Name of this project for the publication.
     */
    var projectName: String by lazyDefault { project.name }

    /**
     * Description of this project for the publication.
     */
    var projectDescription: String by lazyDefault {
        project.description ?: project.rootProject.description ?: projectName
    }

    /**
     * Group ID for the publication.
     */
    var groupId: String by lazyDefault { project.group.toString() }

    /**
     * Artifact ID for the publication.
     */
    var artifactId: String by lazyDefault { project.name }

    /**
     * Version for the publication.
     */
    var version: String by lazyDefault { project.version.toString() }

    ///////////////////////////////////////////////////////////////////////////
    // Optional configurators
    ///////////////////////////////////////////////////////////////////////////

    /**
     * [MavenPom] (`pom.xml`) configurators.
     *
     * Maven Central will validate this pom, and requires the following information:
     * - project id and group ---- [artifactId] and [groupId]
     * - project name ---- [projectName]
     * - project description ---- [projectDescription]
     * - project url ---- [projectUrl]
     * - project SCM ---- [connection]
     * - project licenses ---- [license]
     * - project developers ---- [developer]
     *
     * Therefore, please ensure that you set
     */
    val pomConfigurators: MutableList<Action<MavenPom>> = mutableListOf()

    /**
     * Adds a configurator to [pomConfigurators]
     */
    fun pom(action: Action<MavenPom>) {
        pomConfigurators.add(action)
    }

    /**
     * [MavenPublication] configurators.
     *
     * This plugin registers a *publication* named `MavenCentral` for Java and Kotlin single target projects.
     * For Kotlin MPP, this will be done by the Kotlin plugin.
     *
     * Each configurator in [publicationConfigurators] will be executed to the *publication* after the invocation of the above configuration,
     * although maybe before shadowed artifact is added.
     * You can add more artifacts via [MavenPublication.from] or [MavenPublication.artifact],
     * but removing artifacts is not supported as all the ones preconfigured for you are required by the Maven Central validator.
     *
     * ## Files in each publication
     *
     * Usually you don't need to care about this.
     *
     * For Kotlin MPP, each target is accompanied by a *publication*, configured with platform-specific source roots.
     *
     * Each *publication* will contain at least these artifacts:
     * - project-name.pom  // contains information configured by [pomConfigurators]
     * - project-name.jar  // compiled, output jar
     * - project-name-sources.jar  // source code, usually from `src/main/java` and `src/main/kotlin`.
     *                                For Kotlin MPP, it is `src/xxxMain/kotlin` plus all its dependant source sets.
     * - project-name-javadoc.jar  // javadoc for this
     *
     * For native targets targeting macOS or Windows, there may be additionally 'project-name-metadata.jar' created by the Kotlin plugin.
     *
     * ### Using shadow plugin
     *
     * The plugin can integrate with Shadow plugin (`com.github.johnrengelman.shadow`), but with care.
     *
     * As [described](https://imperceptiblethoughts.com/shadow/publishing/#publishing-shadow-jars), Shadow plugin automatically
     * adds an artifact "$name-$version-all.jar" to all `MavenPublication`s. This file will be included in the publication.
     *
     * This would work normally if both `mavenCentralPublish.artifactId == project.name`
     * and `mavenCentralPublish.version == project.version`. However, if not, you should rename the '-all' artifact as follows:
     *
     * ```
     * tasks.withType(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
     *     archiveFileName.set("${mavenCentralPublish.artifactId}-${mavenCentralPublish.version}-all.jar")
     * }
     * ```

     * Additionally, each file is signed with your [PublicationCredentials.gpgPrivateKey] in [credentials].
     */
    val publicationConfigurators: MutableList<Action<MavenPublication>> = mutableListOf()

    /**
     * Adds a configurator to [publicationConfigurators]
     */
    fun publication(action: Action<MavenPublication>) {
        publicationConfigurators.add(action)
    }

    /**
     * If `false`, no [components][Project.getComponents] will be added to publications, so that you can add by your own in [publicationConfigurators]
     *
     * If `true`, the component `project.components["java"]` will be added.
     * Such component usually contains a binary jar that is compiled from your source code at `src/main/`.
     *
     * @see publicationConfigurators
     */
    var addProjectComponents: Boolean = true

    /**
     * The target name to be published also in root module. Example value: `"jvm"`.
     *
     * This enables Kotlin Multiplatform Projects to be resolved by consumers who have no access to Gradle Metadata (e.g., Maven users).
     */
    var publishPlatformArtifactsInRootModule: String? = null

    ///////////////////////////////////////////////////////////////////////////
    // Quick configurators
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Configure publication for a GitHub project, setting all the information required.
     *
     * [user] and [repositoryName] are A and B in `https://github.com/A/B` respectively.
     *
     * After this, you need to add at least one [developer] and at least one [license].
     *
     * @param user the name of the organization or the user that this repository belongs to
     * @param repositoryName the name of the repository
     *
     * @see developer
     * @see license
     */
    fun githubProject(
        user: String,
        repositoryName: String,
    ) {
        val projectUrl = "https://github.com/$user/$repositoryName"
        this.projectUrl = projectUrl
        this.connection = "scm:git:git://github.com/$user/$repositoryName"
    }

    /**
     * Configure publication for a GitHub project with one developer, setting all the information required.
     *
     * [user] and [repositoryName] are A and B in `https://github.com/A/B` respectively.
     *
     * After this, you need to add at least one [license].
     *
     * More developers can be added through [developer].
     *
     * @param user the name of the organization or the user that this repository belongs to
     * @param repositoryName the name of the repository
     *
     * @see developer
     * @see license
     */
    @JvmOverloads
    fun singleDevGithubProject(
        user: String,
        repositoryName: String,
        author: String = user,
    ) {
        githubProject(user, repositoryName)
        developer(author)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Developers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Add a developer. [id] is required and must be unique.
     *
     * @see developer
     */
    @JvmOverloads
    fun developer(
        id: String,
        name: String? = id,
        email: String? = null,
        url: String? = null,
        roles: String? = null,
        organization: String? = null,
        organizationUrl: String? = null,
    ) {
        developer {
            it.id.set(id)
            it.name.set(name)
            if (!email.isNullOrBlank()) it.email.set(email)
            if (!url.isNullOrBlank()) it.url.set(url)
            if (!roles.isNullOrBlank()) it.roles.set(roles.split(','))
            if (!organization.isNullOrBlank()) it.organization.set(organization)
            if (!organizationUrl.isNullOrBlank()) it.organizationUrl.set(organizationUrl)
        }
    }

    /**
     * Add a developer. [MavenPomDeveloper.getId] and [MavenPomDeveloper.getName] are required.
     * @see developer
     */
    fun developer(action: Action<MavenPomDeveloper>) {
        pomConfigurators.add {
            it.developers { spec ->
                spec.developer { dev ->
                    action.execute(dev)
                }
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Licenses
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds a license.
     *
     * If the project is hosted on GitHub, [licenseFromGitHubProject] can be used for convenience.
     *
     * [licenseAGplV3], [licenseMit], etc. methods are also available.
     */
    fun license(
        name: String,
        url: String,
    ) {
        pomConfigurators.add { pom ->
            pom.licenses { spec ->
                spec.license { l ->
                    l.name.set(name)
                    l.url.set(url)
                }
            }
        }
    }

    /**
     * Configures using the license from your GitHub project.
     *
     * Please ensure your [projectUrl] refers to a valid GitHub project. This is also done by using [githubProject] or [singleDevGithubProject], or set [projectUrl] to your project url before this function.
     * @see license
     */
    @JvmOverloads
    fun licenseFromGitHubProject(
        licenseName: String,
        branchName: String = "main",
    ) {
        val urlWithoutSuffixSlash = projectUrl.removeSuffix("/")
        val userAndProj = urlWithoutSuffixSlash.substringAfter("github.com/").split('/')
        require(userAndProj.size == 2) {
            "projectUrl '$projectUrl' does not refer to a GitHub project."
        }
        license(licenseName, "$urlWithoutSuffixSlash/blob/$branchName/LICENSE")
    }

    /**
     * Configures using GNU General Public License, version 3
     * @see license
     */
    fun licenseGplV3() {
        license("GNU GPLv3", "https://www.gnu.org/licenses/gpl-3.0.en.html")
    }

    /**
     * Configures using GNU General Public License, version 2
     * @see license
     */
    fun licenseGplV2() {
        license("GNU GPLv2", "https://www.gnu.org/licenses/old-licenses/gpl-2.0.html")
    }

    /**
     * Configures using GNU Affero General Public License, version 3
     * @see license
     */
    fun licenseAGplV3() {
        license("GNU AGPLv3", "https://www.gnu.org/licenses/agpl-3.0.en.html")
    }

    /**
     * Configures using MIT license
     * @see license
     */
    fun licenseMit() {
        license("MIT", "https://opensource.org/licenses/MIT")
    }

    /**
     * Configures using Apache License, version 2.0
     * @see license
     */
    fun licenseApacheV2() {
        license("Apache-2.0", "https://www.apache.org/licenses/LICENSE-2.0")
    }

}