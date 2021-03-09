plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
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
    compileOnly(kotlin("stdlib"))
    testApi(gradleTestKit())
}

pluginBundle {
    website = "https://github.com/Him188/maven-central-publish"
    vcsUrl = "https://github.com/Him188/maven-central-publish"
    tags = listOf("maven", "publishing", "tools")
}

gradlePlugin {
    plugins {
        create("MavenCentralPublish") {
            id = "net.mamoe.him188.maven-central-publish"
            displayName = "Maven Central Publish"
            description = project.description
            implementationClass = "net.mamoe.him188.maven.central.publish.MavenCentralPublishPlugin"
        }
    }
}

kotlin.target.compilations.all {
    kotlinOptions {
        apiVersion = "1.3"
        languageVersion = "1.4"
    }
}