# Troubleshooting

This document shows frequently asked questions and their solutions.

## Publishing

### `org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':generateMetadataFileForKotlinMultiplatformPublication'.`

```text
Caused by: org.gradle.api.InvalidUserCodeException: Invalid publication 'kotlinMultiplatform':

- Variants 'androidApiElements-published' and 'commonApiElements-published' have the same attributes and capabilities.
  Please make sure either attributes or capabilities are different.
- Variants 'androidRuntimeElements-published' and 'commonRuntimeElements-published' have the same attributes and
  capabilities. Please make sure either attributes or capabilities are different.
- Variants 'androidApiElements-published' and 'jvmApiElements-published' have the same attributes and capabilities.
  Please make sure either attributes or capabilities are different.
- Variants 'androidRuntimeElements-published' and 'jvmRuntimeElements-published' have the same attributes and
  capabilities. Please make sure either attributes or capabilities are different.
```

This is known to happen in hierarchical MPP. To solve this issue, simply apply the plugin after Kotlin plugin.

```kotlin
plugins {
    kotlin("multiplatform") version "1.6.0"
    id("me.him188.maven-central-publish") // after Kotlin
}
```

## Signing

One step in 'publish' is signing. The plugin calls external program GPG to sign your files.

### GPG command response 2 != 0

```text
Execution failed for task ':kotlin-jvm-blocking-bridge-compiler:signPublicationMavenCentral'.
> GPG command response 2 != 0, 'gpg --homedir gpg-homedir --batch --import /Users/him188/Projects/kotlin-jvm-blocking-bridge/compiler-plugin/build/publishing-tmp/keys/key.pub'
```

GPG requires a short filepath. This means your workingDir is too long. You can change it in the `mavenCentralPublish`
configuration.

```kotlin
mavenCentralPublish {
    workingDir = file("/Users/him188/tmp/pub-temp") // change here, make it as short as it can.
}
```

## Consuming the project

### Maven could not resolve MPP project

Kotlin MPP project is not directly accessible from the root module by consumers who cannot access Gradle Metadata, e.g.
Maven.   
You will need to include a platform module suffix, '-jvm' for example, to access such modules.  
However, this plugin supports a simpler configuration, that adds a dependency in root module towards your platform
module. So that depending on the root module also works for Maven.

```kotlin
mavenCentralPublish {
    // For MPP with JVM target. This enables Maven to access your project without the '-jvm' suffix.
    publishPlatformArtifactsInRootModule = "jvm" // name of your JVM target ---- it is "jvm" by default.
}
```