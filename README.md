# maven-central-publish

Configure publication to Maven Central repository for Gradle projects with minimal effort.

## Why this plugin?

### Pack credentials once, use anywhere

You can pack your Sonatype and GPG keys once, then use it around any projects directly. You can add it into your global
gradle.properties or store in GitHub secretes for Actions.

### Automatic configuration for usual Java and Kotlin projects

This simplest configuration applies to most projects:

```kotlin
mavenCentralPublish {
    singleDevGithubProject("Him188", "maven-central-publish")
    licenseApacheV2()
}
```

By running 'publish', everything will be done by the plugin.

### Predictable artifacts configuration

The plugin provides a task `previewPublication` allowing you to know everything about the project being published. An
example

### Full support for Kotlin Multiplatform (KMM)

The plugin supports not only KMM with Native targets, but hierarchical multiplatform projects.

For example a nativeBaseMain with androidArm64Main and iosArm64Main depending on it.

Hierarchical JVM projects are also supported: a shared jvmBaseMain with jvmDesktopMain and androidMain.

### Platform artifacts in KMM root module

When your multiplatform project targets JVM, you can set `publishPlatformArtifactsInRootModule="jvm"` to allow the
plugin to add a dependency to the jvm module on the root module.

This enables users who can not access Gradle Metadata to add dependency using the root module without '-jvm' suffix.

## Using the plugin

```kotlin
plugins {
    id("me.him188.maven-central-publish") version "1.0.0-dev-1"
}
```

## Step-by-step tutorials

You can read these tutorials if you are new to publishing. Also, you can continue reading this article for quick
reference.

- [How to use this plugin in a local project](UseInLocalProject.md)
- [How to use this plugin in CI environment](UseInCI.md)

## Configuring the plugin

The plugin adds a `mavenCentralPublish` configuration. This chapter shows some handy examples.

See [MavenCentralPublishExtension](plugin/src/main/kotlin/me/him188/maven/central/publish/gradle/MavenCentralPublishExtension.kt)
for full details for each property.

### Basic configuration

As required by Maven Central, you would need

- project id and group ---- `[Project.getName]` and `[Project.getGroup]`
- project name ---- `[Project.getName]`
- project description ---- `[Project.getDescription]`
- project url ---- `[projectUrl]`
- project SCM ---- `[connection]`
- project licenses ---- `[license]`
- project developers ---- `[developer]`

It would be easier to keep your `project.name`, `project.group`, `project.version` same as which you want to use for
your published artifacts.

A recommended, minimal, manual configuration is:

```kotlin
mavenCentralPublish {
    // If different from that from project, specify manually:
    artifactId = "kotlin-jvm-blocking-bridge-runtime"
    groupId = "me.him188"
    projectName = "Kotlin JVM Blocking Bridge Runtime"
    // description from project.description by default

    url = "https://github.com/him188/kotlin-jvm-blocking-bridge"
    connection = "scm:git:git://github.com/him188/kotlin-jvm-blocking-bridge.git"
    license("Apache-2.0", "https://www.apache.org/licenses/LICENSE-2.0")

    developer("Him188")
}
```

However, configuration for GitHub projects can be simplified:

```kotlin
mavenCentralPublish {
    // If different from that from project, specify manually:
    artifactId = "kotlin-jvm-blocking-bridge-runtime"
    groupId = "me.him188"
    projectName = "Kotlin JVM Blocking Bridge Runtime"

    githubProject("him188", "kotlin-jvm-blocking-bridge")
    developer("him188")
    licenseApacheV2()

    // and can be more simplified as 
    singleDevGithubProject("him188", "kotlin-jvm-blocking-bridge")
    licenseApacheV2()
}
```

### Configuring other details

The `mavenCentralPublish { }` contain only the required information. You can add further configurators as follows. Note
that all these configurators override the properties in `mavenCentralPublish { }`.

```kotlin
mavenCentralPublish {
    pom { // this: MavenPom
        // Configures the pom. Example:
        name.set("Project Name Here") // This 'overrides' mavenCentralPublish.projectName
        inceptionYear.set("2021") // Set more optional details
    }
    publication { // this: MavenPublication
        // Configures the publication.
        groupId = "me.him188" // This 'overrides' mavenCentralPublish.groupId
        from(components.getByName("java")) // Add custom component if needed. You may also set `mavenCentralPublish.addProjectComponents` to `false` to disable default components.
        artifact(tasks.get("myCustomJarTask")) // You can a custom artifact
    }
}
```

### Supporting consumers who cannot access Gradle Metadata

This is only for Kotlin MPP with JVM targets. This enables Maven to access your project without the '-jvm' suffix.

```kotlin
mavenCentralPublish {
    publishPlatformArtifactsInRootModule = "jvm" // name of your JVM target ---- it is "jvm" by default.
}
```

### Integration with shadow plugin

As [described](https://imperceptiblethoughts.com/shadow/publishing/#publishing-shadow-jars), Shadow plugin automatically
adds an artifact "$name-$version-all.jar" to all `MavenPublication`s. This file will be included in the publication.

This would work normally if both `mavenCentralPublish.artifactId == project.name`
and `mavenCentralPublish.version == project.version`. However, if not, you should rename the '-all' artifact as follows:

```kotlin
tasks.withType(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    this.archiveFileName.set("${mavenCentralPublish.artifactId}-${mavenCentralPublish.version}-all")
}
```

### Adding custom artifacts

```kotlin
mavenCentralPublish {
    publication {
        artifacts.artifact(tasks.getByName("myCustomJarTask"))
    }
}
```

## Checking your configuration

All tasks are in the group 'publishing' like the task 'publish'.

### Task `checkPublicationCredentials`

Ensures publication credentials is set.

### Task `checkMavenCentralPublication`

Ensures project is ready to publish with all required information configured.

### Task `previewPublication`

Have a preview at the project structure to be published to eliminate your concerns and saves time. Example:

```text
Publication Preview

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

Maven users can add jvm dependency as follows:
<dependency>
    <groupId>group-id</groupId>
    <artifactId>project-name-jvm</artifactId>
    <version>1.0.0</version>
</dependency>

You have configured to publish jvm into root module.
So, Maven users can also add jvm dependency as follows:
<dependency>
    <groupId>group-id</groupId>
    <artifactId>project-name</artifactId>
    <version>1.0.0</version>
</dependency>

Publication Preview End
```

## Troubleshooting

See [Troubleshooting.md](Troubleshooting.md).

## Supporting this plugin

I personally develop various libraries and publish them to Maven Central. So even without anyone's help, I will continue
to maintain this plugin. However, it would be really lovely if you could give me a star!