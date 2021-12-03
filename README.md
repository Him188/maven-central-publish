# maven-central-publish

Configure publication to Maven Central repository for Gradle projects with minimal effort.

- [How to use this plugin in a local project](UseInLocalProject.md)
- [How to use this plugin in CI environment](UseInCI.md)

## Why this plugin?

### Pack credentials once, use anywhere

You can pack your Sonatype and PGP keys once, then use it around any projects directly. You can add it into your global
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

## Configuring the plugin

The plugin adds a `mavenCentralPublish` configuration.

```kotlin
mavenCentralPublish {
    // TODO
}
```

## Plugin Tasks

### `checkPublicationCredentials`

Ensures publication credentials is set.

### `checkMavenCentralPublication`

Ensures project is ready to publish with all required information configured.

### `previewPublication`

Have a preview at the project structure to be published. Example:

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