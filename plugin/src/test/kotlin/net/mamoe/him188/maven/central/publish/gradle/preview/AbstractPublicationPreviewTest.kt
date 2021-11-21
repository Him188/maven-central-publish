package net.mamoe.him188.maven.central.publish.gradle.preview

import net.mamoe.him188.maven.central.publish.gradle.createTempDirSmart
import java.io.File

abstract class AbstractPublicationPreviewTest {
    val publisherDir: File by lazy { createTempDirSmart() }
    val consumerDir: File by lazy { createTempDirSmart() }
}