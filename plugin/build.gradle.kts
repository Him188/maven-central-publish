import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.gradle.plugin-publish")
    id("com.github.johnrengelman.shadow")
}

kotlin {
    explicitApi()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
    compileOnly(kotlin("gradle-plugin-api").toString()) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }
    compileOnly(localGroovy())
    compileOnly(kotlin("stdlib"))
    testApi(gradleTestKit())

    api(project(":maven-central-publish-protocol"))

    api("io.github.karlatemp:PublicationSign:1.1.0")
    api("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")
}

tasks.getByName("shadowJar", ShadowJar::class) {
    archiveClassifier.set("")
}

tasks.getByName("publishPlugins").dependsOn("shadowJar")

pluginBundle {
    website = "https://github.com/Him188/maven-central-publish"
    vcsUrl = "https://github.com/Him188/maven-central-publish"
    tags = listOf("maven", "publishing", "tools")
}

gradlePlugin {
    plugins {
        create("MavenCentralPublish") {
            id = "net.mamoe.maven-central-publish"
            displayName = "Maven Central Publish"
            description = project.description
            implementationClass = "net.mamoe.him188.maven.central.publish.gradle.MavenCentralPublishPlugin"
        }
    }
}

kotlin.target.compilations.all {
    kotlinOptions {
        apiVersion = "1.3"
        languageVersion = "1.4"
        freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all"
        jvmTarget = "1.8"
    }
}