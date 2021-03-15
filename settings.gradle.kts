pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "maven-central-publish"

includeProject("plugin", "plugin")
includeProject("protocol", "protocol")
includeProject("generator", "generator")

fun includeProject(name: String, path: String) {
    include(path)
    project(":$path").name = name
}