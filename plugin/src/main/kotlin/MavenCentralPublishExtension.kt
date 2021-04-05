@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package net.mamoe.him188.maven.central.publish.gradle

import net.mamoe.him188.maven.central.publish.protocol.PublicationCredentials
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomDeveloper
import org.gradle.api.publish.maven.MavenPublication

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
    /**
     * Project main URL. Example: `https://github.com/him188/maven-central-publish`
     */
    var projectUrl: String = ""

    /**
     * Connection URL. Example: `scm:git:git://github.com/him188/maven-central-publish.git`
     */
    var connection: String = ""

    /**
     * [MavenPom] (`pom.xml`) configurators.
     *
     * Maven Central will validate this pom, and requires the following information:
     * - project id and group ---- auto-get from [Project.getName] and [Project.getGroup]
     * - project name ---- auto-get from [Project.getName]
     * - project description ---- auto-get from [Project.getDescription]
     * - project url ---- use [projectUrl]
     * - project licenses ---- use [license]
     * - project developers ---- use [developer]
     * - project SCM ---- use [connection]
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
     * This plugin registers a *publication* named `MavenCentral` for Java and Kotlin single target projects and configures.
     *
     * For Kotlin MPP, each target is accompanied with a *publication*, configured with platform-specific source roots.
     *
     * The *publication* will contain these artifacts:
     * - project-name.pom  // contains information configured by [pomConfigurators]
     * - project-name.jar  // compiled, output jar
     * - project-name-sources.jar  // source code, usually from `src/main/java` and `src/main/kotlin`.
     *                                For Kotlin MPP, it is `src/xxxMain/kotlin` plus all its dependant source sets.
     * - project-name-javadoc.jar  // javadoc for this
     *
     * If you applied shadow-plugin (`com.github.johnrengelman.shadow`), there will be another artifact named `project-name-all.jar`.
     *
     * Additionally, each file is signed with your [PublicationCredentials.pgpPrivateKey] in [credentials].
     *
     *
     * Each configurator in [publicationConfigurators] will be executed to the *publication* after the invocation of the above configuration,
     * although maybe before shadowed artifact is added.
     * You can add more artifacts via [MavenPublication.from] or [MavenPublication.artifact],
     * but removing artifacts is not supported as all the ones preconfigured for you are required by the Maven Central validator.
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
     * `true` to publish jvm platform artifacts also in root module.
     *
     * This enables Kotlin Multiplatform Projects to be resolved by consumers who has no access to Gradle Metadata (e.g. Maven users).
     */
    var publishPlatformArtifactsInRootModule: Boolean = false

    ///////////////////////////////////////////////////////////////////////////
    // Quick configurator
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Configure publication for a GitHub project, setting all the information required.
     *
     * [user] and [repositoryName] are A and B in `https://github.com/A/B` respectively.
     *
     * Please configure at least one [developer] after invoking this function.
     *
     * @param user the name of the organization or the user that this repository belongs to
     * @param repositoryName the name of the repository
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
     * Configure publication for a GitHub project who has , setting all the information required.
     *
     * [user] and [repositoryName] are A and B in `https://github.com/A/B` respectively.
     *
     * This function configures all the required information so no further configurations needed.
     *
     * @param user the name of the organization or the user that this repository belongs to
     * @param repositoryName the name of the repository
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
     * Add a developer.
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
     * Add a developer
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
     * Adds a license
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
     * Please ensure you used [githubProject] or [singleDevGithubProject], or set [projectUrl] to your project url before this function.
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
     */
    fun licenseGplV3() {
        license("GNU GPLv3", "https://www.gnu.org/licenses/gpl-3.0.en.html")
    }

    /**
     * Configures using GNU General Public License, version 2
     */
    fun licenseGplV2() {
        license("GNU GPLv2", "https://www.gnu.org/licenses/old-licenses/gpl-2.0.html")
    }

    /**
     * Configures using GNU Affero General Public License, version 3
     */
    fun licenseAGplV3() {
        license("GNU AGPLv3", "https://www.gnu.org/licenses/agpl-3.0.en.html")
    }

    /**
     * Configures using MIT license
     */
    fun licenseMit() {
        license("MIT", "https://opensource.org/licenses/MIT")
    }

    /**
     * Configures using Apache License, version 2.0
     */
    fun licenseApacheV2() {
        license("Apache-2.0", "https://www.apache.org/licenses/LICENSE-2.0")
    }


    ///////////////////////////////////////////////////////////////////////////
    // Credentials
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Sonatype accounts and GPG keys.
     *
     * Will find from project property `PUBLICATION_CREDENTIALS`, `publication.credentials` or from [System.getProperty] and [System.getenv]
     */
    var credentials: PublicationCredentials? = kotlin.runCatching { Credentials.findCredentials(project) }.getOrNull()
}