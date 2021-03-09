pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "maven-central-publish"

includeProject("plugin", "plugin")

fun includeProject(name: String, path: String) {
    include(path)
    project(":$path").name = name
}