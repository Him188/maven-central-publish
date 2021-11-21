package net.mamoe.him188.maven.central.publish.gradle.publishing

import org.gradle.api.internal.artifacts.mvnsettings.DefaultLocalMavenRepositoryLocator
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenFileLocations
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenSettingsProvider
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class AbstractPublishingTest {
    val mavenLocal: File by lazy {
        DefaultLocalMavenRepositoryLocator(DefaultMavenSettingsProvider(DefaultMavenFileLocations())).localMavenRepository
    }

    @TempDir
    lateinit var publisherDir: File

    @TempDir
    lateinit var consumerDir: File

}