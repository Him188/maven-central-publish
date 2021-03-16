pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "maven-central-publish"

includeProject("maven-central-publish-gradle", "plugin")
includeProject("maven-central-publish-protocol", "protocol")
includeProject("maven-central-publish-generator", "generator")

fun includeProject(name: String, path: String) {
    include(path)
    project(":$path").name = name
}