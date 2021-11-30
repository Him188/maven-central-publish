package me.him188.maven.central.publish.gradle.configuration

import me.him188.maven.central.publish.gradle.MavenCentralPublishPlugin
import org.junit.jupiter.api.Test
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SecurityFileTest : AbstractPluginConfigurationTest() {

    @Test
    fun `should pass if workingDir does not exists`() {
        val resolve = tempDir.resolve("empty-dir")
        assertTrue { !resolve.exists() }
        MavenCentralPublishPlugin.checkSecurityFile(resolve)
    }

    @Test
    fun `should pass if workingDir is empty`() {
        val resolve = tempDir.resolve("empty-dir")
        resolve.mkdirs()
        assertTrue { resolve.exists() && resolve.isDirectory && resolve.listFiles()!!.isEmpty() }
        MavenCentralPublishPlugin.checkSecurityFile(resolve)
    }

    @Test
    fun `should pass if workingDir is not empty with security file`() {
        val resolve = tempDir.resolve("empty-dir")
        resolve.mkdirs()
        assertTrue { resolve.exists() }
        assertTrue { resolve.isDirectory }
        tempDir.resolve(MavenCentralPublishPlugin.SECURITY_FILE_NAME).writeText("1")
        tempDir.resolve("something.txt").writeText("1")
        MavenCentralPublishPlugin.checkSecurityFile(resolve)
    }

    @Test
    fun `should fail if workingDir is file`() {
        val resolve = tempDir.resolve("empty-dir")
        resolve.writeText("1")
        assertTrue { resolve.exists() && resolve.isFile }
        assertFails { MavenCentralPublishPlugin.checkSecurityFile(resolve) }.let { exception ->
            assertTrue { exception.message!!.contains("is not a directory") }
        }
    }

    @Test
    fun `should fail if workingDir is not empty without security file`() {
        val resolve = tempDir.resolve("empty-dir")
        resolve.mkdirs()
        assertTrue { resolve.exists() && resolve.isDirectory }
        tempDir.resolve("something.txt").writeText("1")
        assertFalse { tempDir.resolve(MavenCentralPublishPlugin.SECURITY_FILE_NAME).exists() }
        MavenCentralPublishPlugin.checkSecurityFile(resolve)
    }
}