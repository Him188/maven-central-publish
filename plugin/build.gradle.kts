import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java-gradle-plugin")
    groovy
    `maven-publish`
    id("com.gradle.plugin-publish")
    id("com.github.johnrengelman.shadow")
}

sourceSets.main {
    java {
        srcDir(project(":maven-central-publish-protocol").projectDir.resolve("src/commonMain/kotlin"))
        // so that no need to publish :maven-central-publish-protocol to maven central.
    }
}

kotlin {
    sourceSets.all {
        languageSettings.progressiveMode = true
        languageSettings.useExperimentalAnnotation("kotlin.OptIn")
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

    //api(project(":maven-central-publish-protocol"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${rootProject.extra.get("serialization")}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${rootProject.extra.get("serialization")}")

    api("io.github.karlatemp:PublicationSign:1.1.0")
    api("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")


    testApi(kotlin("test-junit5"))
    testApi("org.junit.jupiter:junit-jupiter-api:${rootProject.extra.get("junit")}")
    testApi("org.junit.jupiter:junit-jupiter-params:${rootProject.extra.get("junit")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${rootProject.extra.get("junit")}")
    testApi("org.spockframework:spock-core:1.3-groovy-2.5")
    testImplementation(kotlin("gradle-plugin"))
    testImplementation(gradleTestKit())
}

tasks.withType(Test::class) {
    useJUnitPlatform()
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
    testSourceSets(sourceSets.test.get())
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
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        jvmTarget = "1.8"
    }
}