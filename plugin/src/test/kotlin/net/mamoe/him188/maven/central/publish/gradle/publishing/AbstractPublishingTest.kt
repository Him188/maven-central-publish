package net.mamoe.him188.maven.central.publish.gradle.publishing

import net.mamoe.him188.maven.central.publish.gradle.AbstractPluginTest
import net.mamoe.him188.maven.central.publish.gradle.createTempDirSmart
import net.mamoe.him188.maven.central.publish.gradle.publishing.multiplatform.AbstractMultiplatformPublishingTest
import net.mamoe.him188.maven.central.publish.gradle.registerDeleteHook
import org.gradle.api.internal.artifacts.mvnsettings.DefaultLocalMavenRepositoryLocator
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenFileLocations
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenSettingsProvider
import java.io.File
import kotlin.test.assertEquals

class VerifierScope(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val dir: File,
    val expected: Boolean,
) {
    fun verify(file: String, andSignature: Boolean = true) {
        assertEquals(expected, dir.resolve(file).exists(), message = dir.resolve(file).absolutePath)
        if (andSignature && !file.endsWith(".asc")) {
            assertEquals(
                expected,
                dir.resolve("$file.asc").exists(),
                message = dir.resolve("$file.asc").absolutePath
            )
        }
    }

    fun verifyCommon() {
        verify("$artifactId-$version-sources.jar")
        verify("$artifactId-$version.module")
        verify("$artifactId-$version.pom")
    }
}

class ProjectScope(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val expected: Boolean,
) {
    fun AbstractPublishingTest.verifyMetadata(
        verifier: Verifier = {}
    ) = verifyModule(groupId, artifactId, version, expected, verifier)

    fun AbstractPublishingTest.verifyJvm(
        targetName: String,
        verifier: Verifier = {}
    ) = verifyModuleJvm(groupId, "${artifactId}-$targetName", version, expected, verifier)

    fun AbstractPublishingTest.verifyNative(
        targetName: String,
        verifier: Verifier = {}
    ) = verifyModuleNative(groupId, "${artifactId}-$targetName", version, expected, verifier)

    fun AbstractPublishingTest.verifyJs(
        targetName: String,
        verifier: Verifier = {}
    ) = verifyModuleJs(groupId, "${artifactId}-$targetName", version, expected, verifier)

    fun AbstractMultiplatformPublishingTest.testJvmConsume(packageName: String) {
        testJvmConsume(packageName, groupId, artifactId, version)
    }

    fun AbstractMultiplatformPublishingTest.testMultiplatformConsume(packageName: String) {
        testMultiplatformConsume(packageName, groupId, artifactId, version)
    }
}

val VerifierScope.module get() = artifactId

typealias Verifier = VerifierScope.() -> Unit


private val mavenLocalDir: File by lazy {
    DefaultLocalMavenRepositoryLocator(DefaultMavenSettingsProvider(DefaultMavenFileLocations())).localMavenRepository
}

fun mavenLocal(groupId: String) = mavenLocalDir.resolve(groupId).apply { registerDeleteHook() }


abstract class AbstractPublishingTest : AbstractPluginTest() {
    val publisherDir: File by lazy { createTempDirSmart() }

    fun runPublishToMavenLocal() = assertGradleTaskSuccess(publisherDir, "publishToMavenLocal")

    /**
     * Verifies:
     * - `-sources.jar`
     * - `.module`
     * - `.pom`
     */
    fun verifyModule(
        groupId: String,
        moduleId: String,
        version: String,
        expected: Boolean,
        verifier: Verifier = {}
    ) {
        val dir = mavenLocal(groupId).resolve(moduleId).resolve(version)
        println("Verifying: " + dir.absolutePath)
        assertEquals(expected, dir.exists(), dir.absolutePath)

        val scope = VerifierScope(groupId, moduleId, version, dir, expected)

        scope.verifier()
        scope.verifyCommon()
    }

    /**
     * Verifies:
     * - `-sources.jar`
     * - `.module`
     * - `.pom`
     */
    fun verifyModuleMetadata(
        groupId: String,
        artifactId: String,
        version: String,
        expected: Boolean,
        verifier: Verifier = {}
    ) = verifyModule(groupId, artifactId, version, expected, verifier)

    /**
     * Verifies:
     * - `.jar`
     * - `-javadoc.jar`
     * - `-sources.jar`
     * - `.module`
     * - `.pom`
     */
    fun verifyModuleJvm(
        groupId: String,
        moduleName: String,
        version: String,
        expected: Boolean,
        verifier: Verifier = {}
    ) = verifyModule(groupId, moduleName, version, expected) {
        verifier()
        verify("$module-$version.jar")
        verify("$module-$version-javadoc.jar")
    }

    /**
     * Verifies:
     * - `.klib`
     * - `-javadoc.jar`
     * - `-sources.jar`
     * - `.module`
     * - `.pom`
     */
    fun verifyModuleNative(
        groupId: String,
        moduleName: String,
        version: String,
        expected: Boolean,
        verifier: Verifier = {}
    ) = verifyModule(groupId, moduleName, version, expected) {
        verifier()
        verify("$module-$version.klib")
        verify("$module-$version-javadoc.jar")
    }

    /**
     * Verifies:
     * - `-samplessources.jar`
     * - `-javadoc.jar`
     * - `-sources.jar`
     * - `.module`
     * - `.pom`
     */
    fun verifyModuleJs(
        groupId: String,
        moduleName: String,
        version: String,
        expected: Boolean,
        verifier: Verifier = {}
    ) = verifyModule(groupId, moduleName, version, expected) {
        verifier()
        verify("$module-$version-samplessources.jar")
        verify("$module-$version-javadoc.jar")
    }

    fun projectScope(
        groupId: String,
        artifactId: String,
        version: String,
        expected: Boolean,
        action: ProjectScope.() -> Unit
    ) {
        return ProjectScope(groupId, artifactId, version, expected).run(action)
    }
}