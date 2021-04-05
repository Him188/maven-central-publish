# maven-central-publish

Configure publication to Maven Central repository for Gradle projects with minimal effort.

- [How to use this plugin in a local project](UseInLocalProject.md)
- [How to use this plugin in CI environment](UseInCI.md)

## Features

### Task `checkPublicationCredentials`

Ensures publication credentials is set.

### Task `checkMavenCentralPublication`

Ensures project is ready to publish with all required information configured.

### Publication `mavenCentral`

Registers a publication named `mavenCentral`. Configures poms, repositories, credentials, and all other information required by Maven Central.