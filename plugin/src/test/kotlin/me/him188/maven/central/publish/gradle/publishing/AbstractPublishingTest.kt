package me.him188.maven.central.publish.gradle.publishing

import me.him188.maven.central.publish.gradle.AbstractPluginTest
import me.him188.maven.central.publish.gradle.AbstractPluginTest.Companion.kotlinVersionForTests
import me.him188.maven.central.publish.gradle.createTempDirSmart
import me.him188.maven.central.publish.gradle.publishing.multiplatform.AbstractMultiplatformPublishingTest
import me.him188.maven.central.publish.gradle.registerDeleteHook
import me.him188.maven.central.publish.gradle.testFramework.MavenRunner
import org.gradle.api.internal.artifacts.mvnsettings.DefaultLocalMavenRepositoryLocator
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenFileLocations
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenSettingsProvider
import org.junit.jupiter.api.DynamicTest
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    val kotlinVersionsScope: KotlinVersionsScope?,
) {
    fun AbstractPublishingTest.verifyMetadata(
        verifier: Verifier = {}
    ) = verifyModule(groupId, artifactId, version, expected, verifier)

    fun AbstractPublishingTest.verifyCommon(
        verifier: Verifier = {}
    ) = verifyModuleJvm(groupId, "${artifactId}-common", version, expected, verifier)

    fun AbstractPublishingTest.verifyJvm(
        targetName: String,
        verifier: Verifier = {}
    ) = verifyModuleJvm(groupId, "${artifactId}-$targetName", version, expected, verifier)

    fun AbstractMultiplatformPublishingTest.verifyNative(
        targetName: String,
        withMetadata: Boolean = true,
        verifier: Verifier = {}
    ) = verifyModuleNative(groupId, "${artifactId}-$targetName", version, expected, verifier)

    fun AbstractMultiplatformPublishingTest.verifyJs(
        targetName: String,
        verifier: Verifier = {}
    ) = verifyModuleJs(groupId, "${artifactId}-$targetName", version, expected, verifier)

    fun AbstractMultiplatformPublishingTest.testJvmConsume(
        packageName: String,
        artifactId: String = this@ProjectScope.artifactId,
        kotlinVersion: String = kotlinVersionsScope?.consumerVersion ?: kotlinVersionForTests
    ) {
        testJvmConsume(packageName, groupId, artifactId, version, kotlinVersion)
    }

    fun AbstractMultiplatformPublishingTest.testMavenConsume(
        packageName: String,
        artifactId: String = this@ProjectScope.artifactId,
        kotlinVersion: String = kotlinVersionsScope?.consumerVersion ?: kotlinVersionForTests
    ) {
        testMavenConsume(packageName, groupId, artifactId, version, kotlinVersion)
    }

    fun AbstractMultiplatformPublishingTest.testMultiplatformConsume(
        packageName: String,
        kotlinVersion: String = kotlinVersionsScope?.consumerVersion ?: kotlinVersionForTests
    ) {
        testMultiplatformConsume(packageName, groupId, artifactId, version, kotlinVersion)
    }

    fun AbstractMultiplatformPublishingTest.testMultiplatformJvmOnlyConsume(
        packageName: String,
        kotlinVersion: String = kotlinVersionsScope?.consumerVersion ?: kotlinVersionForTests
    ) {
        testMultiplatformJvmOnlyConsume(packageName, groupId, artifactId, version, kotlinVersion)
    }
}

val VerifierScope.module get() = artifactId

typealias Verifier = VerifierScope.() -> Unit

val mavenLocalDir: File by lazy {
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
        val dir = mavenLocal(groupId).resolve(moduleId.toLowerCase()).resolve(version)
        println("Verifying: " + dir.absolutePath)
        assertEquals(expected, dir.exists(), dir.absolutePath)

        val scope = VerifierScope(groupId.toLowerCase(), moduleId.toLowerCase(), version, dir, expected)

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

    fun projectScope(
        groupId: String,
        artifactId: String,
        version: String,
        expected: Boolean,
        action: ProjectScope.() -> Unit
    ) {
        return ProjectScope(groupId, artifactId, version, expected, null).run(action)
    }

    fun KotlinVersionsScope.projectScope(
        groupId: String,
        artifactId: String,
        version: String,
        expected: Boolean,
        action: ProjectScope.() -> Unit
    ) {
        return ProjectScope(groupId, artifactId, version, expected, this).run(action)
    }


    fun testJvmConsume(
        packageName: String,
        groupId: String,
        artifactId: String,
        version: String,
        kotlinVersion: String = kotlinVersionForTests,
    ) {
        val consumerDir = createTempDirSmart()
        consumerDir.mkdirs()
        consumerDir.resolve("gradle.properties").writeText("\n")
        consumerDir.resolve("settings.gradle.kts").writeText("rootProject.name = \"test\"")
        consumerDir.resolve("src/main/kotlin/").mkdirs()
        consumerDir.resolve("src/main/kotlin/main.kt")
            .writeText("import $packageName.Test; \nfun main() { println(Test.toString()) }")
        consumerDir.resolve("build.gradle.kts").writeText(
            """
                plugins {
                    kotlin("jvm") version "$kotlinVersion"
                }
                repositories { mavenCentral(); mavenLocal() }
                dependencies {
                    implementation("$groupId:$artifactId:$version")
                }
            """.trimIndent()
        )
        assertGradleTaskSuccess(consumerDir, "assemble")
    }

    fun testMavenConsume(
        packageName: String,
        groupId: String,
        artifactId: String,
        version: String,
        kotlinVersion: String = kotlinVersionForTests,
    ) {
        val mavenDir = createTempDirSmart()
        mavenDir.mkdirs()
        mavenDir.resolve("src/main/java/test/").mkdirs()
        mavenDir.resolve("src/main/java/test/Main.java")
            .writeText(
                """
                    package test;
                    import $packageName.Test; 
                    public class Main {
                        public static void main(String[] args) { 
                            System.out.println(Test.INSTANCE.toString());
                        }
                    }
                """.trimIndent()
            )

        MavenRunner.runMaven(mavenDir, kotlinVersion, "package") {
            addDependency(groupId, artifactId, version)
        }
        assertTrue { mavenDir.resolve("target/classes/test/Main.class").exists() }
    }


    ///////////////////////////////////////////////////////////////////////////
    // createTestsForKotlinVersions
    ///////////////////////////////////////////////////////////////////////////

    fun withKotlinVersions(block: KotlinVersionsScope.() -> Unit): KotlinVersionsScope.() -> Unit = block

    open fun createTestsForKotlinVersions(
        versions: List<String> = supportingKotlinVersions,
        runTest: KotlinVersionsScope.() -> Unit
    ): List<DynamicTest> {
        fun List<String>.permutations(): List<Pair<String, String>> {
            return this.associateWith { this }.flatMap { (key, value) -> value.map { key to it } }
        }

        return versions.permutations().map { (publisherVersion, consumerVersion) ->
            DynamicTest.dynamicTest("when publisher use Kotlin $publisherVersion, consumer use $consumerVersion") {
                try {
                    runTest(KotlinVersionsScope(publisherVersion, consumerVersion))
                } finally {
                    publisherDir.deleteRecursively()
                    publisherDir.mkdirs()
                }
            }
        }
    }
}

class KotlinVersionsScope(
    val publisherVersion: String,
    val consumerVersion: String
)
