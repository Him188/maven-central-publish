package me.him188.maven.central.publish.gradle

import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.protobuf.ProtoBuf
import me.him188.maven.central.publish.protocol.PublicationCredentials
import org.gradle.api.Project

internal object Credentials {
    private fun find(project: Project, propName: String): Any? {
        return project.findProperty(propName)
            ?: System.getProperty(propName, null)
            ?: System.getenv(propName)
    }

    fun findCredentials(project: Project): PublicationCredentials? {
        val info = find(project, "PUBLICATION_CREDENTIALS")
            ?: find(project, "PUBLICATION.CREDENTIALS")
            ?: find(project, "publication.credentials")
            ?: return null

        @Suppress("EXPERIMENTAL_API_USAGE")
        return ProtoBuf.decodeFromHexString(PublicationCredentials.serializer(), info.toString())
    }

    fun check(credentials: PublicationCredentials) {
        check(credentials.gpgPublicKey.trimStart().startsWith(GPG_PUBLIC_KEY_BEGIN)) { "Invalid GPG public key" }
        check(credentials.gpgPublicKey.trimEnd().endsWith(GPG_PUBLIC_KEY_END)) { "Invalid GPG public key" }

        check(credentials.gpgPrivateKey.trimStart().startsWith(GPG_PRIVATE_KEY_BEGIN)) { "Invalid GPG private key" }
        check(credentials.gpgPrivateKey.trimEnd().endsWith(GPG_PRIVATE_KEY_END)) { "Invalid GPG private key" }

        check(credentials.sonatypePassword.isNotBlank()) { "Sonatype password must not be empty." }
        check(credentials.sonatypeUsername.isNotBlank()) { "Sonatype username must not be empty." }
    }

    const val GPG_PUBLIC_KEY_BEGIN = "-----BEGIN GPG PUBLIC KEY BLOCK-----"
    const val GPG_PUBLIC_KEY_END = "-----END GPG PUBLIC KEY BLOCK-----"
    const val GPG_PRIVATE_KEY_BEGIN = "-----BEGIN GPG PRIVATE KEY BLOCK-----"
    const val GPG_PRIVATE_KEY_END = "-----END GPG PRIVATE KEY BLOCK-----"
}