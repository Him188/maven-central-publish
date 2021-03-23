import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.gradle.plugin-publish")
    id("com.github.johnrengelman.shadow")
}

kotlin {
    explicitApi()
}

sourceSets.main {
    java {
        srcDir(project(":maven-central-publish-protocol").projectDir.resolve("src/commonMain/kotlin"))
        // so that no need to publish :maven-central-publish-protocol to maven central.
    }
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
    compileOnly(kotlin("gradle-plugin-api").toString()) {
        isTransitive = false
    }
    compileOnly(kotlin("gradle-plugin").toString()) {
        isTransitive = false
    }
    compileOnly(localGroovy())
    compileOnly(kotlin("stdlib"))
    testApi(gradleTestKit())

    //api(project(":maven-central-publish-protocol"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${rootProject.extra.get("serialization")}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${rootProject.extra.get("serialization")}")

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