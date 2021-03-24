# How to use this plugin in a local project

- [I have used this plugin before](#i-have-used-this-plugin-before)
- [I am new to publication](#i-am-new-to-publication)
- [I already had ***credentials***](#i-already-had-credentials)

## I have used this plugin before

1. Apply Gradle plugin `net.mamoe.maven-central-publish`, version `0.1.12`
2. Configure `mavenCentralPublish`  
   Raed `MavenCentralPublishExtension.pomConfigurators` for required information, **or** simply configure a GitHub project like:
   ```kotlin
   mavenCentralPublish {
       singleDevGithubProject("Him188", "yamlkt")
       licenseFromGitHubProject("Apache-2.0", "master")
   }
   ```
3. Set ***credentials:*** set any of Gradle property, JVM property, JVM or system environment variable named `publication.credentials` or `PUBLICATION_CREDENTIALS`
4. Finish

----

## I am new to publication

If you publish your artifact (namely your project files) to [Maven Central repository](https://repo.maven.apache.org/maven2/), anyone can access your files directly,
in Gradle by declaring `mavenCentral()` repository and in maven that is already done by default.

Anyone can publish artifacts to the Maven Central repository (MC in short), however MC has strict rules.

If you are a personal developer, you usually don't need to pay specific attention to these rule, just follow this guide and use this plugin.

### Register and activate Sonatype account

[Karlatemp/PublicationSign]: https://raw.githubusercontent.com/Karlatemp/PublicationSign
[@Karlatemp]: https://github.com/Karlatemp/

Follow steps 1 to 7 described by [How to Publish Your Artifacts to Maven Central](https://dzone.com/articles/publish-your-artifacts-to-maven-central). (Thanks to the original author)

### Get Sonatype User Token

<https://oss.sonatype.org/>

1. Login with your Sonatype account
2. On the upper right corner, click your username
3. Click `Profile`
4. Click `Summary` and choose `User Token`
5. `Access User Token`
6. Note down the ***`username`*** and ***`password`***, this will be your Sonatype User Token.

You can also use this pair of username and password to log into your account, so it is just like another way to access your Sonatype account, but you can reset the User Token so it is more safe.

### Generate key pair

1. Generate a key pair
   1. Install [GnuPG](https://www.gnupg.org/download/) (Binary releases)

   2. Download [`key-gen.sh`](https://raw.githubusercontent.com/Karlatemp/PublicationSign/master/key-gen.sh) (from [Karlatemp/PublicationSign] by [@Karlatemp])

   3. Execute `key-gen.sh`, and you will get `keys.gpg.pub` and `keys.gpg` that are to be used later.
      - `keys.gpg.pub`: the ***public key***
      - `keys.gpg`: the ***private key***

2. Upload the key pair to a public keyserver

   <http://pool.sks-keyservers.net:11371/>

   Open `keys.gpg.pub` as a text file, upload its content to the website.

### Prepare *credentials*

*Credentials* is a pack of your Sonatype user and key pair that can be re-used for further publications.

1. Download a generator from [releases](https://github.com/Him188/maven-central-publish/releases)
2. Create a directory, place the following files into it.
   - the generator you just downloaded
   - `keys.gpg.pub`: your GnuPG ***public key***
   - `keys.gpg`: your GnuPG ***private key***
   - `sonatype.txt`: a text file that contains your ***Sonatype User Token***, with ***`username`*** in the first line, and the ***`password`*** in the second line.
3. Execute the generator.
4. The keys are packed into a file `credentials.txt`, the content of which is your ***`credentials`***

You can re-use the ***`credentials`*** for all of your projects.

Then follow the chapter [I already had credentials](#i-already-had-credentials).

## I already had credentials

You need ***`credentials`***, otherwise read [I am new to publication](#i-am-new-to-publication) first.

### 1. Apply Gradle plugin

**Note that the plugin should be applied before any other plugins, in other words, at the first line of `plugins` block.**

The plugin should be applied to the subproject that needs to be published. Applying the plugin to the root project will not affect subprojects.

#### Using `build.gradle.kts`
```kotlin
plugins {
    id("net.mamoe.maven-central-publish") version "0.1.12"
    // then apply other plugins if needed
}
```

#### Using `build.gradle`
```
plugins {
    id 'net.mamoe.maven-central-publish' version '0.1.12' 
    // then apply other plugins if needed
}
```

### 2. Configure publishing

#### Single developer GitHub project

A simple configurator for GitHub projects with single developer.

```kotlin
mavenCentralPublish {
    singleDevGithubProject("GitHub username of the owner of the repository", "GitHub repository name")
    licenseFromGitHubProject("Open-source License name", "Repository main branch nagradle.propertiesme")
}
```

A real example from [yamlkt](https://github.com/him188/yamlkt):
```kotlin
mavenCentralPublish {
    singleDevGithubProject("Him188", "yamlkt")
    licenseFromGitHubProject("Apache-2.0", "master")
}
```

#### Multi developers GitHub project

```kotlin
mavenCentralPublish {gradle.properties
    githubProject("GitHub username of the owner of the repository", "GitHub repository name")
    licenseFromGitHubProject("Open-source License name", "Repository main branch name")
    developer("Developer1")
    developer("Developer2") // add as many as needed
}
```

#### Other projects: Set manually

Read `MavenCentralPublishExtension.publicationConfigurators` for detailed manual configurations.

### 3. Add ***credentials***

You should have got ***credentials*** [previously](#prepare-credentials), then follow one of the following methods.

#### Set in `gradle.properties`

In your `GRADLE_HOME` (to set globally) or project dir (to set locally), open or create `gradle.properties`, append a line:
```
publication.credentials=CONTENT OF credentials.txt
```

This should be like:
```
publication.credentials=0afc0c2d2d2d2... (very long)
```

#### Set in system environment

Add a system environment variable named `publication.credentials` or `PUBLICATION_CREDENTIALS` with the content of ***credentials.txt*** (not the file path)

#### Set in JVM environment

Add an JVM environment variable named `publication.credentials` or `PUBLICATION_CREDENTIALS` with the content of ***credentials.txt*** (not the file path)

#### Set in Gradle command line

Add argument `-Ppublication.credentials=CONTENT OF credentials.txt`.

#### Load manually on configuration

```groovy
mavenCentralPublish {
    credentials = TODO()
}
```

### 4. Finish

Now you can execute the `publish` task to upload artifacts. If you want to use this plugin a second time, just read [I have used this plugin before](#i-have-used-this-plugin-before)

## After uploading the artifacts

When `publish` executed successfully, you need to do `close` and `release` on Sonatype <https://oss.sonatype.org/#stagingRepositories>.

If a `close` has failed, it means this plugin has done something wrong. Please report to [issues](https://github.com/Him188/maven-central-publish/issues).

If all succeed, your repository will be in Maven Central and can be accessed publicly. You can check on <https://mvnrepository.com/>.

### Automation

`close` and `release` can be done automatically by another plugin <https://github.com/bmuschko/gradle-nexus-plugin>.  
`maven-central-publish` has already configured that for you. You can execute task `closeAndReleaseRepository` in root project.

However, this can only handle single publication. If you publish multiple modules at one time, you can only close and release on the Sonatype website.
