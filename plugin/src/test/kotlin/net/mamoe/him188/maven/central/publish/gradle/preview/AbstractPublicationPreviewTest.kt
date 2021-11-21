package net.mamoe.him188.maven.central.publish.gradle.net.mamoe.him188.maven.central.publish.gradle.preview

import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class AbstractPublicationPreviewTest {
    @TempDir
    lateinit var publisherDir: File

    @TempDir
    lateinit var consumerDir: File

}