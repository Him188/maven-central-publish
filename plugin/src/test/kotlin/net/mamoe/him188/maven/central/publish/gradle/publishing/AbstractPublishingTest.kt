package net.mamoe.him188.maven.central.publish.gradle.publishing

import net.mamoe.him188.maven.central.publish.gradle.createTempDirSmart
import org.gradle.api.internal.artifacts.mvnsettings.DefaultLocalMavenRepositoryLocator
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenFileLocations
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenSettingsProvider
import java.io.File

abstract class AbstractPublishingTest {
    val mavenLocal: File by lazy {
        DefaultLocalMavenRepositoryLocator(DefaultMavenSettingsProvider(DefaultMavenFileLocations())).localMavenRepository
    }

    val publisherDir: File by lazy { createTempDirSmart() }
}